package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.showroom.ShowroomSearchRequest;
import vn.springboot.entity.showroom.ShowroomEntity;

public class ShowroomSpecification {

    public static Specification<ShowroomEntity> build(ShowroomSearchRequest request) {
        return Specification.allOf(
                like("name", request.getName()),
                isActive(request.getIsActive()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<ShowroomEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<ShowroomEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
