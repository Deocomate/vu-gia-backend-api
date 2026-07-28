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
 * Surface-rect fields are re-validated cross-field in the service (left&lt;right,
 * top&lt;bottom) since Bean Validation can't express relations between fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarModelSizeCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String label;

    @NotNull
    @Positive
    private Integer widthCm;

    @NotNull
    @Positive
    private Integer depthCm;

    @NotBlank
    @Size(max = 255)
    @StorageUrl
    private String backgroundImage;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceLeft;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceTop;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceRight;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double surfaceBottom;

    @NotNull
    @Positive
    private Integer surfaceWidthCm;

    /** {@code null} → defaults to {@code 0} on create. */
    private Integer priority;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;
}
