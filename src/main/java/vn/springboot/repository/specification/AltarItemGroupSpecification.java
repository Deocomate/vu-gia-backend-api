package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.altar.AltarItemGroupSearchRequest;
import vn.springboot.entity.altar.AltarItemGroupEntity;

public class AltarItemGroupSpecification {

    public static Specification<AltarItemGroupEntity> build(AltarItemGroupSearchRequest request) {
        return Specification.allOf(
                like("name", request.getName()),
                isActive(request.getIsActive()),
                renderOnAltar(request.getRenderOnAltar()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<AltarItemGroupEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<AltarItemGroupEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    private static Specification<AltarItemGroupEntity> renderOnAltar(Boolean renderOnAltar) {
        return (root, query, cb) -> {
            if (renderOnAltar == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("renderOnAltar"), renderOnAltar);
        };
    }
}
