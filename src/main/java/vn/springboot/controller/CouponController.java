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
import vn.springboot.dto.request.coupon.CouponCreateRequest;
import vn.springboot.dto.request.coupon.CouponSearchRequest;
import vn.springboot.dto.request.coupon.CouponUpdateRequest;
import vn.springboot.dto.request.coupon.CouponValidateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.coupon.CouponResponse;
import vn.springboot.dto.response.coupon.CouponValidationResponse;
import vn.springboot.service.CouponService;

/**
 * Coupon administration. Listing/detail are staff-only (codes must not leak);
 * only {@code POST /validate} is public so the cart can preview a code.
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<PageResponse<CouponResponse>> search(@ModelAttribute CouponSearchRequest request) {
        return ApiResponse.success(couponService.search(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<CouponResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(couponService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<CouponResponse> create(@Valid @RequestBody CouponCreateRequest request) {
        return ApiResponse.success("Created successfully", couponService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<CouponResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody CouponUpdateRequest request) {
        return ApiResponse.success("Updated successfully", couponService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }

    /** Public: apply a coupon code to an order amount (cart preview). */
    @PostMapping("/validate")
    public ApiResponse<CouponValidationResponse> validate(@Valid @RequestBody CouponValidateRequest request) {
        return ApiResponse.success(couponService.validate(request));
    }
}
