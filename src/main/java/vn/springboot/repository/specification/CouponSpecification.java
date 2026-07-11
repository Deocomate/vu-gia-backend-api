package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.coupon.CouponSearchRequest;
import vn.springboot.entity.coupon.CouponEntity;
import vn.springboot.entity.enums.DiscountType;

public class CouponSpecification {

    public static Specification<CouponEntity> build(CouponSearchRequest request) {
        return Specification.allOf(
                hasCode(request.getCode()),
                hasDiscountType(request.getDiscountType()),
                hasActive(request.getIsActive()));
    }

    private static Specification<CouponEntity> hasCode(String code) {
        return (root, query, cb) -> {
            if (code == null || code.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%");
        };
    }

    private static Specification<CouponEntity> hasDiscountType(DiscountType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("discountType"), type);
    }

    private static Specification<CouponEntity> hasActive(Boolean isActive) {
        return (root, query, cb) -> isActive == null ? cb.conjunction() : cb.equal(root.get("isActive"), isActive);
    }
}
