package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;

import java.util.List;

/**
 * Seeds the 3 real altar placements — one per {@code bat-huong-1/2/3.png} overlay, the only
 * transparent-cutout assets that exist in the repo (plan decision D1). Every other
 * altar-set product ({@link ProductSeeder#buildAltarSetProducts()}) intentionally gets
 * <em>no</em> placement row rather than a fabricated one: those products are real, complete
 * catalog rows (visible in the palette, priced, described) that simply cannot be dragged
 * onto the altar canvas yet — a documented scope reduction, not a defect (see the phase
 * plan's Architecture section, "Accepted consequence").
 *
 * <p>Runs after {@link ProductSeeder} (needs the generated {@code product_image_id}s for the
 * "Bát hương 20cm" product's 3 images) and resolves them via
 * {@link ProductImageRepository#findByProductIdOrderByPriorityAscIdAsc(Long)} — never a
 * literal id — which returns the 3 images in {@code priority} order (0/1/2 = bat-huong-1/2/3).
 *
 * <p>No optional/nullable columns are left null on any row here except
 * {@code zIndexOverride}, which is intentionally {@code null} on every placement (auto-z
 * from {@code defaultY} is the seeded intent — see {@link AltarPlacementEntity#getZIndexOverride()}).
 */
@Component
@RequiredArgsConstructor
public class AltarPlacementSeeder implements DomainSeeder {

    /** Matches {@code ProductSeeder#buildAltarSetProducts()}'s "Bát hương 20cm" row. */
    private static final String BAT_HUONG_SLUG = "bat-huong-20cm";

    private final AltarPlacementRepository altarPlacementRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public boolean isEmpty() {
        return altarPlacementRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        altarPlacementRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        ProductEntity batHuong = productRepository.findBySlug(BAT_HUONG_SLUG)
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPlacementSeeder requires ProductSeeder to have seeded '" + BAT_HUONG_SLUG + "' first"));
        List<ProductImageEntity> images = productImageRepository
                .findByProductIdOrderByPriorityAscIdAsc(batHuong.getId());
        if (images.size() < 3) {
            throw new IllegalStateException(
                    "Expected 3 images for '" + BAT_HUONG_SLUG + "', found " + images.size());
        }

        altarPlacementRepository.saveAll(List.of(
                placement(images.get(0), "assets/images/altar-customizer/bat-huong-1.png", 0.30, 0.55, 20),
                placement(images.get(1), "assets/images/altar-customizer/bat-huong-2.png", 0.50, 0.58, 22),
                placement(images.get(2), "assets/images/altar-customizer/bat-huong-3.png", 0.70, 0.55, 20)));
    }

    private AltarPlacementEntity placement(ProductImageEntity productImage, String overlayImage,
            double defaultX, double defaultY, int widthCm) {
        return AltarPlacementEntity.builder()
                .productImage(productImage)
                .overlayImage(overlayImage)
                .defaultX(defaultX)
                .defaultY(defaultY)
                .widthCm(widthCm)
                .scaleAdjust(1.0)
                .zIndexOverride(null) // auto-z from defaultY — intentional, see class javadoc.
                .flippable(true)
                .build();
    }
}
