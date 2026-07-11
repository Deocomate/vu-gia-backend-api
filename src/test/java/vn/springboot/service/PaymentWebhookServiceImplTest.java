package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import vn.springboot.dto.request.webhook.SepayWebhookRequest;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.order.OrderEntity;
import vn.springboot.entity.payment.PaymentTransactionEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.event.OrderPlacedEvent;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.PaymentTransactionRepository;
import vn.springboot.service.impl.PaymentWebhookServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private PaymentWebhookServiceImpl service;

    private SepayWebhookRequest payload(long id, String code, long amount, String type) {
        return SepayWebhookRequest.builder()
                .id(id).code(code).transferAmount(amount).transferType(type)
                .gateway("MBBank").content(code + " chuyen tien").referenceCode("FT1")
                .transactionDate("2024-07-02 11:08:33").build();
    }

    private OrderEntity order(String code, long total, OrderStatus status, PaymentStatus paymentStatus) {
        UserEntity user = new UserEntity();
        user.setEmail("buyer@example.com");
        OrderEntity o = OrderEntity.builder()
                .orderCode(code).totalAmount(total).status(status).paymentStatus(paymentStatus)
                .user(user).receiverName("A").receiverPhone("0900").receiverAddress("HN").build();
        o.setId(5L);
        return o;
    }

    @Test
    void alreadyProcessed_isNoOp() {
        when(paymentTransactionRepository.existsBySepayId(92704L)).thenReturn(true);

        service.handleSepay(payload(92704L, "OD1", 100_000, "in"));

        verify(orderRepository, never()).findByOrderCode(any());
        verify(paymentTransactionRepository, never()).save(any());
    }

    @Test
    void matched_marksPaidProcessing_publishesEmail_recordsTxn() {
        OrderEntity order = order("OD1", 100_000, OrderStatus.PENDING_PAYMENT, PaymentStatus.PENDING);
        when(paymentTransactionRepository.existsBySepayId(1L)).thenReturn(false);
        when(orderRepository.findByOrderCode("OD1")).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(anyLong())).thenReturn(List.of());

        service.handleSepay(payload(1L, "OD1", 100_000, "in"));

        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class));
        verify(paymentTransactionRepository).save(any(PaymentTransactionEntity.class));
    }

    @Test
    void underpaid_doesNotMarkPaid_butRecordsTxn() {
        OrderEntity order = order("OD1", 100_000, OrderStatus.PENDING_PAYMENT, PaymentStatus.PENDING);
        when(paymentTransactionRepository.existsBySepayId(1L)).thenReturn(false);
        when(orderRepository.findByOrderCode("OD1")).thenReturn(Optional.of(order));

        service.handleSepay(payload(1L, "OD1", 50_000, "in")); // less than total

        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentTransactionRepository).save(any(PaymentTransactionEntity.class));
    }

    @Test
    void orderNotFound_recordsTxnOnly() {
        when(paymentTransactionRepository.existsBySepayId(1L)).thenReturn(false);
        when(orderRepository.findByOrderCode("ODX")).thenReturn(Optional.empty());

        service.handleSepay(payload(1L, "ODX", 100_000, "in"));

        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentTransactionRepository).save(any(PaymentTransactionEntity.class));
    }

    @Test
    void outgoingTransfer_ignoredForMatching_butRecorded() {
        when(paymentTransactionRepository.existsBySepayId(1L)).thenReturn(false);

        service.handleSepay(payload(1L, "OD1", 100_000, "out"));

        verify(orderRepository, never()).findByOrderCode(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentTransactionRepository).save(any(PaymentTransactionEntity.class));
    }
}
