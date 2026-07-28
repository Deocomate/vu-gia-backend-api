package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.entity.altar.AltarPresetEntity;
import vn.springboot.entity.altar.AltarPresetItemEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarPresetItemRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;

import java.util.List;

/**
 * Seeds one admin-authored preset ("Bộ tiêu chuẩn"): the single placeable canvas item
 * ("Bát hương 20cm", using the same product image {@link AltarPlacementSeeder} attached a
 * real placement to) plus the 3 accessory products, tied to the 153cm
 * {@link vn.springboot.entity.altar.AltarModelSizeEntity} and the "Men lam" style.
 *
 * <p>Runs after {@link AltarPlacementSeeder} in {@link SeedRunner}'s FK-safe order — not a
 * hard FK dependency (the preset item references the product image directly, not the
 * placement row), but the correct conceptual order: a canvas preset item is only meaningful
 * once its image has a real placement to size/position it from.
 *
 * <p>Null audit: every field is populated except the 3 accessory items'
 * {@code productImage}/{@code x}/{@code y} (the documented Phase 3 invariant —
 * {@code productImage != null <=> x/y set}, enforced at the service layer, not the entity)
 * and {@code zIndexOverride} on every item (auto-z from {@code y}, same convention as
 * {@link vn.springboot.entity.altar.AltarPlacementEntity}).
 */
@Component
@RequiredArgsConstructor
public class AltarPresetSeeder implements DomainSeeder {

    private static final String PRESET_MODEL_SIZE_LABEL = "153 cm";
    private static final String PRESET_STYLE_SLUG = "men-lam";
    private static final String BAT_HUONG_SLUG = "bat-huong-20cm";
    private static final String TRO_NEP_SLUG = "tro-nep";
    private static final String COT_BAT_HUONG_SLUG = "cot-bat-huong";
    private static final String BO_THAT_THAO_SLUG = "bo-that-thao";

    private final AltarPresetRepository altarPresetRepository;
    private final AltarPresetItemRepository altarPresetItemRepository;
    private final AltarModelSizeRepository altarModelSizeRepository;
    private final AltarStyleRepository altarStyleRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public boolean isEmpty() {
        return altarPresetRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        altarPresetItemRepository.deleteAllInBatch();
        altarPresetRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        AltarModelSizeEntity targetSize = altarModelSizeRepository.findAll().stream()
                .filter(size -> PRESET_MODEL_SIZE_LABEL.equals(size.getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires AltarModelSeeder to have seeded the '"
                                + PRESET_MODEL_SIZE_LABEL + "' size first"));
        AltarStyleEntity style = altarStyleRepository.findAll().stream()
                .filter(s -> PRESET_STYLE_SLUG.equals(s.getSlug()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires AltarStyleSeeder to have seeded '" + PRESET_STYLE_SLUG + "' first"));

        ProductEntity batHuong = requireProduct(BAT_HUONG_SLUG);
        ProductImageEntity batHuongImage = productImageRepository
                .findByProductIdOrderByPriorityAscIdAsc(batHuong.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires ProductSeeder to have seeded images for '" + BAT_HUONG_SLUG + "'"));

        ProductEntity troNep = requireProduct(TRO_NEP_SLUG);
        ProductEntity cotBatHuong = requireProduct(COT_BAT_HUONG_SLUG);
        ProductEntity boThatThao = requireProduct(BO_THAT_THAO_SLUG);

        AltarPresetEntity preset = altarPresetRepository.save(AltarPresetEntity.builder()
                .name("Bộ tiêu chuẩn")
                .slug("bo-tieu-chuan")
                .thumb("assets/images/altar-customizer/altar-preview.png")
                .description("Bộ gợi ý tiêu chuẩn cho bàn thờ gia tiên 153cm: bát hương chính giữa cùng tro nếp, cốt bát hương và thất thảo đi kèm.")
                .altarModelSize(targetSize)
                .altarStyle(style)
                .priority(1)
                .isActive(true)
                .build());

        altarPresetItemRepository.saveAll(List.of(
                // Canvas item: the only placeable altar-set product, same anchor as its AltarPlacementSeeder default.
                AltarPresetItemEntity.builder()
                        .preset(preset)
                        .product(batHuong)
                        .productImage(batHuongImage)
                        .quantity(1)
                        .x(0.30)
                        .y(0.55)
                        .scaleAdjust(1.0)
                        .flipped(false)
                        .zIndexOverride(null) // auto-z from y — intentional, see class javadoc.
                        .sortOrder(0)
                        .build(),
                // Accessory items: summary-only, no canvas position (Phase 3 invariant: productImage/x/y all null).
                accessoryItem(preset, troNep, 2, 1),
                accessoryItem(preset, cotBatHuong, 1, 2),
                accessoryItem(preset, boThatThao, 1, 3)));
    }

    private AltarPresetItemEntity accessoryItem(AltarPresetEntity preset, ProductEntity product, int quantity, int sortOrder) {
        return AltarPresetItemEntity.builder()
                .preset(preset)
                .product(product)
                .productImage(null) // WHITELISTED: accessory item, never rendered on canvas.
                .quantity(quantity)
                .x(null) // WHITELISTED: null iff productImage is null (Phase 3 invariant).
                .y(null) // WHITELISTED: null iff productImage is null (Phase 3 invariant).
                .scaleAdjust(1.0)
                .flipped(false)
                .zIndexOverride(null) // WHITELISTED: auto-z convention, no canvas render anyway.
                .sortOrder(sortOrder)
                .build();
    }

    private ProductEntity requireProduct(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires ProductSeeder to have seeded '" + slug + "' first"));
    }
}
