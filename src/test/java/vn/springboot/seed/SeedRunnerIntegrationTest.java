package vn.springboot.seed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("development")
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
    private OrphanReferenceChecker orphanReferenceChecker;

    @Test
    void seedsEveryDomainWithRowCountsMatchingTheSourceSeedFile() {
        assertThat(shippingMethodRepository.count()).isEqualTo(2);
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(productCategoryRepository.count()).isEqualTo(6);
        assertThat(productRepository.count()).isEqualTo(12);
        assertThat(productImageRepository.count()).isEqualTo(15);
        assertThat(newsCategoryRepository.count()).isEqualTo(3);
        assertThat(newsRepository.count()).isEqualTo(22);
        assertThat(bannerRepository.count()).isEqualTo(6);
        assertThat(showroomRepository.count()).isEqualTo(1);
        assertThat(galleryImageRepository.count()).isEqualTo(5);
        assertThat(faqRepository.count()).isEqualTo(19);
        assertThat(pageRepository.count()).isEqualTo(7);
        assertThat(couponRepository.count()).isEqualTo(3);
    }

    @Test
    void seededAdminHasCanonicalIdentity() {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getEmail()).isEqualTo("admin@gomvugia.vn");
        assertThat(admin.getRole().name()).isEqualTo("ADMIN");
    }

    @Test
    void orphanReferenceCheckerFindsNoOrphansAfterASeedRun() {
        assertThatCode(() -> orphanReferenceChecker.check()).doesNotThrowAnyException();
    }
}
