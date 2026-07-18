package vn.springboot.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.ContentStatus;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsResponse {

    private Long id;

    private String title;

    @StorageUrl
    private String thumb;

    private String shortContent;

    /** JSON string. */
    private String des;

    private String slug;

    private Integer priority;

    private int viewCount;

    private ContentStatus status;

    private Instant publishedAt;

    private NewsCategoryBriefResponse category;

    private String seoTitle;

    private String seoDescription;

    @StorageUrl
    private String seoImage;

    private Instant createdAt;

    private Instant updatedAt;
}
