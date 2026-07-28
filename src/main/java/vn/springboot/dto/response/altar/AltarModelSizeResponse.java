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
public class AltarModelSizeResponse {

    private Long id;

    private Long altarModelId;

    private String label;

    private Integer widthCm;

    private Integer depthCm;

    @StorageUrl
    private String backgroundImage;

    private Double surfaceLeft;

    private Double surfaceTop;

    private Double surfaceRight;

    private Double surfaceBottom;

    private Integer surfaceWidthCm;

    private Integer priority;

    /** Wrapper {@code Boolean}; see {@link AltarItemGroupResponse#isActive} for why. */
    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
