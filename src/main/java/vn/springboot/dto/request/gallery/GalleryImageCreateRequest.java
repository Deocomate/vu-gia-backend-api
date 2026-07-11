package vn.springboot.dto.request.gallery;

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
public class GalleryImageCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String imageUrl;

    @Size(max = 255)
    private String title;

    @Size(max = 100)
    private String category;

    /** {@code null} → defaults to {@code 0} on create. */
    private Integer sortOrder;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;
}
