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

    private boolean isActive;

    private String seoTitle;

    private String seoDescription;

    @StorageUrl
    private String seoImage;

    private Instant createdAt;

    private Instant updatedAt;
}
