package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarPresetItemEntity;

import java.util.List;

@Repository
public interface AltarPresetItemRepository extends JpaRepository<AltarPresetItemEntity, Long> {

    List<AltarPresetItemEntity> findByPresetIdOrderBySortOrderAscIdAsc(Long presetId);

    boolean existsByProductId(Long productId);

    boolean existsByProductImageId(Long productImageId);

    /** Names the preset(s) referencing a product, for the 409 delete-guard in {@code ProductServiceImpl}. */
    @Query("SELECT DISTINCT i.preset.name FROM AltarPresetItemEntity i WHERE i.product.id = :productId")
    List<String> findDistinctPresetNamesByProductId(@Param("productId") Long productId);

    /** Names the preset(s) referencing a product image, for the 409 delete-guard in {@code ProductImageServiceImpl}. */
    @Query("SELECT DISTINCT i.preset.name FROM AltarPresetItemEntity i WHERE i.productImage.id = :productImageId")
    List<String> findDistinctPresetNamesByProductImageId(@Param("productImageId") Long productImageId);

    /** Used by {@link vn.springboot.seed.OrphanReferenceChecker} — no DB-level FK to catch this otherwise. */
    @Query("SELECT COUNT(i) FROM AltarPresetItemEntity i WHERE i.preset.id NOT IN (SELECT p.id FROM AltarPresetEntity p)")
    long countWithOrphanedPreset();

    /** Same idea as {@link #countWithOrphanedPreset()} but for the non-nullable {@code product}. */
    @Query("SELECT COUNT(i) FROM AltarPresetItemEntity i WHERE i.product.id NOT IN (SELECT p.id FROM ProductEntity p)")
    long countWithOrphanedProduct();

    /** {@code productImage} is nullable (accessory items never carry one), so only non-null values are checked. */
    @Query("SELECT COUNT(i) FROM AltarPresetItemEntity i WHERE i.productImage IS NOT NULL AND i.productImage.id NOT IN (SELECT pi.id FROM ProductImageEntity pi)")
    long countWithOrphanedProductImage();
}
