package vn.springboot.dto.request.page;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.ContentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String key;

    @Size(max = 255)
    private String title;

    /** Free-form JSON payload (stored as-is). */
    private String content;

    @Size(max = 255)
    private String heroTitle;

    @Size(max = 255)
    private String heroSubtitle;

    private String heroDes;

    @Size(max = 255)
    @StorageUrl
    private String heroImage;

    /** {@code null} → defaults to {@code DRAFT} on create. */
    private ContentStatus status;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    @StorageUrl
    private String seoImage;
}
