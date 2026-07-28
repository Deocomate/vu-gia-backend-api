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
public class AltarStyleResponse {

    private Long id;

    private String name;

    private String slug;

    @StorageUrl
    private String thumb;

    private String description;

    private Integer priority;

    /** Wrapper {@code Boolean}; see {@link AltarItemGroupResponse#isActive} for why. */
    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
