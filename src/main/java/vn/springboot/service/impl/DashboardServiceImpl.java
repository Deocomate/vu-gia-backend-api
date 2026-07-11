package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import vn.springboot.service.DashboardService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int MAX_TOP_PRODUCTS = 50;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ContactRequestRepository contactRequestRepository;
    private final NewsletterSubscriberRepository newsletterSubscriberRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        ZoneId zone = ZoneId.systemDefault();
        Instant startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant startOfMonth = LocalDate.now(zone).withDayOfMonth(1).atStartOfDay(zone).toInstant();

        return DashboardSummaryResponse.builder()
                .totalOrders(orderRepository.count())
                .paidOrders(orderRepository.countByPaymentStatus(PaymentStatus.PAID))
                .totalRevenue(orderRepository.sumTotalAmountByPaymentStatus(PaymentStatus.PAID))
                .todayOrders(orderRepository.countByCreatedAtGreaterThanEqual(startOfToday))
                .todayRevenue(orderRepository.sumTotalAmountByPaymentStatusSince(PaymentStatus.PAID, startOfToday))
                .monthOrders(orderRepository.countByCreatedAtGreaterThanEqual(startOfMonth))
                .monthRevenue(orderRepository.sumTotalAmountByPaymentStatusSince(PaymentStatus.PAID, startOfMonth))
                .ordersByStatus(ordersByStatus())
                .totalCustomers(userRepository.countByRole(Role.CUSTOMER))
                .totalProducts(productRepository.count())
                .totalProductsSold(orderItemRepository.totalSoldQuantity())
                .newContactRequests(contactRequestRepository.countByStatus(ContactStatus.NEW))
                .activeSubscribers(newsletterSubscriberRepository.countByIsActiveTrue())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenuePointResponse> getRevenueSeries(Instant from, Instant to) {
        Instant end = to != null ? to : Instant.now();
        Instant start = from != null ? from : end.minus(30, ChronoUnit.DAYS);

        return orderRepository.revenueSeries(start, end).stream()
                .map(row -> RevenuePointResponse.builder()
                        .date((String) row[0])
                        .revenue(((Number) row[1]).longValue())
                        .orders(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopProducts(int limit) {
        int capped = Math.clamp(limit, 1, MAX_TOP_PRODUCTS);
        return orderItemRepository.topSellingProducts(PageRequest.of(0, capped)).stream()
                .map(row -> TopProductResponse.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .totalQuantity(((Number) row[2]).longValue())
                        .totalRevenue(((Number) row[3]).longValue())
                        .build())
                .toList();
    }

    /** All statuses present (default 0), then overwritten with actual counts. */
    private Map<String, Long> ordersByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            result.put(status.name(), 0L);
        }
        for (Object[] row : orderRepository.countGroupByStatus()) {
            result.put(((OrderStatus) row[0]).name(), ((Number) row[1]).longValue());
        }
        return result;
    }
}
