package vn.springboot.dto.request.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ContentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageSearchRequest {

    private String key;

    private String title;

    private ContentStatus status;

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
