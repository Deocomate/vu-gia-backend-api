package vn.springboot.entity.product;

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
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "products")
public class ProductEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "thumb", nullable = false, length = 255)
    private String thumb;

    @Column(name = "sku", unique = true, length = 100)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ProductType type;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "compare_at_price")
    private Long compareAtPrice;

    @Builder.Default
    @Column(name = "sold_count", nullable = false)
    private int soldCount = 0;

    @Builder.Default
    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "json")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "combo_products", columnDefinition = "json")
    private String comboProducts;

    @Column(name = "slug", unique = true, nullable = false, length = 255)
    private String slug;

    @Builder.Default
    @Column(name = "priority")
    private Integer priority = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_category_id", nullable = false)
    private ProductCategoryEntity productCategory;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "seo_image", length = 255)
    private String seoImage;
}
