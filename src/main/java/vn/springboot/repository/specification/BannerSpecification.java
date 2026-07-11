package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.banner.BannerSearchRequest;
import vn.springboot.entity.banner.BannerEntity;
import vn.springboot.entity.enums.BannerPosition;

public class BannerSpecification {

    public static Specification<BannerEntity> build(BannerSearchRequest request) {
        return Specification.allOf(
                like("title", request.getTitle()),
                position(request.getPosition()),
                isActive(request.getIsActive()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<BannerEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<BannerEntity> position(BannerPosition position) {
        return (root, query, cb) -> {
            if (position == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("position"), position);
        };
    }

    private static Specification<BannerEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
