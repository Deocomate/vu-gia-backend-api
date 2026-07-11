package vn.springboot.dto.request.page;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ContentStatus;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageUpdateRequest {

    @Size(max = 255)
    private String key;

    @Size(max = 255)
    private String title;

    private String content;

    @Size(max = 255)
    private String heroTitle;

    @Size(max = 255)
    private String heroSubtitle;

    private String heroDes;

    @Size(max = 255)
    private String heroImage;

    private ContentStatus status;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    private String seoImage;
}
