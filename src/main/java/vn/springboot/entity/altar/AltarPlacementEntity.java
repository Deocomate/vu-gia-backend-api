package vn.springboot.entity.altar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import vn.springboot.common.entity.BaseEntity;
import vn.springboot.entity.product.ProductImageEntity;

/**
 * The optional (0..1) altar placement authored for a single {@link ProductImageEntity}: a
 * transparent overlay PNG plus the default position, real-world width and z-order it takes
 * when a customer drops it onto an altar surface. See the coordinate/scale contract in
 * {@code phase-01-backend-altar-catalog-domain-and-admin-crud.md} and the auto-z rule in
 * {@code phase-02-placement-model-and-admin-drag-drop-editor.md}.
 *
 * <p>Note {@code ProductImageEntity} declares its own {@code @Id} and does not extend
 * {@link BaseEntity} — the {@code productImage} relation targets that id normally.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "altar_placements")
public class AltarPlacementEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_image_id", nullable = false, unique = true)
    private ProductImageEntity productImage;

    /** Transparent PNG, cropped tight to the object. */
    @Column(name = "overlay_image", nullable = false, length = 255)
    private String overlayImage;

    /** Surface-relative [0,1] bottom-center anchor X. */
    @Column(name = "default_x", nullable = false)
    private Double defaultX;

    /** Surface-relative [0,1] bottom-center anchor Y. */
    @Column(name = "default_y", nullable = false)
    private Double defaultY;

    /** Real object width in cm — drives cross-altar-size scaling. */
    @Column(name = "width_cm", nullable = false)
    private Integer widthCm;

    @Builder.Default
    @Column(name = "scale_adjust", nullable = false)
    private Double scaleAdjust = 1.0;

    /** {@code null} means auto-z from {@code defaultY} — a legitimate, intentional null. */
    @Column(name = "z_index_override")
    private Integer zIndexOverride;

    @Builder.Default
    @Column(name = "flippable", nullable = false)
    private boolean flippable = true;
}
