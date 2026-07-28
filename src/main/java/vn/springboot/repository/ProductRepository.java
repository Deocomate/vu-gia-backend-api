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
import vn.springboot.entity.enums.CategoryType;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.product.ProductEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository
        extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    /**
     * Overrides the Specification finder to eagerly join {@code productCategory}/
     * {@code altarItemGroup}/{@code altarStyle}, so mapping a page of products to
     * responses doesn't trigger one extra query per row per to-one association (N+1).
     * Safe with pagination — these are all to-one joins, not collections.
     */
    @Override
    @EntityGraph(attributePaths = {"productCategory", "altarItemGroup", "altarStyle"})
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

    /** Used by {@link vn.springboot.seed.OrphanReferenceChecker} — no DB-level FK to catch this otherwise. */
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.productCategory.id NOT IN (SELECT c.id FROM ProductCategoryEntity c)")
    long countWithOrphanedCategory();

    /** Used by {@link vn.springboot.seed.OrphanReferenceChecker}. {@code altarItemGroup} is nullable
     * (only altar-set products carry it), so only non-null values are checked. */
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.altarItemGroup IS NOT NULL AND p.altarItemGroup.id NOT IN (SELECT g.id FROM AltarItemGroupEntity g)")
    long countWithOrphanedAltarItemGroup();

    /** Same as {@link #countWithOrphanedAltarItemGroup()} but for the nullable {@code altarStyle}. */
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.altarStyle IS NOT NULL AND p.altarStyle.id NOT IN (SELECT s.id FROM AltarStyleEntity s)")
    long countWithOrphanedAltarStyle();

    /**
     * Bulk-nulls {@code altarItemGroup} on every product referencing the given group, so
     * deleting an {@code AltarItemGroupEntity} doesn't orphan the products that carry it
     * (enforces {@code ON DELETE SET NULL} semantics at the service layer).
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.altarItemGroup = null WHERE p.altarItemGroup.id = :altarItemGroupId")
    int nullifyAltarItemGroupReferences(@Param("altarItemGroupId") Long altarItemGroupId);

    /** Same as {@link #nullifyAltarItemGroupReferences} but for {@code altarStyle}. */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.altarStyle = null WHERE p.altarStyle.id = :altarStyleId")
    int nullifyAltarStyleReferences(@Param("altarStyleId") Long altarStyleId);

    /**
     * Backs the public altar-customizer feed ({@code GET /api/altar-customizer/items}): one
     * fetch-joined query for {@code productCategory}/{@code altarItemGroup}/{@code altarStyle},
     * filtered to published {@code BO_DO_THO} products and optionally narrowed by group/style.
     * Image + placement joining happens separately in {@code AltarCustomizerServiceImpl} (bulk
     * IN queries, not per-product) since they're not to-one relations reachable from here.
     */
    @Query("""
            SELECT p FROM ProductEntity p
            JOIN FETCH p.productCategory pc
            LEFT JOIN FETCH p.altarItemGroup aig
            LEFT JOIN FETCH p.altarStyle ast
            WHERE p.status = :status
              AND pc.categoryType = :categoryType
              AND (:altarItemGroupId IS NULL OR aig.id = :altarItemGroupId)
              AND (:altarStyleId IS NULL OR ast.id = :altarStyleId)
            ORDER BY p.priority ASC, p.id ASC
            """)
    List<ProductEntity> findAltarCustomizerItems(
            @Param("status") ProductStatus status,
            @Param("categoryType") CategoryType categoryType,
            @Param("altarItemGroupId") Long altarItemGroupId,
            @Param("altarStyleId") Long altarStyleId);
}
