package vn.springboot.dto.request.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ContentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCreateRequest {

    @NotBlank
    private String title;

    /** Image URL of the news thumbnail. */
    @NotBlank
    @Size(max = 255)
    private String thumb;

    @NotBlank
    private String shortContent;

    /** JSON string. */
    @NotBlank
    private String des;

    /** Optional; auto-generated from {@code title} when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;

    /** {@code null} → defaults to {@code DRAFT} on create. */
    private ContentStatus status;

    @NotNull
    private Long newsCategoryId;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    private String seoImage;
}
