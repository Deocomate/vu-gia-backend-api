package vn.springboot.dto.response.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

import java.time.Instant;

/**
 * Lightweight per-row shape for the design library list (grid of thumbnails). {@code totalPrice}
 * is the stored save-time snapshot — no live price recompute here, that only matters when a
 * design is actually opened (see {@link AltarDesignResponse#getCurrentTotalPrice()}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarDesignSummaryResponse {

    private Long id;

    private String name;

    @StorageUrl
    private String thumb;

    private Long altarModelSizeId;

    private String altarModelSizeLabel;

    private String altarModelName;

    /** {@code null} => the design isn't tied to a specific glaze style. */
    private Long altarStyleId;

    private String altarStyleName;

    /** Stored snapshot at save time. */
    private Long totalPrice;

    private Instant createdAt;

    private Instant updatedAt;
}
