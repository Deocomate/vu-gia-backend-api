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

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds 3 real admin-authored presets spanning altar size, price tier and glaze style, so the
 * customizer's preset picker demonstrates meaningfully different starting arrangements instead
 * of one bát hương plus three invisible accessories.
 *
 * <p>Runs after {@link AltarPlacementSeeder} in {@link SeedRunner}'s FK-safe order — not a hard
 * FK dependency (a preset item references the product image directly, not the placement row),
 * but the correct conceptual order: a canvas preset item is only meaningful once its image has
 * a real placement to size/position it from. Canvas item {@code x}/{@code y} reuse each
 * product's {@link AltarPlacementSeeder} default so a preset never contradicts its own items'
 * natural resting position.
 *
 * <p>Preset 3's {@code altarStyle} is "Men lam vẽ vàng" even though 3 of its 6 canvas items are
 * plain men lam — {@code altarStyle} labels the preset's overall character, it does not
 * constrain item membership (nothing in the service or UI cross-checks the two), and only 3
 * products are men-lam-ve-vang, too sparse for a strictly single-style preset to be useful.
 *
 * <p>Null audit: every field is populated except each preset's accessory items'
 * {@code productImage}/{@code x}/{@code y} (the documented Phase 3 invariant —
 * {@code productImage != null <=> x/y set}, enforced at the service layer, not the entity)
 * and {@code zIndexOverride} on every item (auto-z from {@code y}, same convention as
 * {@link vn.springboot.entity.altar.AltarPlacementEntity}).
 */
@Component
@RequiredArgsConstructor
public class AltarPresetSeeder implements DomainSeeder {

    /** One canvas item: which product, and its position — mirrors its {@link AltarPlacementSeeder} default. */
    private record CanvasItem(String slug, double x, double y) {
    }

    /** One accessory item: which product, how many. */
    private record AccessoryItem(String slug, int quantity) {
    }

    /** One preset: copy + which altar size/style it targets + its canvas and accessory items. */
    private record PresetSeed(String name, String slug, String thumbSlug, String description,
            String sizeLabel, String styleSlug, List<CanvasItem> canvas, List<AccessoryItem> accessories) {
    }

    private static final List<PresetSeed> PRESETS = List.of(
            new PresetSeed("Bộ cơ bản 127cm", "bo-co-ban-127cm", "bat-huong-men-lam-ve-rong-h20",
                    "Bộ gợi ý cơ bản cho bàn thờ gia tiên 127cm: bát hương, đôi lọ hoa, kỷ 3 chén và đĩa thờ men lam, cùng phụ kiện tro nếp và cốt bát hương đi kèm.",
                    "127 cm", "men-lam",
                    List.of(
                            new CanvasItem("bat-huong-men-lam-ve-rong-h20", 0.50, 0.62),
                            new CanvasItem("lo-hoa-men-lam-h30", 0.87, 0.20),
                            new CanvasItem("lo-hoa-men-lam-h20", 0.20, 0.42),
                            new CanvasItem("ky-3-chen-men-lam-de-rong", 0.24, 0.86),
                            new CanvasItem("dia-tho-men-lam-d20", 0.66, 0.72)),
                    List.of(
                            new AccessoryItem("tro-nep", 2),
                            new AccessoryItem("cot-bat-huong", 1))),
            new PresetSeed("Bộ đầy đủ men lam 153cm", "bo-day-du-men-lam-153cm", "lo-hoa-men-lam-h35",
                    "Bộ gợi ý đầy đủ cho bàn thờ gia tiên 153cm: bát hương, đôi lọ hoa, đôi chóe thờ, bát sâm, kỷ 5 chén, ống hương và đôi đèn dầu men lam, cùng đủ 3 loại phụ kiện đi kèm.",
                    "153 cm", "men-lam",
                    List.of(
                            new CanvasItem("bat-huong-men-lam-ve-rong-h20", 0.50, 0.62),
                            new CanvasItem("lo-hoa-men-lam-h35", 0.13, 0.20),
                            new CanvasItem("lo-hoa-men-lam-h30", 0.87, 0.20),
                            new CanvasItem("choe-tho-men-lam-h19", 0.30, 0.18),
                            new CanvasItem("choe-tho-men-lam-h14", 0.70, 0.18),
                            new CanvasItem("bat-sam-men-lam-ve-rong-phuong", 0.50, 0.22),
                            new CanvasItem("ky-5-chen-men-lam-de-rong", 0.50, 0.92),
                            new CanvasItem("ong-huong-men-lam-h31", 0.09, 0.34),
                            new CanvasItem("den-dau-tho-men-lam-doi", 0.28, 0.52)),
                    List.of(
                            new AccessoryItem("tro-nep", 3),
                            new AccessoryItem("cot-bat-huong", 1),
                            new AccessoryItem("bo-that-thao", 1))),
            new PresetSeed("Bộ cao cấp vẽ vàng 175cm", "bo-cao-cap-ve-vang-175cm", "doi-hac-tho-men-lam-ve-vang",
                    "Bộ gợi ý cao cấp cho bàn thờ gia tiên 175cm: bát hương, đôi hạc thờ, bộ nậm rượu & kỷ chén vẽ vàng, nậm rượu vẽ vàng, lọ hoa và bát sâm men lam, tôn lên vẻ sang trọng.",
                    "175 cm", "men-lam-ve-vang",
                    List.of(
                            new CanvasItem("bat-huong-men-lam-ve-rong-h20", 0.50, 0.62),
                            new CanvasItem("doi-hac-tho-men-lam-ve-vang", 0.50, 0.10),
                            new CanvasItem("bo-nam-ruou-ky-chen-men-lam-ve-vang", 0.50, 0.78),
                            new CanvasItem("nam-ruou-men-lam-ve-vang-h28", 0.62, 0.52),
                            new CanvasItem("lo-hoa-men-lam-h35", 0.13, 0.20),
                            new CanvasItem("bat-sam-men-lam-co-nap", 0.38, 0.30)),
                    List.of(
                            new AccessoryItem("tro-nep", 3),
                            new AccessoryItem("cot-bat-huong", 1),
                            new AccessoryItem("bo-that-thao", 1))));

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
        List<AltarPresetItemEntity> allItems = new ArrayList<>();
        for (int priority = 0; priority < PRESETS.size(); priority++) {
            PresetSeed presetSeed = PRESETS.get(priority);
            AltarPresetEntity preset = altarPresetRepository.save(buildPreset(presetSeed, priority + 1));
            allItems.addAll(buildItems(preset, presetSeed));
        }
        altarPresetItemRepository.saveAll(allItems);
    }

    private AltarPresetEntity buildPreset(PresetSeed presetSeed, int priority) {
        AltarModelSizeEntity size = altarModelSizeRepository.findAll().stream()
                .filter(s -> presetSeed.sizeLabel().equals(s.getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires AltarModelSeeder to have seeded the '"
                                + presetSeed.sizeLabel() + "' size first"));
        AltarStyleEntity style = altarStyleRepository.findAll().stream()
                .filter(s -> presetSeed.styleSlug().equals(s.getSlug()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires AltarStyleSeeder to have seeded '"
                                + presetSeed.styleSlug() + "' first"));

        return AltarPresetEntity.builder()
                .name(presetSeed.name())
                .slug(presetSeed.slug())
                .thumb(thumbPath(presetSeed.thumbSlug()))
                .description(presetSeed.description())
                .altarModelSize(size)
                .altarStyle(style)
                .priority(priority)
                .isActive(true)
                .build();
    }

    private List<AltarPresetItemEntity> buildItems(AltarPresetEntity preset, PresetSeed presetSeed) {
        List<AltarPresetItemEntity> items = new ArrayList<>();
        int sortOrder = 0;
        for (CanvasItem canvasItem : presetSeed.canvas()) {
            items.add(canvasItem(preset, canvasItem, sortOrder++));
        }
        for (AccessoryItem accessoryItem : presetSeed.accessories()) {
            items.add(accessoryItem(preset, requireProduct(accessoryItem.slug()), accessoryItem.quantity(), sortOrder++));
        }
        return items;
    }

    private AltarPresetItemEntity canvasItem(AltarPresetEntity preset, CanvasItem canvasItem, int sortOrder) {
        ProductEntity product = requireProduct(canvasItem.slug());
        ProductImageEntity productImage = productImageRepository
                .findByProductIdOrderByPriorityAscIdAsc(product.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires ProductSeeder to have seeded images for '" + canvasItem.slug() + "'"));

        return AltarPresetItemEntity.builder()
                .preset(preset)
                .product(product)
                .productImage(productImage)
                .quantity(1)
                .x(canvasItem.x())
                .y(canvasItem.y())
                .scaleAdjust(1.0)
                .flipped(false)
                .zIndexOverride(null) // auto-z from y — intentional, see class javadoc.
                .sortOrder(sortOrder)
                .build();
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

    private String thumbPath(String slug) {
        return "assets/images/altar-customizer/products/" + slug + "/01.png";
    }

    private ProductEntity requireProduct(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalStateException(
                        "AltarPresetSeeder requires ProductSeeder to have seeded '" + slug + "' first"));
    }
}
