package vn.springboot.dto.request.redirect;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedirectUpdateRequest {

    @Size(max = 500)
    private String fromPath;

    @Size(max = 500)
    private String toPath;

    /** {@code null} → left unchanged. */
    private Integer statusCode;

    /** {@code null} → left unchanged. */
    private Boolean isActive;
}
