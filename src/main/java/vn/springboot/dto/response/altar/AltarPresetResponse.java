package vn.springboot.dto.response.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

import java.time.Instant;
import java.util.List;

/**
 * {@code priceSummary} is derived — the sum of {@code item.productPrice * item.quantity} across
 * every item, canvas and accessory alike — never stored, recomputed on every read in
 * {@code AltarPresetServiceImpl}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarPresetResponse {

    private Long id;

    private String name;

    private String slug;

    @StorageUrl
    private String thumb;

    private String description;

    private Long altarModelSizeId;

    /** {@code null} => the preset isn't tied to a specific glaze style. */
    private Long altarStyleId;

    private Integer priority;

    /** Wrapper {@code Boolean}; see {@link AltarItemGroupResponse#isActive} for why. */
    private Boolean isActive;

    /** Derived, not stored — see class javadoc. */
    private Long priceSummary;

    private List<AltarPresetItemResponse> items;

    private Instant createdAt;

    private Instant updatedAt;
}
