package vn.springboot.dto.request.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    @StorageUrl
    private String thumb;

    /** Optional stock keeping unit; must be unique when provided. */
    @Size(max = 100)
    private String sku;

    private ProductType type;

    @Positive
    private Long price;

    private Long compareAtPrice;

    private Boolean isFeatured;

    private ProductStatus status;

    /** JSON string. */
    private String description;

    /** JSON string. */
    private String comboProducts;

    /** Optional; ignored when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;

    private Long productCategoryId;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    @StorageUrl
    private String seoImage;
}
