package vn.springboot.dto.response.gallery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryImageResponse {

    private Long id;

    @StorageUrl
    private String imageUrl;

    private String title;

    private String category;

    private Integer sortOrder;

    /**
     * Wrapper {@code Boolean} (not primitive) deliberately: a primitive {@code boolean}
     * field named {@code isActive} makes Lombok's getter/JSON property name "active"
     * (the "is" prefix is stripped for primitive booleans), which silently mismatches
     * the FE-facing "isActive" wire contract. The wrapper type keeps the standard
     * {@code getIsActive()}/{@code isActive} JSON-key pairing with no ambiguity.
     */
    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
