package vn.springboot.dto.response.gallery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryImageResponse {

    private Long id;

    private String imageUrl;

    private String title;

    private String category;

    private Integer sortOrder;

    private boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
