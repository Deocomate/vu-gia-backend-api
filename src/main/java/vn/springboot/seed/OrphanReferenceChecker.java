package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.AltarPresetItemRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.NewsRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;

/**
 * Post-seed correctness gate. {@code V1__init_db.sql} declares zero real database-level
 * {@code FOREIGN KEY} constraints, so a seeder ordering mistake produces a silently
 * dangling reference, not a loud crash — "no exception was thrown during seeding" is
 * not proof the seed succeeded correctly. Runs after every {@link SeedRunner}
 * completion, in every profile (not dev-gated): the query cost is a handful of
 * {@code COUNT}s, and there's no DB-level safety net in any environment to catch this
 * otherwise. Throws (rather than just logging) so a real mismatch fails the boot loudly.
 *
 * <p>Phase 6 adds the 7 altar-catalog FK checks below (5 catalog tables, 2 nullable FKs
 * on {@code products}, plus {@code altar_presets.altar_style_id}). {@code altar_designs}
 * is deliberately out of scope here — it's transactional (per-user) data reset by
 * {@link TransactionalDataCleaner}, not seeded, same remit boundary as
 * {@code orders}/{@code cart_items}, which this checker has never covered either.
 */
@Component
@RequiredArgsConstructor
public class OrphanReferenceChecker {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final NewsRepository newsRepository;
    private final AltarModelSizeRepository altarModelSizeRepository;
    private final AltarPlacementRepository altarPlacementRepository;
    private final AltarPresetRepository altarPresetRepository;
    private final AltarPresetItemRepository altarPresetItemRepository;

    public void check() {
        long orphanedProductImages = productImageRepository.countOrphaned();
        long productsWithOrphanedCategory = productRepository.countWithOrphanedCategory();
        long newsWithOrphanedCategory = newsRepository.countWithOrphanedCategory();
        long productsWithOrphanedAltarItemGroup = productRepository.countWithOrphanedAltarItemGroup();
        long productsWithOrphanedAltarStyle = productRepository.countWithOrphanedAltarStyle();
        long altarModelSizesWithOrphanedModel = altarModelSizeRepository.countWithOrphanedAltarModel();
        long altarPlacementsWithOrphanedProductImage = altarPlacementRepository.countWithOrphanedProductImage();
        long altarPresetsWithOrphanedModelSize = altarPresetRepository.countWithOrphanedAltarModelSize();
        long altarPresetsWithOrphanedStyle = altarPresetRepository.countWithOrphanedAltarStyle();
        long altarPresetItemsWithOrphanedPreset = altarPresetItemRepository.countWithOrphanedPreset();
        long altarPresetItemsWithOrphanedProduct = altarPresetItemRepository.countWithOrphanedProduct();
        long altarPresetItemsWithOrphanedProductImage = altarPresetItemRepository.countWithOrphanedProductImage();

        long total = orphanedProductImages + productsWithOrphanedCategory + newsWithOrphanedCategory
                + productsWithOrphanedAltarItemGroup + productsWithOrphanedAltarStyle
                + altarModelSizesWithOrphanedModel + altarPlacementsWithOrphanedProductImage
                + altarPresetsWithOrphanedModelSize + altarPresetsWithOrphanedStyle
                + altarPresetItemsWithOrphanedPreset + altarPresetItemsWithOrphanedProduct
                + altarPresetItemsWithOrphanedProductImage;

        if (total > 0) {
            throw new IllegalStateException(
                    ("Orphan reference check failed after seeding: product_images.product_id=%d, "
                            + "products.product_category_id=%d, news.news_category_id=%d, "
                            + "products.altar_item_group_id=%d, products.altar_style_id=%d, "
                            + "altar_model_sizes.altar_model_id=%d, altar_placements.product_image_id=%d, "
                            + "altar_presets.altar_model_size_id=%d, altar_presets.altar_style_id=%d, "
                            + "altar_preset_items.preset_id=%d, altar_preset_items.product_id=%d, "
                            + "altar_preset_items.product_image_id=%d")
                            .formatted(orphanedProductImages, productsWithOrphanedCategory, newsWithOrphanedCategory,
                                    productsWithOrphanedAltarItemGroup, productsWithOrphanedAltarStyle,
                                    altarModelSizesWithOrphanedModel, altarPlacementsWithOrphanedProductImage,
                                    altarPresetsWithOrphanedModelSize, altarPresetsWithOrphanedStyle,
                                    altarPresetItemsWithOrphanedPreset, altarPresetItemsWithOrphanedProduct,
                                    altarPresetItemsWithOrphanedProductImage));
        }
    }
}
