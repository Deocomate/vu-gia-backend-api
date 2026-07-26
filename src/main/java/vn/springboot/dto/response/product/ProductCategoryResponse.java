package vn.springboot.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.CategoryType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryResponse {

    private Long id;

    private CategoryType categoryType;

    private String name;

    @StorageUrl
    private String thumb;

    private Integer priority;

    private String shortDescription;

    private String detailContent;

    private String slug;

    /**
     * Wrapper {@code Boolean} (not primitive) deliberately: a primitive {@code boolean}
     * field named {@code isActive} makes Lombok's getter/JSON property name "active"
     * (the "is" prefix is stripped for primitive booleans), which silently mismatches
     * the FE-facing "isActive" wire contract. The wrapper type keeps the standard
     * {@code getIsActive()}/{@code isActive} JSON-key pairing with no ambiguity.
     */
    private Boolean isActive;

    private String seoTitle;

    private String seoDescription;

    @StorageUrl
    private String seoImage;

    private Instant createdAt;

    private Instant updatedAt;
}
