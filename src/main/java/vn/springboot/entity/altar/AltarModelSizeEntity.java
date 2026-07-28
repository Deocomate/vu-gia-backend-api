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

/**
 * One size variant of an {@link AltarModelEntity} (e.g. "127 x 61 cm"), carrying its own
 * render background and usable-surface rect. See the coordinate/scale contract in
 * {@code phase-01-backend-altar-catalog-domain-and-admin-crud.md} for how {@code surfaceLeft/
 * Top/Right/Bottom} and {@code surfaceWidthCm} are consumed by the placement renderer.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "altar_model_sizes", indexes = {
        @Index(name = "idx_altar_model_sizes_altar_model_id", columnList = "altar_model_id")
})
public class AltarModelSizeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "altar_model_id", nullable = false)
    private AltarModelEntity altarModel;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "width_cm", nullable = false)
    private Integer widthCm;

    @Column(name = "depth_cm", nullable = false)
    private Integer depthCm;

    @Column(name = "background_image", nullable = false, length = 255)
    private String backgroundImage;

    /** Usable-surface rect, expressed as fractions of the background image's own dimensions. */
    @Column(name = "surface_left", nullable = false)
    private Double surfaceLeft;

    @Column(name = "surface_top", nullable = false)
    private Double surfaceTop;

    @Column(name = "surface_right", nullable = false)
    private Double surfaceRight;

    @Column(name = "surface_bottom", nullable = false)
    private Double surfaceBottom;

    /** Real usable tabletop width in cm, used to scale placed items by their real-world width. */
    @Column(name = "surface_width_cm", nullable = false)
    private Integer surfaceWidthCm;

    @Builder.Default
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
