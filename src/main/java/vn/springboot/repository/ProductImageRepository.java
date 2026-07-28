package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.product.ProductImageEntity;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    List<ProductImageEntity> findByProductIdOrderByPriorityAscIdAsc(Long productId);

    /**
     * Bulk variant used by the altar-customizer feed: one query for every candidate product's
     * images, ordered so the first row per {@code productId} is that product's first/thumbnail
     * image (matches the single-product gallery ordering above).
     */
    List<ProductImageEntity> findByProductIdInOrderByProductIdAscPriorityAscIdAsc(List<Long> productIds);

    /** Used by {@link vn.springboot.seed.OrphanReferenceChecker} — no DB-level FK to catch this otherwise. */
    @Query("SELECT COUNT(pi) FROM ProductImageEntity pi WHERE pi.product.id NOT IN (SELECT p.id FROM ProductEntity p)")
    long countOrphaned();
}
