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
 * An admin-authored, pre-arranged altar ("bộ gợi ý"): metadata plus a target altar model size
 * and optional glaze style, owning an ordered list of {@link AltarPresetItemEntity} rows.
 *
 * <p>Items carry a unidirectional FK back to this entity — no {@code @OneToMany} collection here
 * — the same style Phase 1 used for {@code AltarModelEntity} -> {@code AltarModelSizeEntity}
 * (see {@code AltarModelServiceImpl#delete}). The service deletes/replaces child rows explicitly
 * ({@code AltarPresetServiceImpl}) instead of relying on JPA cascade/orphanRemoval.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "altar_presets", indexes = {
        @Index(name = "idx_altar_presets_altar_model_size_id", columnList = "altar_model_size_id"),
        @Index(name = "idx_altar_presets_altar_style_id", columnList = "altar_style_id")
})
public class AltarPresetEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", unique = true, nullable = false, length = 200)
    private String slug;

    @Column(name = "thumb", nullable = false, length = 255)
    private String thumb;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Target altar model size — a preset stays tied to exactly one size (decision D3). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "altar_model_size_id", nullable = false)
    private AltarModelSizeEntity altarModelSize;

    /** {@code null} => the preset isn't tied to a specific glaze style. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "altar_style_id")
    private AltarStyleEntity altarStyle;

    @Builder.Default
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
