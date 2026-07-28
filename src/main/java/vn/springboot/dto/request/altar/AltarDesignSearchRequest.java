package vn.springboot.dto.request.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Pagination-only — no filter fields, the library is always scoped to the caller's own designs. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarDesignSearchRequest {

    /** 1-based page number (1 = first page). */
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
