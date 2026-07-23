package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.dto.request.webhook.SepayWebhookRequest;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.order.OrderEntity;
import vn.springboot.entity.payment.PaymentTransactionEntity;
import vn.springboot.event.OrderPlacedEvent;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.PaymentTransactionRepository;
import vn.springboot.service.PaymentWebhookService;

import java.util.List;

/**
 * Handles the SePay webhook: idempotent by SePay transaction id, matches the order
 * by transfer memo (= order code), marks it PAID + PROCESSING when the amount is
 * enough, and fires the (previously deferred) confirmation email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private static final String TRANSFER_IN = "in";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void handleSepay(SepayWebhookRequest payload) {
        // 1) Idempotency — same SePay transaction (retry/replay) is processed once.
        if (payload.getId() == null || paymentTransactionRepository.existsBySepayId(payload.getId())) {
            return;
        }

        String orderCode = payload.getCode();
        boolean matched = false;

        // 2) Only incoming transfers can pay an order.
        if (TRANSFER_IN.equalsIgnoreCase(payload.getTransferType()) && orderCode != null && !orderCode.isBlank()) {
            OrderEntity order = orderRepository.findByOrderCode(orderCode.trim()).orElse(null);
            if (order != null
                    && order.getPaymentStatus() == PaymentStatus.PENDING
                    // A cancelled/returned order must never be payable — the customer can
                    // self-cancel a PENDING_PAYMENT order (BE-3) while its VietQR is still
                    // technically transferable; without this guard a stray/late transfer
                    // would silently mark a cancelled order PAID with no reconciliation path.
                    && order.getStatus() != OrderStatus.CANCELLED
                    && order.getStatus() != OrderStatus.RETURNED
                    && payload.getTransferAmount() >= order.getTotalAmount()) {
                order.setPaymentStatus(PaymentStatus.PAID);
                if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    order.setStatus(OrderStatus.PROCESSING);
                }
                orderRepository.save(order);
                publishConfirmationEmail(order);
                matched = true;
                log.info("Order {} marked PAID via SePay txn {}", orderCode, payload.getId());
            } else {
                log.info("SePay txn {} did not match a payable order (code={})", payload.getId(), orderCode);
            }
        }

        // 3) Record the transaction (UNIQUE sepay_id is the race-safe idempotency backstop).
        paymentTransactionRepository.save(PaymentTransactionEntity.builder()
                .sepayId(payload.getId())
                .orderCode(orderCode)
                .gateway(payload.getGateway())
                .amount(payload.getTransferAmount())
                .transferType(payload.getTransferType())
                .referenceCode(payload.getReferenceCode())
                .transactionDate(payload.getTransactionDate())
                .content(payload.getContent())
                .matched(matched)
                .build());
    }

    /** Rebuilds the order-placed event (same one used for COD) so the email is sent after commit. */
    private void publishConfirmationEmail(OrderEntity order) {
        List<OrderPlacedEvent.Item> items = orderItemRepository.findByOrder_IdOrderByIdAsc(order.getId()).stream()
                .map(i -> new OrderPlacedEvent.Item(
                        i.getProductName(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .toList();
        long subtotal = items.stream().mapToLong(OrderPlacedEvent.Item::subtotal).sum();

        eventPublisher.publishEvent(new OrderPlacedEvent(
                order.getUser().getEmail(), order.getOrderCode(),
                subtotal, order.getDiscountAmount(), order.getTotalAmount(),
                order.getReceiverName(), order.getReceiverPhone(), order.getReceiverAddress(),
                items));
    }
}
