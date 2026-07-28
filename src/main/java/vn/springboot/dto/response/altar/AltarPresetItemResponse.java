package vn.springboot.dto.response.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

/**
 * One item of {@link AltarPresetResponse#getItems()}. Denormalizes basic product display fields
 * (name/slug/thumb/price) directly onto the row — same convention as
 * {@link AltarCustomizerItemResponse} — so the client makes one call instead of a follow-up
 * product lookup per item. {@code placement} is the underlying {@code AltarPlacementEntity} of
 * {@code productImageId} (overlay art + real width, needed to render the item on the canvas at
 * this item's own {@code x}/{@code y}/{@code scaleAdjust}); it is {@code null} for accessory
 * items and for canvas items whose image placement no longer exists.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarPresetItemResponse {

    private Long id;

    private Long productId;

    private String productName;

    private String productSlug;

    @StorageUrl
    private String productThumb;

    private Long productPrice;

    /** {@code null} => accessory item (summary-only, never rendered on the canvas). */
    private Long productImageId;

    /** {@code null} for accessory items, or when the referenced image has no authored placement. */
    private AltarPlacementResponse placement;

    private Integer quantity;

    /** Surface-relative [0,1]; {@code null} iff {@code productImageId} is null. */
    private Double x;

    private Double y;

    private Double scaleAdjust;

    private Boolean flipped;

    /** {@code null} => auto-z from {@code y}. */
    private Integer zIndexOverride;

    private Integer sortOrder;
}
