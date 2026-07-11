package vn.springboot.dto.response.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.DiscountType;

/**
 * Result of applying a coupon code to a cart. Always returned with HTTP 200; the
 * {@code valid} flag and {@code message} let the front-end show inline feedback.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResponse {

    private boolean valid;

    private String code;

    private DiscountType discountType;

    /** Amount (VND) subtracted from the order subtotal; 0 for FREE_SHIP / invalid. */
    private long discountAmount;

    /** True when the coupon grants free shipping (discountType = FREE_SHIP). */
    private boolean freeShipping;

    private String message;
}
