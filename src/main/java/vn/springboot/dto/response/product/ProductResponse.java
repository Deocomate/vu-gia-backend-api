package vn.springboot.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;

    private String name;

    @StorageUrl
    private String thumb;

    private String sku;

    private ProductType type;

    private Long price;

    private Long compareAtPrice;

    private int soldCount;

    private boolean isFeatured;

    private ProductStatus status;

    /** JSON string. */
    private String description;

    /** JSON string. */
    private String comboProducts;

    private String slug;

    private Integer priority;

    private ProductCategoryBriefResponse category;

    /** Only populated on detail views; {@code null} in list responses. */
    private List<ProductImageResponse> images;

    private String seoTitle;

    private String seoDescription;

    @StorageUrl
    private String seoImage;

    private Instant createdAt;

    private Instant updatedAt;
}
