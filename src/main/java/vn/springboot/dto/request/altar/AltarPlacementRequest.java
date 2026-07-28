package vn.springboot.dto.request.altar;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

/**
 * Upsert body for {@code PUT /api/products/{productId}/images/{imageId}/placement}. This is a
 * full-representation PUT, not a partial patch — every field the admin can author is sent every
 * time. {@code scaleAdjust}/{@code flippable} fall back to their entity defaults ({@code 1.0} /
 * {@code true}) when omitted; {@code zIndexOverride} left {@code null} is the legitimate
 * "auto-z from Y" case, not a missing value.
 *
 * <p>{@code defaultX}/{@code defaultY} are surface-relative fractions, historically limited to
 * [0,1] (strictly inside the altar-size's tabletop rect). By product decision an item may now be
 * placed anywhere on the full backdrop image, which in surface-relative terms can fall outside
 * [0,1] — how far depends on how large the tabletop is relative to the image for a given altar
 * size (see the client's {@code fullImageSurfaceBounds}). Bean Validation can't look up a
 * specific size's rect per-request, so the bound below is a generous static one (not
 * per-size-exact) sized for a tabletop occupying at least ~20% of the image on either axis —
 * still finite so a garbage/malicious value doesn't reach persistence unchecked.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarPlacementRequest {

    @NotBlank
    @Size(max = 255)
    @StorageUrl
    private String overlayImage;

    @NotNull
    @DecimalMin("-2.0")
    @DecimalMax("3.0")
    private Double defaultX;

    @NotNull
    @DecimalMin("-2.0")
    @DecimalMax("3.0")
    private Double defaultY;

    @NotNull
    @Positive
    private Integer widthCm;

    /** {@code null} → defaults to {@code 1.0}. */
    @DecimalMin("0.1")
    @DecimalMax("5.0")
    private Double scaleAdjust;

    /** {@code null} → auto-z from {@code defaultY}, not validated against a range. */
    private Integer zIndexOverride;

    /** {@code null} → defaults to {@code true}. */
    private Boolean flippable;
}
