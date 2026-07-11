package vn.springboot.dto.request.gallery;

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
public class GalleryImageUpdateRequest {

    @Size(max = 255)
    private String imageUrl;

    @Size(max = 255)
    private String title;

    @Size(max = 100)
    private String category;

    private Integer sortOrder;

    private Boolean isActive;
}
