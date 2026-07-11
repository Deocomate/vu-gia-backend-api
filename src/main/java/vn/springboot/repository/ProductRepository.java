package vn.springboot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.product.ProductEntity;

import java.util.Optional;

@Repository
public interface ProductRepository
        extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    /**
     * Overrides the Specification finder to eagerly join {@code productCategory},
     * so mapping a page of products to responses doesn't trigger one extra query
     * per row (N+1). Safe with pagination — it's a to-one join, not a collection.
     */
    @Override
    @EntityGraph(attributePaths = "productCategory")
    Page<ProductEntity> findAll(Specification<ProductEntity> spec, Pageable pageable);

    Optional<ProductEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductCategoryId(Long productCategoryId);

    /**
     * Atomically bumps a product's sold counter by the ordered quantity. Done as a
     * single UPDATE (not read-modify-write) so concurrent orders can't lose counts.
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.soldCount = p.soldCount + :quantity WHERE p.id = :id")
    int incrementSoldCount(@Param("id") Long id, @Param("quantity") int quantity);
}
