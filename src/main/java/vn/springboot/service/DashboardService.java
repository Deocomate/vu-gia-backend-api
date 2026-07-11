package vn.springboot.service;

import vn.springboot.dto.response.dashboard.DashboardSummaryResponse;
import vn.springboot.dto.response.dashboard.RevenuePointResponse;
import vn.springboot.dto.response.dashboard.TopProductResponse;

import java.time.Instant;
import java.util.List;

/** Read-only aggregates for the admin dashboard. */
public interface DashboardService {

    DashboardSummaryResponse getSummary();

    /** Daily paid-revenue series in [from, to]; both default to the last 30 days. */
    List<RevenuePointResponse> getRevenueSeries(Instant from, Instant to);

    List<TopProductResponse> getTopProducts(int limit);
}
