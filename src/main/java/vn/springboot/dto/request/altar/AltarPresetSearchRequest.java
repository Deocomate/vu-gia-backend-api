package vn.springboot.dto.request.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code isActive} defaults to {@code true} (a plain field initializer, applied by the no-arg
 * constructor {@code @ModelAttribute} binding uses — same mechanism as
 * {@code AltarModelSearchRequest#page}). The public listing endpoint
 * ({@code GET /api/altar-presets}) is unauthenticated, so this default is the only thing keeping
 * inactive presets out of the storefront by default; the admin builder page passes
 * {@code isActive=false} (or {@code true}) explicitly to see a specific subset.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarPresetSearchRequest {

    private Long altarModelSizeId;

    private Long altarStyleId;

    @Builder.Default
    private Boolean isActive = true;

    /** 1-based page number (1 = first page). */
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "priority";

    @Builder.Default
    private String sortDirection = "ASC";
}
