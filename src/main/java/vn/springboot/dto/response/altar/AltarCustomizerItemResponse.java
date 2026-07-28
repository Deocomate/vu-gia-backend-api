package vn.springboot.dto.response.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

/**
 * One row of {@code GET /api/altar-customizer/items}: a published {@code BO_DO_THO} product with
 * its placement (from its first/thumbnail image) joined inline, so Phase 4's canvas makes one
 * call instead of N+1 per-image placement fetches.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarCustomizerItemResponse {

    private Long productId;

    private String name;

    private String slug;

    private Long price;

    @StorageUrl
    private String thumb;

    /** {@code product.altarItemGroup.id}, null if the product has no group assigned yet. */
    private Long groupId;

    /** {@code product.altarStyle.id}, null if the product has no style assigned yet. */
    private Long styleId;

    /**
     * {@code product.altarItemGroup.renderOnAltar}, defaulted to {@code false} when the product
     * has no altar item group assigned (nothing to key the placement grouping off of yet).
     */
    private Boolean renderOnAltar;

    /**
     * Placement of the product's first (priority ASC, id ASC) image, or {@code null} if that
     * image has none. Reuses {@link AltarPlacementResponse} as-is — its {@code productImageId}
     * field serves as the image id.
     */
    private AltarPlacementResponse placement;
}
