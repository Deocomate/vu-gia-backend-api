package vn.springboot.dto.request.banner;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.BannerPosition;

import java.time.Instant;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerUpdateRequest {

    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String imageUrl;

    @Size(max = 255)
    private String linkUrl;

    private BannerPosition position;

    private Integer sortOrder;

    private Boolean isActive;

    private Instant startsAt;

    private Instant endsAt;
}
