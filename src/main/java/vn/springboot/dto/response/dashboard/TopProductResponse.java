package vn.springboot.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A best-selling product row, aggregated across all order items. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductResponse {

    private Long productId;

    private String productName;

    private long totalQuantity;

    private long totalRevenue;
}
