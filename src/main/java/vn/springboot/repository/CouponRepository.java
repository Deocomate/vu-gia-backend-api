package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.coupon.CouponEntity;

import java.util.Optional;

@Repository
public interface CouponRepository
        extends JpaRepository<CouponEntity, Long>, JpaSpecificationExecutor<CouponEntity> {

    Optional<CouponEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Atomically claims one usage of a coupon: increments {@code used_count} only
     * while it is still under the limit (or the limit is unlimited). Returns the
     * number of rows updated — {@code 0} means the coupon just ran out, so the
     * check-then-increment is race-free without any lock.
     */
    @Modifying
    @Query("""
            UPDATE CouponEntity c
               SET c.usedCount = c.usedCount + 1
             WHERE c.id = :id
               AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)
            """)
    int incrementUsedCount(@Param("id") Long id);

    /**
     * Exact inverse of {@link #incrementUsedCount(Long)}: restores one usage on order
     * cancellation. Floors at 0 so a race (e.g. a double-cancel) can never drive the
     * counter negative.
     */
    @Modifying
    @Query("""
            UPDATE CouponEntity c
               SET c.usedCount = GREATEST(c.usedCount - 1, 0)
             WHERE c.id = :id
            """)
    int decrementUsedCount(@Param("id") Long id);
}
