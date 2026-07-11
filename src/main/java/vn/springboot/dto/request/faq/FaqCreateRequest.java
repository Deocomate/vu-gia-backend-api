package vn.springboot.dto.request.faq;

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
public class FaqCreateRequest {

    @NotBlank
    @Size(max = 500)
    private String question;

    @NotBlank
    private String answer;

    @Size(max = 100)
    private String category;

    private Integer sortOrder;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;
}
