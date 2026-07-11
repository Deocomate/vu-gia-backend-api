package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.order.OrderItemRequest;
import vn.springboot.dto.request.order.OrderPlaceRequest;
import vn.springboot.entity.coupon.CouponEntity;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentMethod;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.enums.ProductType;
import vn.springboot.entity.order.OrderEntity;
import vn.springboot.entity.order.OrderItemEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.event.OrderPlacedEvent;
import vn.springboot.repository.CartItemRepository;
import vn.springboot.repository.CouponRepository;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.ProductRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The transactional unit that materialises an order. Kept separate from the
 * orchestrating {@code OrderServiceImpl} so the idempotency retry (which catches
 * a unique-constraint violation) sits <em>outside</em> this transaction and can
 * safely re-read the winning order after this one rolls back.
 */
@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final CartItemRepository cartItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderEntity create(UserEntity user, OrderPlaceRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_EMPTY);
        }

        // Snapshot each line from the live product (name/type/price frozen at order time).
        Map<Long, ProductEntity> products = loadProducts(request.getItems());
        // COD by default; ONL means the customer pays online (VietQR) before fulfilment.
        PaymentMethod paymentMethod = request.getPaymentMethod() != null
                ? request.getPaymentMethod()
                : PaymentMethod.COD;

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .orderCode(generateOrderCode())
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(paymentMethod)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .note(request.getNote())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        List<OrderItemEntity> items = request.getItems().stream()
                .map(line -> buildItem(order, products.get(line.getProductId()), line.getQuantity()))
                .toList();
        long orderAmount = items.stream().mapToLong(OrderItemEntity::getSubtotal).sum();

        // Coupon: validate every condition, then atomically claim one usage.
        long discount = applyCoupon(order, request.getCouponCode(), user, orderAmount);
        order.setTotalAmount(orderAmount - discount);

        orderRepository.save(order);          // may throw on duplicate (user, idempotencyKey)
        orderItemRepository.saveAll(items);

        // NOTE: sold_count is bumped only when the order reaches COMPLETED
        // (see OrderServiceImpl.updateStatus) — placing an order that later gets
        // cancelled/refunded must not inflate "đã bán".

        // Reflect what was bought back into the cart (reduce or remove the matching lines).
        deductFromCart(user.getId(), request.getItems());

        // COD → confirm immediately (email now). ONL → wait until payment is confirmed
        // (the webhook, added later, will trigger the email), so don't send it here.
        if (paymentMethod == PaymentMethod.COD) {
            List<OrderPlacedEvent.Item> emailItems = items.stream()
                    .map(i -> new OrderPlacedEvent.Item(
                            i.getProductName(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                    .toList();
            eventPublisher.publishEvent(new OrderPlacedEvent(
                    user.getEmail(), order.getOrderCode(),
                    orderAmount, discount, order.getTotalAmount(),
                    order.getReceiverName(), order.getReceiverPhone(), order.getReceiverAddress(),
                    emailItems));
        }

        return order;
    }

    private Map<Long, ProductEntity> loadProducts(List<OrderItemRequest> lines) {
        List<Long> ids = lines.stream().map(OrderItemRequest::getProductId).distinct().toList();
        Map<Long, ProductEntity> byId = productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));
        // Any requested id that didn't resolve → hard fail before touching money.
        for (Long id : ids) {
            if (!byId.containsKey(id)) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND, "Sản phẩm không tồn tại: " + id);
            }
        }
        return byId;
    }

    private OrderItemEntity buildItem(OrderEntity order, ProductEntity product, int quantity) {
        long unitPrice = product.getPrice();
        return OrderItemEntity.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .productType(product.getType())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .subtotal(unitPrice * quantity)
                // COMBO lines keep a snapshot of the combo makeup; SINGLE lines have none.
                .comboItems(product.getType() == ProductType.COMBO ? product.getComboProducts() : null)
                .build();
    }

    /**
     * Applies a coupon if one was supplied. Checks all conditions, then claims a
     * usage slot with a single atomic UPDATE (race-free, no lock). Returns the
     * discount to subtract from the order amount.
     */
    private long applyCoupon(OrderEntity order, String rawCode, UserEntity user, long orderAmount) {
        if (rawCode == null || rawCode.isBlank()) {
            return 0L;
        }
        String code = rawCode.trim().toUpperCase();
        CouponEntity coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND, "Mã giảm giá không tồn tại"));

        if (!coupon.isActive()) {
            throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE, "Mã giảm giá đã bị vô hiệu hoá");
        }
        Instant now = Instant.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE, "Mã giảm giá chưa có hiệu lực");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE, "Mã giảm giá đã hết hạn");
        }
        if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
            throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE,
                    "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã");
        }
        if (coupon.getUsageLimitPerUser() != null) {
            long usedByUser = orderRepository.countByUser_IdAndCoupon_Id(user.getId(), coupon.getId());
            if (usedByUser >= coupon.getUsageLimitPerUser()) {
                throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE,
                        "Bạn đã dùng hết lượt cho mã giảm giá này");
            }
        }

        long discount = computeDiscount(coupon, orderAmount);

        // Atomic claim: increments only while under the global limit; 0 rows = just ran out.
        if (couponRepository.incrementUsedCount(coupon.getId()) == 0) {
            throw new AppException(ErrorCode.COUPON_NOT_APPLICABLE, "Mã giảm giá đã hết lượt sử dụng");
        }

        order.setCoupon(coupon);
        order.setCouponCode(code);
        order.setDiscountAmount(discount);
        return discount;
    }

    /** Discount (VND) applied to the order subtotal, clamped to [0, orderAmount]. */
    private long computeDiscount(CouponEntity coupon, long orderAmount) {
        long discount = switch (coupon.getDiscountType()) {
            case PERCENT -> {
                long raw = orderAmount * coupon.getDiscountValue() / 100;
                yield coupon.getMaxDiscountAmount() != null
                        ? Math.min(raw, coupon.getMaxDiscountAmount())
                        : raw;
            }
            case FIXED -> coupon.getDiscountValue();
            case FREE_SHIP -> 0L; // shipping handled elsewhere
        };
        return Math.clamp(discount, 0L, orderAmount);
    }

    private void deductFromCart(Long userId, List<OrderItemRequest> lines) {
        for (OrderItemRequest line : lines) {
            cartItemRepository.findByUser_IdAndProduct_Id(userId, line.getProductId())
                    .ifPresent(cartItem -> {
                        if (cartItem.getQuantity() <= line.getQuantity()) {
                            cartItemRepository.delete(cartItem);
                        } else {
                            cartItem.setQuantity(cartItem.getQuantity() - line.getQuantity());
                            cartItemRepository.save(cartItem);
                        }
                    });
        }
    }

    private String generateOrderCode() {
        return "OD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
