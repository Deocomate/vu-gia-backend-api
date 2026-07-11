package vn.springboot.dto.request.faq;

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
public class FaqUpdateRequest {

    @Size(max = 500)
    private String question;

    private String answer;

    @Size(max = 100)
    private String category;

    private Integer sortOrder;

    private Boolean isActive;
}
