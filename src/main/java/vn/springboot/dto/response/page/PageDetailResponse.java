package vn.springboot.dto.response.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.ContentStatus;

import java.time.Instant;

/** CMS page representation (distinct from the pagination envelope
 * {@code vn.springboot.dto.response.PageResponse}). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageDetailResponse {

    private Long id;

    private String key;

    private String title;

    private String content;

    private String heroTitle;

    private String heroSubtitle;

    private String heroDes;

    @StorageUrl
    private String heroImage;

    private ContentStatus status;

    private String seoTitle;

    private String seoDescription;

    @StorageUrl
    private String seoImage;

    private Instant createdAt;

    private Instant updatedAt;
}
