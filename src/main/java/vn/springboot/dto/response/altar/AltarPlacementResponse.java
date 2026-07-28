package vn.springboot.dto.response.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarPlacementResponse {

    private Long id;

    private Long productImageId;

    @StorageUrl
    private String overlayImage;

    private Double defaultX;

    private Double defaultY;

    private Integer widthCm;

    private Double scaleAdjust;

    /** {@code null} → auto-z from {@code defaultY} on the frontend. */
    private Integer zIndexOverride;

    /** Wrapper {@code Boolean}; see {@link AltarItemGroupResponse#isActive} for why. */
    private Boolean flippable;

    private Instant createdAt;

    private Instant updatedAt;
}
