package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.order.OrderItemRequest;
import vn.springboot.dto.request.order.OrderPlaceRequest;
import vn.springboot.entity.cart.CartItemEntity;
import vn.springboot.entity.coupon.CouponEntity;
import vn.springboot.entity.enums.DiscountType;
import vn.springboot.entity.enums.PaymentMethod;
import vn.springboot.entity.enums.ProductType;
import vn.springboot.entity.order.OrderEntity;
import vn.springboot.entity.order.OrderItemEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.shipping.ShippingMethodEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.event.OrderPlacedEvent;
import vn.springboot.repository.CartItemRepository;
import vn.springboot.repository.CouponRepository;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.ShippingMethodRepository;
import vn.springboot.service.impl.OrderCreationService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCreationServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ShippingMethodRepository shippingMethodRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private OrderCreationService service;

    private UserEntity user(Long id) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setEmail("buyer@example.com");
        return u;
    }

    private ProductEntity product(Long id, long price, ProductType type) {
        ProductEntity p = new ProductEntity();
        p.setId(id);
        p.setName("Product " + id);
        p.setPrice(price);
        p.setType(type);
        return p;
    }

    private OrderPlaceRequest request(String couponCode, OrderItemRequest... items) {
        return OrderPlaceRequest.builder()
                .idempotencyKey("key-1")
                .items(List.of(items))
                .couponCode(couponCode)
                .receiverName("Alice")
                .receiverPhone("0900")
                .receiverAddress("HN")
                .build();
    }

    private OrderItemRequest line(long productId, int qty) {
        return OrderItemRequest.builder().productId(productId).quantity(qty).build();
    }

    @Test
    void create_snapshotsItems_andComputesTotal_noCoupon() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 100, ProductType.SINGLE)));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.empty());

        OrderEntity order = service.create(u, request(null, line(10L, 3)));

        assertThat(order.getTotalAmount()).isEqualTo(300);
        assertThat(order.getDiscountAmount()).isEqualTo(0);
        assertThat(order.getOrderCode()).startsWith("OD");

        ArgumentCaptor<List<OrderItemEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getSubtotal()).isEqualTo(300);
        assertThat(captor.getValue().get(0).getProductName()).isEqualTo("Product 10");
        // sold_count is bumped at COMPLETED, not at placement.
        verify(productRepository, never()).incrementSoldCount(any(), org.mockito.ArgumentMatchers.anyInt());
        verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void create_withValidPercentCoupon_appliesDiscount_andClaimsUsage() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 1000, ProductType.SINGLE)));
        CouponEntity coupon = CouponEntity.builder()
                .code("SALE10").discountType(DiscountType.PERCENT).discountValue(10L)
                .usageLimitPerUser(5).isActive(true).build();
        coupon.setId(7L);
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(coupon));
        when(orderRepository.countByUser_IdAndCoupon_Id(1L, 7L)).thenReturn(0L);
        when(couponRepository.incrementUsedCount(7L)).thenReturn(1);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.empty());

        OrderEntity order = service.create(u, request("sale10", line(10L, 2))); // 2000 - 10%

        assertThat(order.getDiscountAmount()).isEqualTo(200);
        assertThat(order.getTotalAmount()).isEqualTo(1800);
        assertThat(order.getCouponCode()).isEqualTo("SALE10");
        verify(couponRepository).incrementUsedCount(7L);
    }

    @Test
    void create_couponExpired_throwsNotApplicable() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 1000, ProductType.SINGLE)));
        CouponEntity coupon = CouponEntity.builder()
                .code("OLD").discountType(DiscountType.FIXED).discountValue(50L).isActive(true)
                .endsAt(Instant.parse("2000-01-01T00:00:00Z")).build();
        coupon.setId(7L);
        when(couponRepository.findByCode("OLD")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> service.create(u, request("OLD", line(10L, 1))))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPON_NOT_APPLICABLE);
        verify(couponRepository, never()).incrementUsedCount(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_couponUsageLimitReached_atomicClaimFails_throws() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 1000, ProductType.SINGLE)));
        CouponEntity coupon = CouponEntity.builder()
                .code("LIMITED").discountType(DiscountType.FIXED).discountValue(50L)
                .usageLimitPerUser(5).isActive(true).build();
        coupon.setId(7L);
        when(couponRepository.findByCode("LIMITED")).thenReturn(Optional.of(coupon));
        when(orderRepository.countByUser_IdAndCoupon_Id(1L, 7L)).thenReturn(0L);
        when(couponRepository.incrementUsedCount(7L)).thenReturn(0); // just ran out

        assertThatThrownBy(() -> service.create(u, request("LIMITED", line(10L, 1))))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPON_NOT_APPLICABLE);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_invalidProduct_throwsProductNotFound() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any())).thenReturn(List.of()); // id 99 unresolved

        assertThatThrownBy(() -> service.create(u, request(null, line(99L, 1))))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void create_onl_doesNotPublishEmailEvent() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 100, ProductType.SINGLE)));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.empty());

        OrderPlaceRequest req = OrderPlaceRequest.builder()
                .idempotencyKey("key-1").items(List.of(line(10L, 1)))
                .receiverName("Alice").receiverPhone("0900").receiverAddress("HN")
                .paymentMethod(PaymentMethod.ONL).build();

        OrderEntity order = service.create(u, req);

        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.ONL);
        verify(eventPublisher, never()).publishEvent(any());  // email deferred until payment
    }

    @Test
    void create_deductsOrderedQuantityFromCart() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 100, ProductType.SINGLE)));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        CartItemEntity cartItem = CartItemEntity.builder().id(3L).user(u).quantity(5).build();
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.of(cartItem));

        service.create(u, request(null, line(10L, 2)));

        assertThat(cartItem.getQuantity()).isEqualTo(3); // 5 - 2
        verify(cartItemRepository).save(cartItem);
        verify(cartItemRepository, never()).delete(any());
    }

    private ShippingMethodEntity shippingMethod(Long id, long fee, boolean active) {
        ShippingMethodEntity e = ShippingMethodEntity.builder()
                .name("Standard").fee(fee).isActive(active).build();
        e.setId(id);
        return e;
    }

    @Test
    void create_withShippingMethod_foldsFeeIntoTotal() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 100, ProductType.SINGLE)));
        when(shippingMethodRepository.findById(5L)).thenReturn(Optional.of(shippingMethod(5L, 30_000, true)));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.empty());

        OrderPlaceRequest req = OrderPlaceRequest.builder()
                .idempotencyKey("key-1").items(List.of(line(10L, 3)))
                .receiverName("Alice").receiverPhone("0900").receiverAddress("HN")
                .shippingMethodId(5L).build();

        OrderEntity order = service.create(u, req);

        assertThat(order.getShippingFee()).isEqualTo(30_000L);
        assertThat(order.getShippingMethod()).isNotNull();
        assertThat(order.getTotalAmount()).isEqualTo(300 + 30_000); // orderAmount + shippingFee
    }

    @Test
    void create_shippingMethodNotFound_throws() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 100, ProductType.SINGLE)));
        when(shippingMethodRepository.findById(99L)).thenReturn(Optional.empty());

        OrderPlaceRequest req = OrderPlaceRequest.builder()
                .idempotencyKey("key-1").items(List.of(line(10L, 1)))
                .receiverName("Alice").receiverPhone("0900").receiverAddress("HN")
                .shippingMethodId(99L).build();

        assertThatThrownBy(() -> service.create(u, req))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SHIPPING_METHOD_NOT_FOUND);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_inactiveShippingMethod_throwsNotFound() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 100, ProductType.SINGLE)));
        when(shippingMethodRepository.findById(5L)).thenReturn(Optional.of(shippingMethod(5L, 30_000, false)));

        OrderPlaceRequest req = OrderPlaceRequest.builder()
                .idempotencyKey("key-1").items(List.of(line(10L, 1)))
                .receiverName("Alice").receiverPhone("0900").receiverAddress("HN")
                .shippingMethodId(5L).build();

        assertThatThrownBy(() -> service.create(u, req))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SHIPPING_METHOD_NOT_FOUND);
    }

    @Test
    void create_nullShippingMethodId_feeIsZero_backwardCompatible() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 100, ProductType.SINGLE)));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.empty());

        OrderEntity order = service.create(u, request(null, line(10L, 3)));

        assertThat(order.getShippingFee()).isEqualTo(0L);
        assertThat(order.getShippingMethod()).isNull();
        assertThat(order.getTotalAmount()).isEqualTo(300);
        verify(shippingMethodRepository, never()).findById(any());
    }

    @Test
    void create_freeShipCoupon_zeroesShippingFeeAddedToTotal_butKeepsSnapshot() {
        UserEntity u = user(1L);
        when(productRepository.findAllById(any()))
                .thenReturn(List.of(product(10L, 1000, ProductType.SINGLE)));
        when(shippingMethodRepository.findById(5L)).thenReturn(Optional.of(shippingMethod(5L, 30_000, true)));
        CouponEntity coupon = CouponEntity.builder()
                .code("FREESHIP").discountType(DiscountType.FREE_SHIP).discountValue(0L).isActive(true).build();
        coupon.setId(7L);
        when(couponRepository.findByCode("FREESHIP")).thenReturn(Optional.of(coupon));
        when(couponRepository.incrementUsedCount(7L)).thenReturn(1);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.empty());

        OrderPlaceRequest req = OrderPlaceRequest.builder()
                .idempotencyKey("key-1").items(List.of(line(10L, 2)))
                .receiverName("Alice").receiverPhone("0900").receiverAddress("HN")
                .couponCode("freeship").shippingMethodId(5L).build();

        OrderEntity order = service.create(u, req);

        // Real fee kept for display, but not added to the payable total.
        assertThat(order.getShippingFee()).isEqualTo(30_000L);
        assertThat(order.getTotalAmount()).isEqualTo(2000); // 2000 (orderAmount) - 0 (discount) + 0 (waived)
    }
}
