package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarModelSizeEntity;

import java.util.List;

@Repository
public interface AltarModelSizeRepository extends JpaRepository<AltarModelSizeEntity, Long> {

    List<AltarModelSizeEntity> findByAltarModelIdOrderByPriorityAscIdAsc(Long altarModelId);

    /** Used by {@link vn.springboot.seed.OrphanReferenceChecker} — no DB-level FK to catch this otherwise. */
    @Query("SELECT COUNT(s) FROM AltarModelSizeEntity s WHERE s.altarModel.id NOT IN (SELECT m.id FROM AltarModelEntity m)")
    long countWithOrphanedAltarModel();
}
