package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarPlacementEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AltarPlacementRepository extends JpaRepository<AltarPlacementEntity, Long> {

    Optional<AltarPlacementEntity> findByProductImageId(Long productImageId);

    boolean existsByProductImageId(Long productImageId);

    /** Bulk lookup used by the altar-customizer feed — one query for every product's first image. */
    List<AltarPlacementEntity> findByProductImageIdIn(List<Long> productImageIds);

    /** Used by {@link vn.springboot.seed.OrphanReferenceChecker} — no DB-level FK to catch this otherwise. */
    @Query("SELECT COUNT(p) FROM AltarPlacementEntity p WHERE p.productImage.id NOT IN (SELECT pi.id FROM ProductImageEntity pi)")
    long countWithOrphanedProductImage();
}
