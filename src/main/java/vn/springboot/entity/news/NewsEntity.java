package vn.springboot.entity.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.springboot.common.entity.BaseEntity;
import vn.springboot.entity.enums.ContentStatus;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "news")
public class NewsEntity extends BaseEntity {

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "thumb", nullable = false, length = 255)
    private String thumb;

    @Column(name = "short_content", nullable = false, columnDefinition = "TEXT")
    private String shortContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "des", nullable = false, columnDefinition = "json")
    private String des;

    @Column(name = "slug", unique = true, nullable = false, length = 255)
    private String slug;

    @Builder.Default
    @Column(name = "priority")
    private Integer priority = 0;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "news_category_id", nullable = false)
    private NewsCategoryEntity newsCategory;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "seo_image", length = 255)
    private String seoImage;
}
