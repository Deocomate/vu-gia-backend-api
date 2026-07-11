package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.product.ProductSearchRequest;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;
import vn.springboot.entity.product.ProductEntity;

public class ProductSpecification {

    public static Specification<ProductEntity> build(ProductSearchRequest request) {
        return Specification.allOf(
                like("name", request.getName()),
                like("sku", request.getSku()),
                hasType(request.getType()),
                hasStatus(request.getStatus()),
                hasCategory(request.getProductCategoryId()),
                isFeatured(request.getIsFeatured()),
                priceGte(request.getMinPrice()),
                priceLte(request.getMaxPrice()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<ProductEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<ProductEntity> hasType(ProductType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), type);
        };
    }

    private static Specification<ProductEntity> hasStatus(ProductStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<ProductEntity> hasCategory(Long productCategoryId) {
        return (root, query, cb) -> {
            if (productCategoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("productCategory").get("id"), productCategoryId);
        };
    }

    private static Specification<ProductEntity> isFeatured(Boolean isFeatured) {
        return (root, query, cb) -> {
            if (isFeatured == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isFeatured"), isFeatured);
        };
    }

    private static Specification<ProductEntity> priceGte(Long minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    private static Specification<ProductEntity> priceLte(Long maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
}
