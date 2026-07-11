package vn.springboot.dto.request.product;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryUpdateRequest {

    @Size(max = 50)
    private String name;

    /** Image URL of the category thumbnail. */
    @Size(max = 255)
    private String thumb;

    /** Optional; slug is left unchanged when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;

    private String longContent;

    /** JSON string. */
    private String des;

    private Boolean isActive;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    private String seoImage;
}
