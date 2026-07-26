package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrates every {@link DomainSeeder} in a fixed, explicit, FK-safe order —
 * deliberately not a Spring-injected {@code List<DomainSeeder>}, since insert/delete
 * order here is correctness-critical and Spring's bean-list ordering isn't something to
 * depend on for that.
 *
 * <p>{@code APP_ENV=development}: every boot fully wipes (children-first) and re-seeds
 * all 12 catalog domains, plus clears the 6 non-seeded transactional tables via
 * {@link TransactionalDataCleaner} — the dev DB is treated as fully disposable.
 * Any other profile: each domain seeds only if empty (idempotent bootstrap), and the
 * transactional tables are never touched.
 *
 * <p>{@link OrphanReferenceChecker} runs unconditionally afterward, in every profile —
 * see its own javadoc for why that isn't dev-gated.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedRunner implements CommandLineRunner {

    private final Environment environment;

    private final ShippingMethodSeeder shippingMethodSeeder;
    private final UserSeeder userSeeder;
    private final ProductCategorySeeder productCategorySeeder;
    private final ProductSeeder productSeeder;
    private final NewsCategorySeeder newsCategorySeeder;
    private final NewsSeeder newsSeeder;
    private final BannerSeeder bannerSeeder;
    private final ShowroomSeeder showroomSeeder;
    private final GalleryImageSeeder galleryImageSeeder;
    private final FaqSeeder faqSeeder;
    private final PageSeeder pageSeeder;
    private final CouponSeeder couponSeeder;

    private final TransactionalDataCleaner transactionalDataCleaner;
    private final OrphanReferenceChecker orphanReferenceChecker;

    @Override
    public void run(String... args) {
        if (environment.matchesProfiles("development")) {
            reseedEverything();
        } else {
            seedEmptyDomainsOnly();
        }
        orphanReferenceChecker.check();
    }

    private List<DomainSeeder> seedersInFkSafeOrder() {
        return List.of(shippingMethodSeeder, userSeeder, productCategorySeeder, productSeeder,
                newsCategorySeeder, newsSeeder, bannerSeeder, showroomSeeder,
                galleryImageSeeder, faqSeeder, pageSeeder, couponSeeder);
    }

    private void reseedEverything() {
        log.info("APP_ENV=development: resetting and reseeding every catalog domain and transactional table");
        transactionalDataCleaner.reset();

        List<DomainSeeder> order = seedersInFkSafeOrder();
        for (int i = order.size() - 1; i >= 0; i--) {
            order.get(i).reset();
        }
        for (DomainSeeder seeder : order) {
            seeder.seed();
        }
    }

    private void seedEmptyDomainsOnly() {
        for (DomainSeeder seeder : seedersInFkSafeOrder()) {
            if (seeder.isEmpty()) {
                seeder.seed();
            }
        }
    }
}
