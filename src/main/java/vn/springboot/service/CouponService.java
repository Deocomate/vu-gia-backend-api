package vn.springboot.service;

import vn.springboot.dto.request.coupon.CouponCreateRequest;
import vn.springboot.dto.request.coupon.CouponSearchRequest;
import vn.springboot.dto.request.coupon.CouponUpdateRequest;
import vn.springboot.dto.request.coupon.CouponValidateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.coupon.CouponResponse;
import vn.springboot.dto.response.coupon.CouponValidationResponse;

public interface CouponService {

    PageResponse<CouponResponse> search(CouponSearchRequest request);

    CouponResponse getById(Long id);

    CouponResponse create(CouponCreateRequest request);

    CouponResponse update(Long id, CouponUpdateRequest request);

    void delete(Long id);

    /** Public cart preview: check a code against an order amount and compute the discount. */
    CouponValidationResponse validate(CouponValidateRequest request);
}
