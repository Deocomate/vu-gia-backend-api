package vn.springboot.dto.request.altar;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

/**
 * Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. The surface-rect
 * fields are re-validated cross-field in the service against the merged (existing + patched)
 * values, since Bean Validation can't express relations between fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarModelSizeUpdateRequest {

    @Size(max = 100)
    private String label;

    @Positive
    private Integer widthCm;

    @Positive
    private Integer depthCm;

    @Size(max = 255)
    @StorageUrl
    private String backgroundImage;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceLeft;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceTop;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceRight;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceBottom;

    @Positive
    private Integer surfaceWidthCm;

    private Integer priority;

    private Boolean isActive;
}
