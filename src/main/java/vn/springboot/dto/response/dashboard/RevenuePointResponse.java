package vn.springboot.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One day on the revenue chart (PAID orders). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenuePointResponse {

    /** Day label, {@code yyyy-MM-dd}. */
    private String date;

    private long revenue;

    private long orders;
}
