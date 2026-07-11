package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.redirect.RedirectSearchRequest;
import vn.springboot.entity.redirect.RedirectEntity;

public class RedirectSpecification {

    public static Specification<RedirectEntity> build(RedirectSearchRequest request) {
        return Specification.allOf(
                like("fromPath", request.getFromPath()),
                like("toPath", request.getToPath()),
                isActive(request.getIsActive()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<RedirectEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<RedirectEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
