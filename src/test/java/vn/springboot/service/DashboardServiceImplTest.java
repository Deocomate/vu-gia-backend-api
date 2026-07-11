package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.springboot.dto.response.dashboard.DashboardSummaryResponse;
import vn.springboot.dto.response.dashboard.RevenuePointResponse;
import vn.springboot.dto.response.dashboard.TopProductResponse;
import vn.springboot.entity.enums.ContactStatus;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.enums.Role;
import vn.springboot.repository.ContactRequestRepository;
import vn.springboot.repository.NewsletterSubscriberRepository;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.UserRepository;
import vn.springboot.service.impl.DashboardServiceImpl;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ContactRequestRepository contactRequestRepository;
    @Mock private NewsletterSubscriberRepository newsletterSubscriberRepository;

    @InjectMocks private DashboardServiceImpl service;

    @Test
    void getSummary_aggregatesMetrics_andFillsAllStatuses() {
        when(orderRepository.count()).thenReturn(12L);
        when(orderRepository.countByPaymentStatus(PaymentStatus.PAID)).thenReturn(8L);
        when(orderRepository.sumTotalAmountByPaymentStatus(PaymentStatus.PAID)).thenReturn(5_000_000L);
        when(orderRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(3L);
        when(orderRepository.sumTotalAmountByPaymentStatusSince(eq(PaymentStatus.PAID), any())).thenReturn(1_200_000L);
        when(orderRepository.countGroupByStatus()).thenReturn(List.of(
                new Object[]{OrderStatus.PROCESSING, 5L},
                new Object[]{OrderStatus.COMPLETED, 3L}));
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(40L);
        when(productRepository.count()).thenReturn(25L);
        when(orderItemRepository.totalSoldQuantity()).thenReturn(320L);
        when(contactRequestRepository.countByStatus(ContactStatus.NEW)).thenReturn(2L);
        when(newsletterSubscriberRepository.countByIsActiveTrue()).thenReturn(100L);

        DashboardSummaryResponse summary = service.getSummary();

        assertThat(summary.getTotalOrders()).isEqualTo(12);
        assertThat(summary.getPaidOrders()).isEqualTo(8);
        assertThat(summary.getTotalRevenue()).isEqualTo(5_000_000L);
        assertThat(summary.getTotalCustomers()).isEqualTo(40);
        assertThat(summary.getTotalProductsSold()).isEqualTo(320);
        assertThat(summary.getNewContactRequests()).isEqualTo(2);
        assertThat(summary.getActiveSubscribers()).isEqualTo(100);
        // Every status key present; missing ones default to 0.
        assertThat(summary.getOrdersByStatus()).hasSize(OrderStatus.values().length);
        assertThat(summary.getOrdersByStatus().get("PROCESSING")).isEqualTo(5L);
        assertThat(summary.getOrdersByStatus().get("CANCELLED")).isEqualTo(0L);
    }

    @Test
    void getRevenueSeries_mapsRows() {
        when(orderRepository.revenueSeries(any(), any())).thenReturn(List.of(
                new Object[]{"2026-07-01", 1_000_000L, 2L},
                new Object[]{"2026-07-02", 500_000L, 1L}));

        List<RevenuePointResponse> series = service.getRevenueSeries(null, null);

        assertThat(series).hasSize(2);
        assertThat(series.get(0).getDate()).isEqualTo("2026-07-01");
        assertThat(series.get(0).getRevenue()).isEqualTo(1_000_000L);
        assertThat(series.get(0).getOrders()).isEqualTo(2L);
    }

    @Test
    void getTopProducts_mapsRows_andClampsLimit() {
        when(orderItemRepository.topSellingProducts(any())).thenReturn(List.<Object[]>of(
                new Object[]{10L, "Sofa", 30L, 15_000_000L}));

        List<TopProductResponse> top = service.getTopProducts(999);

        assertThat(top).hasSize(1);
        assertThat(top.get(0).getProductId()).isEqualTo(10L);
        assertThat(top.get(0).getProductName()).isEqualTo("Sofa");
        assertThat(top.get(0).getTotalQuantity()).isEqualTo(30L);
        assertThat(top.get(0).getTotalRevenue()).isEqualTo(15_000_000L);
    }

    @Test
    void getRevenueSeries_defaultsToLast30Days_whenNoBounds() {
        when(orderRepository.revenueSeries(any(), any())).thenReturn(List.of());

        List<RevenuePointResponse> series = service.getRevenueSeries(null, Instant.parse("2026-07-11T00:00:00Z"));

        assertThat(series).isEmpty();
    }
}
