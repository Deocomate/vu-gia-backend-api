package vn.springboot.dto.request.altar;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One item within {@link AltarPresetRequest#getItems()}. {@code productImageId} is {@code null}
 * for accessory/summary-only items (no coordinates); otherwise it must carry both {@code x} and
 * {@code y}. This is a cross-field rule bean validation can't express — enforced service-side in
 * {@code AltarPresetServiceImpl#validateItemInvariant}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarPresetItemRequest {

    @NotNull
    private Long productId;

    /** {@code null} => accessory item, never rendered on the canvas. */
    private Long productImageId;

    @NotNull
    @Positive
    private Integer quantity;

    /**
     * Surface-relative; {@code null} iff {@code productImageId} is null. Historically [0,1]
     * (strictly inside the tabletop); now allowed outside that range so an item can be placed
     * anywhere on the full backdrop image — see {@code AltarPlacementRequest}'s docblock for why
     * this bound is a generous static one rather than per-size-exact.
     */
    @DecimalMin("-2.0")
    @DecimalMax("3.0")
    private Double x;

    @DecimalMin("-2.0")
    @DecimalMax("3.0")
    private Double y;

    /** {@code null} => defaults to {@code 1.0}. */
    @DecimalMin("0.1")
    @DecimalMax("5.0")
    private Double scaleAdjust;

    /** {@code null} => defaults to {@code false}. */
    private Boolean flipped;

    /** {@code null} => auto-z from {@code y}, not validated against a range. */
    private Integer zIndexOverride;

    /** {@code null} => defaults to this item's index within the {@code items} list. */
    private Integer sortOrder;
}
