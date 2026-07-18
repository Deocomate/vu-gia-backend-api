package vn.springboot.dto.request.banner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BannerCreateRequest {

    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    @StorageUrl
    private String imageUrl;

    @Size(max = 255)
    private String linkUrl;

    @NotNull
    private BannerPosition position;

    /** {@code null} → defaults to {@code 0} on create. */
    private Integer sortOrder;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;

    private Instant startsAt;

    private Instant endsAt;
}
