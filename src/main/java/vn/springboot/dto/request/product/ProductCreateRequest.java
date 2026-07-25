package vn.springboot.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    /** Image URL of the product thumbnail. */
    @NotBlank
    @Size(max = 255)
    @StorageUrl
    private String thumb;

    /** Optional stock keeping unit; must be unique when provided. */
    @Size(max = 100)
    private String sku;

    @NotNull
    private ProductType type;

    @NotNull
    @Positive
    private Long price;

    private Long compareAtPrice;

    /** {@code null} → defaults to {@code false} on create. */
    private Boolean isFeatured;

    /** {@code null} → defaults to {@code DRAFT} on create. */
    private ProductStatus status;

    /** JSON string. */
    private String description;

    /** JSON string; chỉ dùng khi type=SINGLE: [{title, description, image}]. */
    private String detailSections;

    /** JSON string; chỉ dùng khi type=COMBO: [{productId, quantity, sortOrder}]. */
    private String comboProducts;

    /** JSON string; chỉ dùng khi type=COMBO: [{name, quantity, unit, usage}]. */
    private String functions;

    /** JSON string; chỉ dùng khi type=COMBO: [{url}]. */
    private String comboGallery;

    /** Optional; auto-generated from {@code name} when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;

    @NotNull
    private Long productCategoryId;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    @StorageUrl
    private String seoImage;

    /** Gallery images created together with the product (URLs already uploaded). */
    @Valid
    private List<ProductImageRequest> images;
}
