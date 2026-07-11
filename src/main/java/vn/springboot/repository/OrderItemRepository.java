package vn.springboot.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.order.OrderItemEntity;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findByOrder_IdOrderByIdAsc(Long orderId);

    /**
     * Best-selling products: rows of {@code [productId, productName, totalQty, totalRevenue]}
     * ordered by quantity sold. Limit via {@link Pageable}.
     */
    @Query("""
            SELECT oi.product.id, oi.productName, SUM(oi.quantity), SUM(oi.subtotal)
              FROM OrderItemEntity oi
             GROUP BY oi.product.id, oi.productName
             ORDER BY SUM(oi.quantity) DESC
            """)
    List<Object[]> topSellingProducts(Pageable pageable);

    /** Total units sold across COMPLETED orders (matches product.soldCount semantics). */
    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItemEntity oi
             WHERE oi.order.status = vn.springboot.entity.enums.OrderStatus.COMPLETED
            """)
    long totalSoldQuantity();
}
