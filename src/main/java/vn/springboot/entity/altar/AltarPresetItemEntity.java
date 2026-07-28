package vn.springboot.entity.altar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
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
import vn.springboot.common.entity.BaseEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;

/**
 * One placed (or accessory) item within an {@link AltarPresetEntity}. Two shapes, distinguished
 * by nullability:
 * <ul>
 *   <li>canvas item — {@code productImage} set, {@code x}/{@code y} both set (surface-relative
 *       [0,1] bottom-center anchor, same space as {@link AltarPlacementEntity#getDefaultX()}/
 *       {@code getDefaultY()});</li>
 *   <li>accessory (summary-only, {@code renderOnAltar = false} groups) — {@code productImage},
 *       {@code x} and {@code y} all {@code null}; only {@code quantity} matters.</li>
 * </ul>
 * The invariant {@code productImage != null <=> (x != null && y != null)} is enforced in
 * {@code AltarPresetServiceImpl#validateItemInvariant}, not here — bean validation can't express
 * this cross-field rule on the entity.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "altar_preset_items", indexes = {
        @Index(name = "idx_altar_preset_items_preset_id", columnList = "preset_id"),
        @Index(name = "idx_altar_preset_items_product_id", columnList = "product_id"),
        @Index(name = "idx_altar_preset_items_product_image_id", columnList = "product_image_id")
})
public class AltarPresetItemEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preset_id", nullable = false)
    private AltarPresetEntity preset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    /** {@code null} means this is an accessory/summary-only item, never rendered on the canvas. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_image_id")
    private ProductImageEntity productImage;

    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    /** Surface-relative [0,1] bottom-center anchor X; {@code null} iff {@code productImage} is null. */
    @Column(name = "x")
    private Double x;

    /** Surface-relative [0,1] bottom-center anchor Y; {@code null} iff {@code productImage} is null. */
    @Column(name = "y")
    private Double y;

    @Builder.Default
    @Column(name = "scale_adjust", nullable = false)
    private Double scaleAdjust = 1.0;

    @Builder.Default
    @Column(name = "flipped", nullable = false)
    private boolean flipped = false;

    /** {@code null} means auto-z from {@code y} — same convention as {@link AltarPlacementEntity}. */
    @Column(name = "z_index_override")
    private Integer zIndexOverride;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
