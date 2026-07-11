package vn.springboot.dto.response.banner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.BannerPosition;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {

    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private BannerPosition position;

    private Integer sortOrder;

    private boolean isActive;

    private Instant startsAt;

    private Instant endsAt;

    private Instant createdAt;

    private Instant updatedAt;
}
