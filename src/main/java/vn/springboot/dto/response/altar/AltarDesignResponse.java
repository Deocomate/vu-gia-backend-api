package vn.springboot.dto.response.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

import java.time.Instant;

/**
 * Detail view of a saved design, returned on open. {@code items}/{@code accessories} are the
 * stored JSON array strings with any entry whose {@code productId} no longer resolves to a
 * published product dropped — {@code droppedItemCount} tells the client how many, so it can
 * surface "N sản phẩm không còn khả dụng đã được loại bỏ" without silently corrupting the design.
 * {@code currentTotalPrice} is recomputed from live catalog prices on every read; a mismatch
 * against the stored {@code totalPrice} snapshot means at least one referenced product's price
 * changed since save. See {@code AltarDesignServiceImpl#getById}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarDesignResponse {

    private Long id;

    private String name;

    @StorageUrl
    private String thumb;

    private Long altarModelSizeId;

    private String altarModelSizeLabel;

    /** {@code null} => the design isn't tied to a specific glaze style. */
    private Long altarStyleId;

    /** Raw JSON array string, with entries for missing/unpublished products dropped. */
    private String items;

    /** Raw JSON array string, with entries for missing/unpublished products dropped. */
    private String accessories;

    /** Snapshot stored at save time. */
    private Long totalPrice;

    /** Recomputed from live catalog prices — see class javadoc. */
    private Long currentTotalPrice;

    /** Count of items/accessories entries dropped because their product is missing or unpublished. */
    private int droppedItemCount;

    private Instant createdAt;

    private Instant updatedAt;
}
