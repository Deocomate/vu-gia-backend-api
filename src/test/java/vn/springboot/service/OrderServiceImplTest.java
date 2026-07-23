package vn.springboot.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.order.OrderAdminSearchRequest;
import vn.springboot.dto.request.order.OrderItemRequest;
import vn.springboot.dto.request.order.OrderPlaceRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.order.OrderResponse;
import vn.springboot.dto.response.order.PaymentInfoResponse;
import vn.springboot.entity.enums.PaymentMethod;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.order.OrderEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.order.OrderItemEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.dto.request.order.OrderStatusUpdateRequest;
import vn.springboot.mapper.OrderItemMapper;
import vn.springboot.mapper.OrderMapper;
import vn.springboot.entity.shipping.ShippingMethodEntity;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.ShippingMethodRepository;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.service.PaymentQrService;
import vn.springboot.service.impl.OrderCreationService;
import vn.springboot.service.impl.OrderServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private OrderCreationService orderCreationService;
    @Mock private PaymentQrService paymentQrService;
    @Mock private ShippingMethodRepository shippingMethodRepository;

    @InjectMocks private OrderServiceImpl service;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = user(1L, Role.CUSTOMER);
        authenticate(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserEntity user(Long id, Role role) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private void authenticate(UserEntity u) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(u), null, List.of()));
    }

    private OrderEntity order(Long id, UserEntity owner) {
        OrderEntity o = OrderEntity.builder().user(owner).orderCode("OD1").idempotencyKey("key-1").build();
        o.setId(id);
        return o;
    }

    private OrderPlaceRequest placeRequest() {
        return OrderPlaceRequest.builder()
                .idempotencyKey("key-1")
                .items(List.of(OrderItemRequest.builder().productId(10L).quantity(1).build()))
                .receiverName("Alice").receiverPhone("0900").receiverAddress("HN")
                .build();
    }

    @Test
    void placeOrder_idempotentReplay_returnsExisting_withoutCreating() {
        OrderEntity existing = order(5L, user);
        when(orderRepository.findByUser_IdAndIdempotencyKey(1L, "key-1")).thenReturn(Optional.of(existing));
        when(orderMapper.toResponse(existing)).thenReturn(OrderResponse.builder().id(5L).build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(5L)).thenReturn(List.of());

        OrderResponse response = service.placeOrder(placeRequest());

        assertThat(response.getId()).isEqualTo(5L);
        verify(orderCreationService, never()).create(any(), any());
    }

    @Test
    void placeOrder_new_createsAndMaps() {
        OrderEntity created = order(9L, user);
        when(orderRepository.findByUser_IdAndIdempotencyKey(1L, "key-1")).thenReturn(Optional.empty());
        when(orderCreationService.create(any(), any())).thenReturn(created);
        when(orderMapper.toResponse(created)).thenReturn(OrderResponse.builder().id(9L).build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(9L)).thenReturn(List.of());

        OrderResponse response = service.placeOrder(placeRequest());

        assertThat(response.getId()).isEqualTo(9L);
        verify(orderCreationService).create(any(), any());
    }

    @Test
    void placeOrder_onl_attachesQrPayment() {
        OrderEntity created = OrderEntity.builder()
                .user(user).orderCode("ODONL").idempotencyKey("key-1")
                .paymentMethod(PaymentMethod.ONL).totalAmount(500_000L).build();
        created.setId(9L);
        when(orderRepository.findByUser_IdAndIdempotencyKey(1L, "key-1")).thenReturn(Optional.empty());
        when(orderCreationService.create(any(), any())).thenReturn(created);
        when(orderMapper.toResponse(created)).thenReturn(OrderResponse.builder().id(9L).orderCode("ODONL").build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(9L)).thenReturn(List.of());
        when(paymentQrService.buildQr("ODONL", 500_000L))
                .thenReturn(PaymentInfoResponse.builder().qrImageUrl("https://qr").amount(500_000L).build());

        OrderResponse response = service.placeOrder(placeRequest());

        assertThat(response.getPayment()).isNotNull();
        assertThat(response.getPayment().getQrImageUrl()).isEqualTo("https://qr");
        verify(paymentQrService).buildQr("ODONL", 500_000L);
    }

    @Test
    void placeOrder_cod_hasNoQrPayment() {
        OrderEntity created = order(9L, user); // COD by default
        when(orderRepository.findByUser_IdAndIdempotencyKey(1L, "key-1")).thenReturn(Optional.empty());
        when(orderCreationService.create(any(), any())).thenReturn(created);
        when(orderMapper.toResponse(created)).thenReturn(OrderResponse.builder().id(9L).build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(9L)).thenReturn(List.of());

        OrderResponse response = service.placeOrder(placeRequest());

        assertThat(response.getPayment()).isNull();
        verify(paymentQrService, never()).buildQr(any(), anyLong());
    }

    @Test
    void searchOrders_returnsPage_acrossAllUsers() {
        OrderEntity o = order(9L, user(2L, Role.CUSTOMER));
        PageImpl<OrderEntity> page = new PageImpl<>(List.of(o), PageRequest.of(0, 10), 1);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(orderMapper.toResponse(o)).thenReturn(OrderResponse.builder().id(9L).build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(9L)).thenReturn(List.of());

        PageResponse<OrderResponse> result =
                service.searchOrders(OrderAdminSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(9L);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getById_notOwnerAndNotStaff_throwsNotFound() {
        OrderEntity foreign = order(9L, user(2L, Role.CUSTOMER));
        when(orderRepository.findById(9L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.getById(9L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    private OrderEntity orderWithStatus(Long id, OrderStatus status) {
        OrderEntity o = OrderEntity.builder().user(user).orderCode("OD" + id).status(status).build();
        o.setId(id);
        return o;
    }

    private OrderItemEntity item(long productId, int qty) {
        ProductEntity p = new ProductEntity();
        p.setId(productId);
        return OrderItemEntity.builder().product(p).quantity(qty).build();
    }

    @Test
    void updateStatus_reachingCompleted_incrementsSoldCount() {
        OrderEntity order = orderWithStatus(1L, OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(1L)).thenReturn(List.of(item(10L, 2)));
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().id(1L).build());

        service.updateStatus(1L, OrderStatusUpdateRequest.builder().status(OrderStatus.COMPLETED).build());

        verify(productRepository).incrementSoldCount(10L, 2);
    }

    @Test
    void updateStatus_leavingCompleted_rollsBackSoldCount() {
        OrderEntity order = orderWithStatus(1L, OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(1L)).thenReturn(List.of(item(10L, 2)));
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().id(1L).build());

        service.updateStatus(1L, OrderStatusUpdateRequest.builder().status(OrderStatus.RETURNED).build());

        verify(productRepository).incrementSoldCount(10L, -2);
    }

    @Test
    void updateStatus_withoutCrossingCompleted_leavesSoldCountUntouched() {
        OrderEntity order = orderWithStatus(1L, OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(1L)).thenReturn(List.of());
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().id(1L).build());

        service.updateStatus(1L, OrderStatusUpdateRequest.builder().status(OrderStatus.SHIPPING).build());

        verify(productRepository, never()).incrementSoldCount(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void getById_adminCanViewAnyOrder() {
        SecurityContextHolder.clearContext();
        authenticate(user(1L, Role.ADMIN));
        OrderEntity foreign = order(9L, user(2L, Role.CUSTOMER));
        when(orderRepository.findById(9L)).thenReturn(Optional.of(foreign));
        when(orderMapper.toResponse(foreign)).thenReturn(OrderResponse.builder().id(9L).build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(9L)).thenReturn(List.of());

        OrderResponse response = service.getById(9L);

        assertThat(response.getId()).isEqualTo(9L);
    }

    /**
     * Regression test for the live-integration bug: the idempotency-retry path re-reads
     * the order outside any transaction, so {@code order.getShippingMethod()} may be an
     * uninitialized Hibernate proxy — touching anything but its id (e.g. {@code .getName()})
     * throws {@code LazyInitializationException}. `buildResponse` must resolve the name via
     * a fresh {@code shippingMethodRepository.findById}, never via the proxy directly. This
     * mock's {@code getName()} throws to prove the fix never calls it.
     */
    @Test
    void placeOrder_withShippingMethod_resolvesNameViaFreshLookup_notLazyProxy() {
        ShippingMethodEntity proxy = mock(ShippingMethodEntity.class);
        when(proxy.getId()).thenReturn(2L);
        // Never expected to be called (proves the fix resolves the name via a fresh
        // repository lookup, not this proxy) — lenient so an unused stub isn't itself
        // flagged as a test failure by Mockito's strict-stubs mode.
        org.mockito.Mockito.lenient().when(proxy.getName())
                .thenThrow(new org.hibernate.LazyInitializationException("no session"));

        OrderEntity created = OrderEntity.builder()
                .user(user).orderCode("OD1").idempotencyKey("key-1")
                .shippingMethod(proxy).shippingFee(30_000L).build();
        created.setId(9L);

        when(orderRepository.findByUser_IdAndIdempotencyKey(1L, "key-1")).thenReturn(Optional.empty());
        when(orderCreationService.create(any(), any())).thenReturn(created);
        when(orderMapper.toResponse(created)).thenReturn(OrderResponse.builder().id(9L).build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(9L)).thenReturn(List.of());
        ShippingMethodEntity resolved = ShippingMethodEntity.builder().name("Hoa toc").build();
        resolved.setId(2L);
        when(shippingMethodRepository.findById(2L)).thenReturn(Optional.of(resolved));

        OrderResponse response = service.placeOrder(placeRequest());

        assertThat(response.getShippingMethodName()).isEqualTo("Hoa toc");
        verify(shippingMethodRepository).findById(2L);
    }

    @Test
    void searchOrders_batchLoadsShippingMethodNames_oneQueryNotPerRow() {
        ShippingMethodEntity sm1 = mock(ShippingMethodEntity.class);
        when(sm1.getId()).thenReturn(2L);
        ShippingMethodEntity sm2 = mock(ShippingMethodEntity.class);
        when(sm2.getId()).thenReturn(3L);

        OrderEntity o1 = OrderEntity.builder().user(user).orderCode("OD1").shippingMethod(sm1).build();
        o1.setId(1L);
        OrderEntity o2 = OrderEntity.builder().user(user).orderCode("OD2").shippingMethod(sm2).build();
        o2.setId(2L);

        PageImpl<OrderEntity> page = new PageImpl<>(List.of(o1, o2), PageRequest.of(0, 10), 2);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(orderMapper.toResponse(o1)).thenReturn(OrderResponse.builder().id(1L).build());
        when(orderMapper.toResponse(o2)).thenReturn(OrderResponse.builder().id(2L).build());
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(any())).thenReturn(List.of());
        ShippingMethodEntity nhanh = ShippingMethodEntity.builder().name("Nhanh").build();
        nhanh.setId(2L);
        ShippingMethodEntity hoaToc = ShippingMethodEntity.builder().name("Hoa toc").build();
        hoaToc.setId(3L);
        when(shippingMethodRepository.findAllById(any())).thenReturn(List.of(nhanh, hoaToc));

        PageResponse<OrderResponse> result =
                service.searchOrders(OrderAdminSearchRequest.builder().build());

        assertThat(result.getContent()).extracting(OrderResponse::getShippingMethodName)
                .containsExactly("Nhanh", "Hoa toc");
        verify(shippingMethodRepository).findAllById(any());
        verify(shippingMethodRepository, never()).findById(any());
    }
}
