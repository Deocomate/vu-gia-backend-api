package vn.springboot.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Top-of-dashboard KPI cards. Revenue figures count PAID orders only. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private long totalOrders;

    private long paidOrders;

    /** Sum of totalAmount over PAID orders. */
    private long totalRevenue;

    private long todayOrders;

    private long todayRevenue;

    private long monthOrders;

    private long monthRevenue;

    /** Count per order status (key = enum name); every status present, default 0. */
    private Map<String, Long> ordersByStatus;

    private long totalCustomers;

    private long totalProducts;

    /** Total units sold across all orders (sum of order-item quantities). */
    private long totalProductsSold;

    /** Contact requests still in NEW state (need handling). */
    private long newContactRequests;

    private long activeSubscribers;
}
