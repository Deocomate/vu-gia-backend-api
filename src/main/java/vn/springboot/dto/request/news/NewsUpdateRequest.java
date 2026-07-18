package vn.springboot.dto.request.news;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.ContentStatus;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsUpdateRequest {

    private String title;

    @Size(max = 255)
    @StorageUrl
    private String thumb;

    private String shortContent;

    /** JSON string. */
    private String des;

    /** Optional; ignored when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;

    private ContentStatus status;

    private Long newsCategoryId;

    @Size(max = 255)
    private String seoTitle;

    @Size(max = 500)
    private String seoDescription;

    @Size(max = 255)
    @StorageUrl
    private String seoImage;
}
