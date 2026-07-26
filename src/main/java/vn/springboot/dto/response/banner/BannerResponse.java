package vn.springboot.dto.response.banner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.BannerPosition;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {

    private Long id;

    private String title;

    @StorageUrl
    private String imageUrl;

    private String linkUrl;

    private BannerPosition position;

    private Integer sortOrder;

    /**
     * Wrapper {@code Boolean} (not primitive) deliberately: a primitive {@code boolean}
     * field named {@code isActive} makes Lombok's getter/JSON property name "active"
     * (the "is" prefix is stripped for primitive booleans), which silently mismatches
     * the FE-facing "isActive" wire contract. The wrapper type keeps the standard
     * {@code getIsActive()}/{@code isActive} JSON-key pairing with no ambiguity.
     */
    private Boolean isActive;

    private Instant startsAt;

    private Instant endsAt;

    private Instant createdAt;

    private Instant updatedAt;
}
