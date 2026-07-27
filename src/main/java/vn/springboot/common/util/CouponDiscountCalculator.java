package vn.springboot.common.util;

import vn.springboot.entity.coupon.CouponEntity;

/**
 * Pure function shared by {@code CouponServiceImpl} (validate preview) and
 * {@code OrderCreationService} (actual order placement) so the discount math
 * lives in exactly one place — a fix to rounding/clamping applied here
 * automatically applies to both call sites.
 */
public final class CouponDiscountCalculator {

    private CouponDiscountCalculator() {
    }

    /** Discount (VND) applied to the order subtotal, clamped to [0, orderAmount]. */
    public static long computeDiscount(CouponEntity coupon, long orderAmount) {
        long discount = switch (coupon.getDiscountType()) {
            case PERCENT -> {
                long raw = orderAmount * coupon.getDiscountValue() / 100;
                yield coupon.getMaxDiscountAmount() != null
                        ? Math.min(raw, coupon.getMaxDiscountAmount())
                        : raw;
            }
            case FIXED -> coupon.getDiscountValue();
            case FREE_SHIP -> 0L; // shipping fee is handled by the order/shipping layer
        };
        return Math.clamp(discount, 0L, orderAmount);
    }
}
