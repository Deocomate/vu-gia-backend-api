package vn.springboot.dto.request.banner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.BannerPosition;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerSearchRequest {

    private String title;

    private BannerPosition position;

    private Boolean isActive;

    /** 1-based page number (1 = first page). */
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "ASC";
}
