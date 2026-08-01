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
 * Seeds 25 real altar placements — one per ceramic product in the altar catalog (decision D7:
 * every ceramic product is a standalone transparent cutout, so every one is placeable; the 3
 * non-ceramic accessories never get a placement, same as before). Runs after {@link ProductSeeder}
 * (needs the generated {@code product_image_id} for each product's {@code priority = 0} image)
 * and resolves it via {@link ProductImageRepository#findByProductIdOrderByPriorityAscIdAsc(Long)}
 * — never a literal id — taking the first (lowest-priority) image, which is always that
 * product's {@code 01.png} overlay (decision D5).
 *
 * <p>{@code defaultX/defaultY} place items within {@link AltarModelSeeder}'s re-measured
 * tabletop band: {@code defaultY} is depth (0 = back edge, 1 = front edge), auto-z derives from
 * it so back items correctly render behind front items. Tall items (lọ hoa, ống hương, hạc) sit
 * back; low items (kỷ chén, đĩa) sit front; bát hương takes the centre.
 *
 * <p>No optional/nullable columns are left null on any row here except {@code zIndexOverride},
 * which is intentionally {@code null} on every placement (auto-z from {@code defaultY} is the
 * seeded intent — see {@link AltarPlacementEntity#getZIndexOverride()}).
 */
@Component
@RequiredArgsConstructor
public class AltarPlacementSeeder implements DomainSeeder {

    /** One placement row: which product, its real width, and where it sits on the tabletop band. */
    private record PlacementSeed(String slug, int widthCm, double defaultX, double defaultY) {
    }

    private static final List<PlacementSeed> PLACEMENTS = List.of(
            new PlacementSeed("bat-huong-men-lam-ve-rong-h20", 20, 0.50, 0.62),
            new PlacementSeed("lo-hoa-men-lam-h35", 16, 0.13, 0.20),
            new PlacementSeed("lo-hoa-men-lam-h30", 14, 0.87, 0.20),
            new PlacementSeed("lo-hoa-men-lam-h20", 10, 0.20, 0.42),
            new PlacementSeed("choe-tho-men-lam-h19", 13, 0.30, 0.18),
            new PlacementSeed("choe-tho-men-lam-h14", 10, 0.70, 0.18),
            new PlacementSeed("bat-sam-men-lam-ve-rong-phuong", 16, 0.50, 0.22),
            new PlacementSeed("bat-sam-men-lam-co-nap", 15, 0.38, 0.30),
            new PlacementSeed("bat-tho-men-lam-co-nap", 12, 0.62, 0.30),
            new PlacementSeed("bat-tho-men-lam-nho", 10, 0.72, 0.40),
            new PlacementSeed("nam-ruou-men-lam-h25", 9, 0.42, 0.46),
            new PlacementSeed("nam-ruou-men-lam-h20", 8, 0.58, 0.46),
            new PlacementSeed("ky-5-chen-men-lam-de-rong", 38, 0.50, 0.92),
            new PlacementSeed("ky-3-chen-men-lam-de-rong", 26, 0.24, 0.86),
            new PlacementSeed("chen-tho-men-lam-bo-3", 14, 0.76, 0.86),
            new PlacementSeed("bo-nam-ruou-ky-chen-men-lam", 24, 0.34, 0.72),
            new PlacementSeed("den-dau-tho-men-lam-h28", 9, 0.16, 0.60),
            new PlacementSeed("den-dau-tho-men-lam-ve-phuong", 9, 0.84, 0.60),
            new PlacementSeed("den-dau-tho-men-lam-doi", 20, 0.28, 0.52),
            new PlacementSeed("ong-huong-men-lam-h31", 13, 0.09, 0.34),
            new PlacementSeed("ong-huong-men-lam-h25", 11, 0.91, 0.34),
            new PlacementSeed("dia-tho-men-lam-d20", 20, 0.66, 0.72),
            new PlacementSeed("bo-nam-ruou-ky-chen-men-lam-ve-vang", 24, 0.50, 0.78),
            new PlacementSeed("nam-ruou-men-lam-ve-vang-h28", 11, 0.62, 0.52),
            new PlacementSeed("doi-hac-tho-men-lam-ve-vang", 30, 0.50, 0.10));

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
        List<AltarPlacementEntity> placements = PLACEMENTS.stream().map(this::toPlacement).toList();
        altarPlacementRepository.saveAll(placements);
    }

    private AltarPlacementEntity toPlacement(PlacementSeed seed) {
        ProductEntity product = productRepository.findBySlug(seed.slug())
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPlacementSeeder requires ProductSeeder to have seeded '" + seed.slug() + "' first"));
        List<ProductImageEntity> images = productImageRepository
                .findByProductIdOrderByPriorityAscIdAsc(product.getId());
        if (images.isEmpty()) {
            throw new IllegalStateException("Expected at least 1 image for '" + seed.slug() + "', found 0");
        }
        ProductImageEntity overlayImage = images.get(0);

        return AltarPlacementEntity.builder()
                .productImage(overlayImage)
                .overlayImage(overlayImage.getUrl())
                .defaultX(seed.defaultX())
                .defaultY(seed.defaultY())
                .widthCm(seed.widthCm())
                .scaleAdjust(1.0)
                .zIndexOverride(null) // auto-z from defaultY — intentional, see class javadoc.
                .flippable(true)
                .build();
    }
}
