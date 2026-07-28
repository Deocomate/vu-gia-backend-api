package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarPresetEntity;

@Repository
public interface AltarPresetRepository
        extends JpaRepository<AltarPresetEntity, Long>, JpaSpecificationExecutor<AltarPresetEntity> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    /** Guards {@code AltarModelServiceImpl}'s size delete — a size still referenced by a preset
     * must not be deletable (would strand the preset pointing at nothing). */
    boolean existsByAltarModelSize_Id(Long altarModelSizeId);

    /** Guards {@code AltarStyleServiceImpl.delete} the same way. */
    boolean existsByAltarStyle_Id(Long altarStyleId);

    /** Used by {@link vn.springboot.seed.OrphanReferenceChecker} — no DB-level FK to catch this otherwise. */
    @Query("SELECT COUNT(p) FROM AltarPresetEntity p WHERE p.altarModelSize.id NOT IN (SELECT s.id FROM AltarModelSizeEntity s)")
    long countWithOrphanedAltarModelSize();

    /** {@code altarStyle} is nullable (a preset needn't be tied to a style), so only non-null values are checked. */
    @Query("SELECT COUNT(p) FROM AltarPresetEntity p WHERE p.altarStyle IS NOT NULL AND p.altarStyle.id NOT IN (SELECT s.id FROM AltarStyleEntity s)")
    long countWithOrphanedAltarStyle();
}
