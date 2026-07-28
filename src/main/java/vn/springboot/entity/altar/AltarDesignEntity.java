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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.springboot.common.entity.BaseEntity;
import vn.springboot.entity.user.UserEntity;

/**
 * A customer's saved altar-customizer arrangement ("Lưu vào thư viện"). Strictly per-user data —
 * every read/write is ownership-checked in {@code AltarDesignServiceImpl} (404, never 403, for a
 * foreign id — see that class's javadoc), mirroring the {@link vn.springboot.entity.order.OrderEntity}
 * precedent, the only other per-user-ownership-checked resource in this codebase.
 *
 * <p>{@code items}/{@code accessories} are opaque JSON snapshots the client round-trips (same
 * shape as the canvas reducer state / {@code [{productId, quantity}]}), never queried by field —
 * same {@code @JdbcTypeCode(SqlTypes.JSON)} raw-{@code String}-column pattern as
 * {@link vn.springboot.entity.product.ProductEntity#getComboProducts()}. {@code totalPrice} is a
 * price snapshot taken at save time; the live-recomputed price is derived on read, never stored
 * (see {@code AltarDesignServiceImpl#getById}).
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "altar_designs", indexes = {
        @Index(name = "idx_altar_designs_user_id", columnList = "user_id")
})
public class AltarDesignEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Composed PNG URL, uploaded via the generic {@code POST /api/media/upload} pipeline. */
    @Column(name = "thumb", nullable = false, length = 500)
    private String thumb;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "altar_model_size_id", nullable = false)
    private AltarModelSizeEntity altarModelSize;

    /** {@code null} => the design isn't tied to a specific glaze style. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "altar_style_id")
    private AltarStyleEntity altarStyle;

    /** Canvas item array snapshot, same shape as the client reducer state. Validated server-side
     * before persisting — see {@code AltarDesignServiceImpl#validateDesignPayload}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "items", nullable = false, columnDefinition = "json")
    private String items;

    /** {@code [{productId, quantity}]} accessory snapshot — same validation as {@link #items}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "accessories", nullable = false, columnDefinition = "json")
    private String accessories;

    /** Price snapshot at save time — see class javadoc. */
    @Column(name = "total_price", nullable = false)
    private Long totalPrice;
}
