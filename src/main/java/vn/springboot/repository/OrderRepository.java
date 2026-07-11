package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.order.OrderEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository
        extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    /** Idempotent-checkout lookup: the existing order for a (user, key) pair. */
    Optional<OrderEntity> findByUser_IdAndIdempotencyKey(Long userId, String idempotencyKey);

    /** Payment reconciliation: match a bank transfer memo to its order. */
    Optional<OrderEntity> findByOrderCode(String orderCode);

    /** How many times this user has already used a given coupon (per-user limit). */
    long countByUser_IdAndCoupon_Id(Long userId, Long couponId);

    // ----- Dashboard aggregates -----

    long countByPaymentStatus(PaymentStatus paymentStatus);

    long countByCreatedAtGreaterThanEqual(Instant from);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.paymentStatus = :status")
    long sumTotalAmountByPaymentStatus(@Param("status") PaymentStatus status);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o
             WHERE o.paymentStatus = :status AND o.createdAt >= :from
            """)
    long sumTotalAmountByPaymentStatusSince(@Param("status") PaymentStatus status,
                                            @Param("from") Instant from);

    /** Rows of {@code [OrderStatus, count]} — used to build the status breakdown. */
    @Query("SELECT o.status, COUNT(o) FROM OrderEntity o GROUP BY o.status")
    List<Object[]> countGroupByStatus();

    /**
     * Daily paid-revenue series: rows of {@code [yyyy-MM-dd, revenue, orderCount]}
     * between two instants. Native so it can group by the SQL {@code DATE()}.
     */
    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS d,
                   COALESCE(SUM(total_amount), 0)      AS revenue,
                   COUNT(*)                            AS orders
              FROM orders
             WHERE payment_status = 'PAID'
               AND created_at BETWEEN :from AND :to
             GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
             ORDER BY d
            """, nativeQuery = true)
    List<Object[]> revenueSeries(@Param("from") Instant from, @Param("to") Instant to);
}
