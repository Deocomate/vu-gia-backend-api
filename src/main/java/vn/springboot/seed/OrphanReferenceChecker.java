package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
 */
@Component
@RequiredArgsConstructor
public class OrphanReferenceChecker {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final NewsRepository newsRepository;

    public void check() {
        long orphanedProductImages = productImageRepository.countOrphaned();
        long productsWithOrphanedCategory = productRepository.countWithOrphanedCategory();
        long newsWithOrphanedCategory = newsRepository.countWithOrphanedCategory();

        if (orphanedProductImages > 0 || productsWithOrphanedCategory > 0 || newsWithOrphanedCategory > 0) {
            throw new IllegalStateException(
                    "Orphan reference check failed after seeding: product_images.product_id orphans=%d, products.product_category_id orphans=%d, news.news_category_id orphans=%d"
                            .formatted(orphanedProductImages, productsWithOrphanedCategory, newsWithOrphanedCategory));
        }
    }
}
