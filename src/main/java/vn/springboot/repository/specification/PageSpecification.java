package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.page.PageSearchRequest;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.page.PageEntity;

public class PageSpecification {

    public static Specification<PageEntity> build(PageSearchRequest request) {
        return Specification.allOf(
                like("key", request.getKey()),
                like("title", request.getTitle()),
                status(request.getStatus()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<PageEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<PageEntity> status(ContentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }
}
