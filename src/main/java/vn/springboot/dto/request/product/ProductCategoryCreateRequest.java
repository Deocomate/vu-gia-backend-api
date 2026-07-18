package vn.springboot.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    /** Image URL of the category thumbnail. */
    @NotBlank
    @Size(max = 255)
    @StorageUrl
    private String thumb;

    /** Optional; auto-generated from {@code name} when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;

    private String longContent;

    /** JSON string. */
    private String des;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    @StorageUrl
    private String seoImage;
}
