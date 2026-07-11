package vn.springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.response.dashboard.DashboardSummaryResponse;
import vn.springboot.dto.response.dashboard.RevenuePointResponse;
import vn.springboot.dto.response.dashboard.TopProductResponse;
import vn.springboot.service.DashboardService;

import java.time.Instant;
import java.util.List;

/**
 * Admin dashboard analytics. All endpoints are staff-only
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @GetMapping("/revenue")
    public ApiResponse<List<RevenuePointResponse>> revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ApiResponse.success(dashboardService.getRevenueSeries(from, to));
    }

    @GetMapping("/top-products")
    public ApiResponse<List<TopProductResponse>> topProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(dashboardService.getTopProducts(limit));
    }
}
