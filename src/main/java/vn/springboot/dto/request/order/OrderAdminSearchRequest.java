package vn.springboot.dto.request.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentStatus;

import java.time.Instant;

/** Admin order search — cross-user, with fuller filters than the customer view. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAdminSearchRequest {

    private String orderCode;

    private OrderStatus status;

    private PaymentStatus paymentStatus;

    /** Filter to a specific customer. */
    private Long userId;

    private String couponCode;

    /** Placed-at (created_at) lower bound, inclusive. ISO-8601, e.g. 2026-07-01T00:00:00Z. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant placedFrom;

    /** Placed-at (created_at) upper bound, inclusive. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant placedTo;

    /** 1-based page number (1 = first page). */
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "DESC";
}
