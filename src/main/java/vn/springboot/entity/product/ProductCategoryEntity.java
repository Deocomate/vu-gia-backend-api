package vn.springboot.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "product_categories")
public class ProductCategoryEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "thumb", nullable = false, length = 255)
    private String thumb;

    @Builder.Default
    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "long_content", columnDefinition = "TEXT")
    private String longContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "des", columnDefinition = "json")
    private String des;

    @Column(name = "slug", unique = true, nullable = false, length = 255)
    private String slug;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "seo_image", length = 255)
    private String seoImage;
}
