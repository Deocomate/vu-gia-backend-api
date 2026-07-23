package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.order.OrderAdminSearchRequest;
import vn.springboot.dto.request.order.OrderPlaceRequest;
import vn.springboot.dto.request.order.OrderSearchRequest;
import vn.springboot.dto.request.order.OrderStatusUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.order.OrderResponse;
import vn.springboot.service.OrderService;

/**
 * Order endpoints. Placing and viewing operate on the authenticated user's own
 * orders; changing status is staff-only ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> place(@Valid @RequestBody OrderPlaceRequest request) {
        return ApiResponse.success("Order placed", orderService.placeOrder(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> myOrders(
            @ModelAttribute OrderSearchRequest request) {
        return ApiResponse.success(orderService.getMyOrders(request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<PageResponse<OrderResponse>> searchAll(
            @ModelAttribute OrderAdminSearchRequest request) {
        return ApiResponse.success(orderService.searchOrders(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(orderService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ApiResponse.success("Status updated", orderService.updateStatus(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success("Order cancelled", orderService.cancel(id));
    }
}
