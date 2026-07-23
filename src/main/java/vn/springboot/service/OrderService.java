package vn.springboot.service;

import vn.springboot.dto.request.order.OrderAdminSearchRequest;
import vn.springboot.dto.request.order.OrderPlaceRequest;
import vn.springboot.dto.request.order.OrderSearchRequest;
import vn.springboot.dto.request.order.OrderStatusUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.order.OrderResponse;

public interface OrderService {

    /** Places an order for the current user (idempotent on {@code idempotencyKey}). */
    OrderResponse placeOrder(OrderPlaceRequest request);

    /** The current user's own orders, paginated. */
    PageResponse<OrderResponse> getMyOrders(OrderSearchRequest request);

    /** Admin: search orders across all users with full filters. */
    PageResponse<OrderResponse> searchOrders(OrderAdminSearchRequest request);

    /** Order detail — owner sees their own; admins see any. */
    OrderResponse getById(Long id);

    /** Admin: advance the order's fulfilment (and optionally payment) status. */
    OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request);

    /**
     * Owner (or staff): cancel an order still in {@code PENDING_PAYMENT}/{@code PROCESSING}.
     * Restores any applied coupon's usage count; no stock model exists to restock.
     */
    OrderResponse cancel(Long id);
}
