package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.gallery.GalleryImageSearchRequest;
import vn.springboot.entity.gallery.GalleryImageEntity;

public class GalleryImageSpecification {

    public static Specification<GalleryImageEntity> build(GalleryImageSearchRequest request) {
        return Specification.allOf(
                like("title", request.getTitle()),
                like("category", request.getCategory()),
                isActive(request.getIsActive()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<GalleryImageEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<GalleryImageEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
