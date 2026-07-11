package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.product.ProductCategorySearchRequest;
import vn.springboot.entity.product.ProductCategoryEntity;

public class ProductCategorySpecification {

    public static Specification<ProductCategoryEntity> build(ProductCategorySearchRequest request) {
        return Specification.allOf(
                like("name", request.getName()),
                like("slug", request.getSlug()),
                isActive(request.getIsActive()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<ProductCategoryEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<ProductCategoryEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
