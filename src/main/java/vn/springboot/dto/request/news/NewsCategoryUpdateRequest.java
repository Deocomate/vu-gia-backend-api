package vn.springboot.dto.request.news;

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
public class NewsCategoryUpdateRequest {

    @Size(max = 50)
    private String name;

    /** Optional; slug is left unchanged when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;
}
