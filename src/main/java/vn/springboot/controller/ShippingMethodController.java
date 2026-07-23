package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.shipping.ShippingMethodCreateRequest;
import vn.springboot.dto.request.shipping.ShippingMethodSearchRequest;
import vn.springboot.dto.request.shipping.ShippingMethodUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.shipping.ShippingMethodResponse;
import vn.springboot.service.ShippingMethodService;

/**
 * Shipping method endpoints. Reads are public (storefront lists active methods
 * at checkout); writes require staff roles ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/shipping-methods")
@RequiredArgsConstructor
public class ShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    @GetMapping
    public ApiResponse<PageResponse<ShippingMethodResponse>> search(
            @ModelAttribute ShippingMethodSearchRequest request) {
        return ApiResponse.success(shippingMethodService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShippingMethodResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(shippingMethodService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ShippingMethodResponse> create(
            @Valid @RequestBody ShippingMethodCreateRequest request) {
        return ApiResponse.success("Created successfully", shippingMethodService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ShippingMethodResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ShippingMethodUpdateRequest request) {
        return ApiResponse.success("Updated successfully", shippingMethodService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        shippingMethodService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
