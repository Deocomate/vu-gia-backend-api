package vn.springboot.entity.altar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
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
 * Catalog of altar accessory "groups" the customizer surfaces (e.g. Bộ tam sự - ngũ sự,
 * Bát hương &amp; phụ kiện). {@code renderOnAltar=false} groups are summary-list only
 * (their items don't get a placeable position on the 3D/2D altar surface).
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "altar_item_groups", indexes = {
        @Index(name = "idx_altar_item_groups_priority", columnList = "priority")
})
public class AltarItemGroupEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", unique = true, nullable = false, length = 150)
    private String slug;

    @Column(name = "thumb", nullable = false, length = 255)
    private String thumb;

    /** {@code false} = accessories rendered as a summary list only, not placed on the altar surface. */
    @Builder.Default
    @Column(name = "render_on_altar", nullable = false)
    private boolean renderOnAltar = true;

    @Builder.Default
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
