package vn.springboot.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchRequest {

    private String name;

    private String sku;

    private ProductType type;

    private ProductStatus status;

    private Long productCategoryId;

    private Boolean isFeatured;

    private Long minPrice;

    private Long maxPrice;

    /** 1-based page number (1 = first page). */
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "ASC";
}
