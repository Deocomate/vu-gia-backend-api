package vn.springboot.dto.request.news;

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
public class NewsCategoryCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    /** Optional; auto-generated from {@code name} when blank. */
    @Size(max = 255)
    private String slug;

    private Integer priority;
}
