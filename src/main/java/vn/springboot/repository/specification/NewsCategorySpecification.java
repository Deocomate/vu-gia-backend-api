package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.news.NewsCategorySearchRequest;
import vn.springboot.entity.news.NewsCategoryEntity;

public class NewsCategorySpecification {

    public static Specification<NewsCategoryEntity> build(NewsCategorySearchRequest request) {
        return Specification.allOf(
                like("name", request.getName()),
                like("slug", request.getSlug()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<NewsCategoryEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }
}
