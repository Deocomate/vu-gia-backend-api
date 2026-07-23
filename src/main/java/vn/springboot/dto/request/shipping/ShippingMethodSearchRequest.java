package vn.springboot.dto.request.shipping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingMethodSearchRequest {

    private String name;

    private Boolean isActive;

    /** 1-based page number (1 = first page). */
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "sortOrder";

    @Builder.Default
    private String sortDirection = "ASC";
}
