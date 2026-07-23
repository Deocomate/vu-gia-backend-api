package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.order.OrderAdminSearchRequest;
import vn.springboot.dto.request.order.OrderPlaceRequest;
import vn.springboot.dto.request.order.OrderSearchRequest;
import vn.springboot.dto.request.order.OrderStatusUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.order.OrderItemResponse;
import vn.springboot.dto.response.order.OrderResponse;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentMethod;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.order.OrderEntity;
import vn.springboot.entity.shipping.ShippingMethodEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.mapper.OrderItemMapper;
import vn.springboot.mapper.OrderMapper;
import vn.springboot.repository.CouponRepository;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.ShippingMethodRepository;
import vn.springboot.repository.specification.OrderSpecification;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.service.OrderService;
import vn.springboot.service.PaymentQrService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "orderCode", "totalAmount", "status", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<OrderStatus> CANCELLABLE_STATUSES =
            Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PROCESSING);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderCreationService orderCreationService;
    private final PaymentQrService paymentQrService;
    private final ShippingMethodRepository shippingMethodRepository;
    private final CouponRepository couponRepository;

    /**
     * Not {@code @Transactional}: the actual write happens in
     * {@link OrderCreationService#create}. Keeping the catch outside that
     * transaction lets us return the winning order after a losing insert rolls back.
     */
    @Override
    public OrderResponse placeOrder(OrderPlaceRequest request) {
        UserEntity user = currentUser();

        // Fast path: a retry with the same key returns the original order — no new
        // order, no coupon claim, no cart change, no second email.
        var existing = orderRepository.findByUser_IdAndIdempotencyKey(user.getId(), request.getIdempotencyKey());
        if (existing.isPresent()) {
            return buildResponse(existing.get());
        }

        try {
            OrderEntity order = orderCreationService.create(user, request);
            return buildResponse(order);
        } catch (DataIntegrityViolationException race) {
            // Concurrent duplicate lost the unique-index race — return the winner.
            OrderEntity winner = orderRepository
                    .findByUser_IdAndIdempotencyKey(user.getId(), request.getIdempotencyKey())
                    .orElseThrow(() -> race);
            return buildResponse(winner);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(OrderSearchRequest request) {
        UserEntity user = currentUser();
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request.getSortBy(), request.getSortDirection()));
        Specification<OrderEntity> specification =
                OrderSpecification.build(request).and(OrderSpecification.ownedBy(user.getId()));

        Page<OrderEntity> page = orderRepository.findAll(specification, pageable);

        List<OrderResponse> content = buildResponseList(page.getContent());

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> searchOrders(OrderAdminSearchRequest request) {
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request.getSortBy(), request.getSortDirection()));

        Page<OrderEntity> page = orderRepository.findAll(OrderSpecification.buildAdmin(request), pageable);

        List<OrderResponse> content = buildResponseList(page.getContent());

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        UserEntity user = currentUser();
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Owner or staff only; otherwise hide existence with a 404.
        if (!order.getUser().getId().equals(user.getId()) && !isStaff(user)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        return buildResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus previous = order.getStatus();
        OrderStatus next = request.getStatus();
        order.setStatus(next);
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        orderRepository.save(order);

        applySoldCountOnStatusChange(order, previous, next);

        return buildResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long id) {
        UserEntity user = currentUser();
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Owner or staff only; otherwise hide existence with a 404 (same as getById).
        if (!order.getUser().getId().equals(user.getId()) && !isStaff(user)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_CANCELLABLE);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // No stock/inventory model exists in this codebase — nothing to restock.
        if (order.getCoupon() != null) {
            couponRepository.decrementUsedCount(order.getCoupon().getId());
        }

        return buildResponse(order);
    }

    /**
     * Keeps {@code product.soldCount} in sync with the COMPLETED boundary: bump it
     * when an order first reaches COMPLETED, and roll it back if the order later
     * leaves COMPLETED (e.g. RETURNED / refunded). Crossing the boundary only once
     * per direction avoids double counting.
     */
    private void applySoldCountOnStatusChange(OrderEntity order, OrderStatus previous, OrderStatus next) {
        boolean wasCompleted = previous == OrderStatus.COMPLETED;
        boolean isCompleted = next == OrderStatus.COMPLETED;
        if (wasCompleted == isCompleted) {
            return; // no crossing of the COMPLETED boundary
        }
        int sign = isCompleted ? 1 : -1;
        orderItemRepository.findByOrder_IdOrderByIdAsc(order.getId()).forEach(item ->
                productRepository.incrementSoldCount(item.getProduct().getId(), sign * item.getQuantity()));
    }

    private OrderResponse buildResponse(OrderEntity order) {
        return buildResponse(order, null);
    }

    /**
     * Builds every row's response with ONE batched {@code shippingMethodRepository.findAllById}
     * instead of one lookup per row (avoids an N+1 on top of the per-order {@code buildResponse}
     * already paying for items separately).
     */
    private List<OrderResponse> buildResponseList(List<OrderEntity> orders) {
        Map<Long, String> shippingMethodNames = shippingMethodRepository
                .findAllById(orders.stream()
                        .map(OrderEntity::getShippingMethod)
                        .filter(Objects::nonNull)
                        .map(ShippingMethodEntity::getId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(ShippingMethodEntity::getId, ShippingMethodEntity::getName));

        return orders.stream().map(order -> buildResponse(order, shippingMethodNames)).toList();
    }

    /**
     * @param shippingMethodNames pre-resolved {@code id -> name} map for batched list callers
     *                            ({@link #buildResponseList}); {@code null} means "resolve it here"
     *                            (single-order callers: place/get/updateStatus).
     */
    private OrderResponse buildResponse(OrderEntity order, Map<Long, String> shippingMethodNames) {
        OrderResponse response = orderMapper.toResponse(order);
        List<OrderItemResponse> items = orderItemRepository.findByOrder_IdOrderByIdAsc(order.getId()).stream()
                .map(orderItemMapper::toResponse)
                .toList();
        response.setItems(items);

        // Online orders that aren't paid yet carry a VietQR so the FE can render it —
        // but never for a cancelled/returned order (BE-3 lets the customer self-cancel
        // a PENDING_PAYMENT order; a still-live "scan to pay" QR on a cancelled order
        // would let a stray transfer mark it PAID via the webhook with no reconciliation
        // path — see the matching guard in PaymentWebhookServiceImpl.handleSepay).
        boolean cancelledOrReturned = order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED;
        if (order.getPaymentMethod() == PaymentMethod.ONL
                && order.getPaymentStatus() != PaymentStatus.PAID
                && !cancelledOrReturned) {
            response.setPayment(paymentQrService.buildQr(order.getOrderCode(), order.getTotalAmount()));
        }

        // Resolved via a fresh lookup by id (not `order.getShippingMethod().getName()`):
        // a proxy's id is always safe to read, but the idempotency-retry path in
        // placeOrder() re-reads the order outside any transaction, so touching any
        // other lazy field on that proxy would throw LazyInitializationException.
        if (order.getShippingMethod() != null) {
            Long shippingMethodId = order.getShippingMethod().getId();
            if (shippingMethodNames != null) {
                response.setShippingMethodName(shippingMethodNames.get(shippingMethodId));
            } else {
                shippingMethodRepository.findById(shippingMethodId)
                        .ifPresent(method -> response.setShippingMethodName(method.getName()));
            }
        }
        return response;
    }

    private boolean isStaff(UserEntity user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.SUPERADMIN;
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal.getUser();
    }

    private Sort resolveSort(String sortBy, String sortDirection) {
        String field = SORTABLE_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
