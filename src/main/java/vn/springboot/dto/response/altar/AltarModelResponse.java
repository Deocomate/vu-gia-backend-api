package vn.springboot.dto.response.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarModelResponse {

    private Long id;

    private String name;

    private String slug;

    @StorageUrl
    private String thumb;

    private String description;

    private Integer priority;

    /** Wrapper {@code Boolean}; see {@link AltarItemGroupResponse#isActive} for why. */
    private Boolean isActive;

    /** Sizes are eager-loaded and embedded on both list and detail views. */
    private List<AltarModelSizeResponse> sizes;

    private Instant createdAt;

    private Instant updatedAt;
}
