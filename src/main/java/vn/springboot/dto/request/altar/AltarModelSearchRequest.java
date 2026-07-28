package vn.springboot.dto.request.altar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarModelSearchRequest {

    private String name;

    private Boolean isActive;

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
