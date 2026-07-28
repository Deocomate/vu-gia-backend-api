package vn.springboot.seed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.entity.altar.AltarModelEntity;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.altar.AltarPresetEntity;
import vn.springboot.entity.altar.AltarPresetItemEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.repository.AltarItemGroupRepository;
import vn.springboot.repository.AltarModelRepository;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.AltarPresetItemRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.BannerRepository;
import vn.springboot.repository.CouponRepository;
import vn.springboot.repository.FaqRepository;
import vn.springboot.repository.GalleryImageRepository;
import vn.springboot.repository.NewsCategoryRepository;
import vn.springboot.repository.NewsRepository;
import vn.springboot.repository.PageRepository;
import vn.springboot.repository.ProductCategoryRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.ShippingMethodRepository;
import vn.springboot.repository.ShowroomRepository;
import vn.springboot.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * First automated test in this repo's {@code src/test/java} tree (this repo has unit
 * and {@code @WebMvcTest} controller tests, but none that boot a full context against a
 * real database — {@link vn.springboot.ApplicationTests} is the only other one, and it
 * only checks {@code contextLoads()}). {@code @ActiveProfiles("development")} drives
 * {@link SeedRunner} through its full-reset-and-reseed branch on every run, against the
 * real dev MySQL from {@code docker-compose.local.yml} (not H2/Testcontainers — the
 * seed data relies on MySQL-specific JSON columns and this project has no existing
 * test-DB tooling to build on). Requires that MySQL to be reachable via {@code DB_URL}/
 * {@code DB_PASSWORD} (see {@code application.yaml} for the defaults), same as running
 * the app itself.
 *
 * <p>Phase 6 extends this class with the 5 new altar-customizer domains: row-count
 * assertions, a double-reseed test (re-invokes {@link SeedRunner} against the already-live
 * context — the closest available proxy in a JUnit test for "restart the process twice"),
 * and an explicit, reviewable null audit across every new table.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("development")
// app.jwt.secret / app.security.cors.allowed-origins have no default in application.yaml
// (Phase 1 security hardening — fail-closed, required env vars in real deployments).
@TestPropertySource(properties = {
        "app.jwt.secret=YcgS5HZmw4ViK7gfWyIpDHORS5Mytm96TnSWhtH7zGh5efncqiVoqOXgFLybSnbH+mDUMGtZYF8FIHMkmA4L+g==",
        "app.security.cors.allowed-origins=http://localhost:5173"
})
class SeedRunnerIntegrationTest {

    @Autowired
    private ShippingMethodRepository shippingMethodRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private NewsCategoryRepository newsCategoryRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private BannerRepository bannerRepository;
    @Autowired
    private ShowroomRepository showroomRepository;
    @Autowired
    private GalleryImageRepository galleryImageRepository;
    @Autowired
    private FaqRepository faqRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private AltarItemGroupRepository altarItemGroupRepository;
    @Autowired
    private AltarStyleRepository altarStyleRepository;
    @Autowired
    private AltarModelRepository altarModelRepository;
    @Autowired
    private AltarModelSizeRepository altarModelSizeRepository;
    @Autowired
    private AltarPlacementRepository altarPlacementRepository;
    @Autowired
    private AltarPresetRepository altarPresetRepository;
    @Autowired
    private AltarPresetItemRepository altarPresetItemRepository;

    @Autowired
    private OrphanReferenceChecker orphanReferenceChecker;
    @Autowired
    private SeedRunner seedRunner;

    @Test
    void seedsEveryDomainWithRowCountsMatchingTheSourceSeedFile() {
        assertThat(shippingMethodRepository.count()).isEqualTo(2);
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(productCategoryRepository.count()).isEqualTo(6);
        // 12 original + 9 altar-set (6 altar products + 3 accessories, Phase 6 decision D6).
        assertThat(productRepository.count()).isEqualTo(21);
        // 15 original + 11 altar-set (3 for "Bát hương 20cm" + 1 each for the other 8 rows).
        assertThat(productImageRepository.count()).isEqualTo(26);
        assertThat(newsCategoryRepository.count()).isEqualTo(3);
        assertThat(newsRepository.count()).isEqualTo(22);
        assertThat(bannerRepository.count()).isEqualTo(6);
        assertThat(showroomRepository.count()).isEqualTo(1);
        assertThat(galleryImageRepository.count()).isEqualTo(5);
        assertThat(faqRepository.count()).isEqualTo(19);
        assertThat(pageRepository.count()).isEqualTo(7);
        assertThat(couponRepository.count()).isEqualTo(3);

        assertThat(altarItemGroupRepository.count()).isEqualTo(6);
        assertThat(altarStyleRepository.count()).isEqualTo(5);
        assertThat(altarModelRepository.count()).isEqualTo(1);
        assertThat(altarModelSizeRepository.count()).isEqualTo(3);
        assertThat(altarPlacementRepository.count()).isEqualTo(3);
        assertThat(altarPresetRepository.count()).isEqualTo(1);
        assertThat(altarPresetItemRepository.count()).isEqualTo(4);
    }

    @Test
    void seededAdminHasCanonicalIdentity() {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getEmail()).isEqualTo("admin@gomvugia.vn");
        assertThat(admin.getRole().name()).isEqualTo("ADMIN");
    }

    @Test
    void seededSuperadminHasCanonicalIdentity() {
        var superadmin = userRepository.findByUsername("superadmin").orElseThrow();
        assertThat(superadmin.getEmail()).isEqualTo("superadmin@gomvugia.vn");
        assertThat(superadmin.getRole().name()).isEqualTo("SUPERADMIN");
    }

    @Test
    void orphanReferenceCheckerFindsNoOrphansAfterASeedRun() {
        assertThatCode(() -> orphanReferenceChecker.check()).doesNotThrowAnyException();
    }

    /**
     * Re-invokes {@link SeedRunner#run} twice more against the already-live context (which
     * itself booted through one full reseed cycle already), the closest available proxy in a
     * JUnit test for "restart the dev process twice in a row" — {@code SeedRunner} does the
     * exact same full-wipe-then-reseed work on every {@code development}-profile boot
     * regardless of caller. Row counts and the orphan checker must both come out identical
     * every time, proving the reset+reseed cycle is idempotent-by-construction, not merely
     * accidentally correct once.
     */
    @Test
    void devProfileReseedTwiceInARowStaysConsistentAndOrphanFree() {
        seedRunner.run();
        seedRunner.run();

        assertThat(productRepository.count()).isEqualTo(21);
        assertThat(altarItemGroupRepository.count()).isEqualTo(6);
        assertThat(altarStyleRepository.count()).isEqualTo(5);
        assertThat(altarModelRepository.count()).isEqualTo(1);
        assertThat(altarModelSizeRepository.count()).isEqualTo(3);
        assertThat(altarPlacementRepository.count()).isEqualTo(3);
        assertThat(altarPresetRepository.count()).isEqualTo(1);
        assertThat(altarPresetItemRepository.count()).isEqualTo(4);
        assertThatCode(() -> orphanReferenceChecker.check()).doesNotThrowAnyException();
    }

    /**
     * Explicit, reviewable null audit across every table Phase 6 adds. Every field is
     * asserted non-null EXCEPT the whitelisted exceptions called out inline below — each one
     * matches a javadoc'd intentional null in the seeder that wrote it
     * ({@link AltarPlacementSeeder}, {@link AltarPresetSeeder}, {@link ProductSeeder}).
     */
    @Test
    void altarSeedDataHasNoUnexpectedNulls() {
        List<AltarItemGroupEntity> groups = altarItemGroupRepository.findAll();
        assertThat(groups).hasSize(6);
        groups.forEach(g -> {
            assertThat(g.getName()).isNotBlank();
            assertThat(g.getSlug()).isNotBlank();
            assertThat(g.getThumb()).isNotBlank();
            assertThat(g.getPriority()).isNotNull();
        });

        List<AltarStyleEntity> styles = altarStyleRepository.findAll();
        assertThat(styles).hasSize(5);
        styles.forEach(s -> {
            assertThat(s.getName()).isNotBlank();
            assertThat(s.getSlug()).isNotBlank();
            assertThat(s.getThumb()).isNotBlank();
            assertThat(s.getDescription()).isNotBlank();
            assertThat(s.getPriority()).isNotNull();
        });

        List<AltarModelEntity> models = altarModelRepository.findAll();
        assertThat(models).hasSize(1);
        models.forEach(m -> {
            assertThat(m.getName()).isNotBlank();
            assertThat(m.getSlug()).isNotBlank();
            assertThat(m.getThumb()).isNotBlank();
            assertThat(m.getDescription()).isNotBlank();
        });

        List<AltarModelSizeEntity> sizes = altarModelSizeRepository.findAll();
        assertThat(sizes).hasSize(3);
        sizes.forEach(s -> {
            assertThat(s.getAltarModel()).isNotNull();
            assertThat(s.getLabel()).isNotBlank();
            assertThat(s.getWidthCm()).isNotNull();
            assertThat(s.getDepthCm()).isNotNull();
            assertThat(s.getBackgroundImage()).isNotBlank();
            assertThat(s.getSurfaceLeft()).isNotNull();
            assertThat(s.getSurfaceTop()).isNotNull();
            assertThat(s.getSurfaceRight()).isNotNull();
            assertThat(s.getSurfaceBottom()).isNotNull();
            assertThat(s.getSurfaceWidthCm()).isNotNull();
        });

        List<AltarPlacementEntity> placements = altarPlacementRepository.findAll();
        assertThat(placements).hasSize(3);
        placements.forEach(p -> {
            assertThat(p.getProductImage()).isNotNull();
            assertThat(p.getOverlayImage()).isNotBlank();
            assertThat(p.getDefaultX()).isNotNull();
            assertThat(p.getDefaultY()).isNotNull();
            assertThat(p.getWidthCm()).isNotNull();
            assertThat(p.getScaleAdjust()).isNotNull();
            // WHITELISTED: null = auto-z from defaultY, the seeded intent for every placement.
            assertThat(p.getZIndexOverride()).isNull();
        });

        List<AltarPresetEntity> presets = altarPresetRepository.findAll();
        assertThat(presets).hasSize(1);
        presets.forEach(p -> {
            assertThat(p.getName()).isNotBlank();
            assertThat(p.getSlug()).isNotBlank();
            assertThat(p.getThumb()).isNotBlank();
            assertThat(p.getDescription()).isNotBlank();
            assertThat(p.getAltarModelSize()).isNotNull();
            assertThat(p.getAltarStyle()).isNotNull();
        });

        List<AltarPresetItemEntity> items = altarPresetItemRepository.findAll();
        assertThat(items).hasSize(4);
        items.forEach(i -> {
            assertThat(i.getPreset()).isNotNull();
            assertThat(i.getProduct()).isNotNull();
            assertThat(i.getQuantity()).isNotNull();
            assertThat(i.getScaleAdjust()).isNotNull();
            assertThat(i.getSortOrder()).isNotNull();
            // WHITELISTED: null = auto-z, same convention as AltarPlacementEntity.
            assertThat(i.getZIndexOverride()).isNull();
            if (i.getProductImage() != null) {
                assertThat(i.getX()).isNotNull();
                assertThat(i.getY()).isNotNull();
            } else {
                // WHITELISTED: accessory item (productImage null) never carries a canvas
                // anchor — the Phase 3 invariant "productImage != null <=> x/y set".
                assertThat(i.getX()).isNull();
                assertThat(i.getY()).isNull();
            }
        });

        // products.altar_item_group_id/altar_style_id: WHITELISTED nulls are the 3 non-ceramic
        // accessory products (glaze style doesn't apply to them, see ProductSeeder javadoc);
        // every other altar-set product has both set.
        List<ProductEntity> accessories = List.of(
                productRepository.findBySlug("tro-nep").orElseThrow(),
                productRepository.findBySlug("cot-bat-huong").orElseThrow(),
                productRepository.findBySlug("bo-that-thao").orElseThrow());
        accessories.forEach(p -> {
            assertThat(p.getAltarItemGroup()).isNotNull();
            assertThat(p.getAltarStyle()).isNull(); // WHITELISTED.
        });

        List<String> altarProductSlugs = List.of(
                "bat-huong-20cm", "lo-hoa-28cm", "mam-bong-24cm", "ky-5-chen", "choe-18cm", "ong-huong-31cm");
        altarProductSlugs.forEach(slug -> {
            ProductEntity p = productRepository.findBySlug(slug).orElseThrow();
            assertThat(p.getAltarItemGroup()).isNotNull();
            assertThat(p.getAltarStyle()).isNotNull();
        });
    }
}
