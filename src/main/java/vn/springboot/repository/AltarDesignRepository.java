package vn.springboot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarDesignEntity;

/**
 * Designs are strictly per-user data. The list and count finders are scoped by {@code userId} at
 * the query level; single-row get/rename/delete deliberately use the inherited unscoped
 * {@code findById} instead, followed by an explicit ownership check in
 * {@code AltarDesignServiceImpl} that throws {@code ALTAR_DESIGN_NOT_FOUND} (404, never 403) on a
 * mismatch — the same belt-and-suspenders split as {@code OrderRepository}/{@code OrderServiceImpl}
 * ("specification-scoped list + explicit check on get-by-id"). Never bypass that explicit check by
 * adding a {@code findByIdAndUser_Id} shortcut here — the service-level check is the one place this
 * codebase's per-user-ownership contract is tested and enforced.
 */
@Repository
public interface AltarDesignRepository extends JpaRepository<AltarDesignEntity, Long> {

    Page<AltarDesignEntity> findByUser_Id(Long userId, Pageable pageable);

    long countByUser_Id(Long userId);

    /** Guards {@code AltarModelServiceImpl}'s size delete — a size still referenced by a
     * customer's saved design must not be deletable (would strand the design). */
    boolean existsByAltarModelSize_Id(Long altarModelSizeId);

    /** Guards {@code AltarStyleServiceImpl.delete} the same way. */
    boolean existsByAltarStyle_Id(Long altarStyleId);
}
