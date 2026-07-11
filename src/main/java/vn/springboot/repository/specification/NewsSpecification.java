package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.news.NewsSearchRequest;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.news.NewsEntity;

public class NewsSpecification {

    public static Specification<NewsEntity> build(NewsSearchRequest request) {
        return Specification.allOf(
                like("title", request.getTitle()),
                hasStatus(request.getStatus()),
                hasCategory(request.getNewsCategoryId()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<NewsEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<NewsEntity> hasStatus(ContentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<NewsEntity> hasCategory(Long newsCategoryId) {
        return (root, query, cb) -> {
            if (newsCategoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("newsCategory").get("id"), newsCategoryId);
        };
    }
}
