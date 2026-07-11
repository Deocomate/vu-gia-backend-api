package vn.springboot.dto.request.redirect;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedirectCreateRequest {

    @NotBlank
    @Size(max = 500)
    private String fromPath;

    @NotBlank
    @Size(max = 500)
    private String toPath;

    /** {@code null} → defaults to {@code 301} on create. */
    private Integer statusCode;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;
}
