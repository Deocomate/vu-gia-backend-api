package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.news.NewsCategoryEntity;
import vn.springboot.repository.NewsCategoryRepository;

import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "NEWS CATEGORIES" section (3 rows). */
@Component
@RequiredArgsConstructor
public class NewsCategorySeeder implements DomainSeeder {

    private final NewsCategoryRepository newsCategoryRepository;

    @Override
    public boolean isEmpty() {
        return newsCategoryRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        newsCategoryRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        newsCategoryRepository.saveAll(List.of(
                NewsCategoryEntity.builder().name("Kiến thức phong thuỷ").slug("phong-thuy").priority(1).build(),
                NewsCategoryEntity.builder().name("Kiến thức sản phẩm").slug("san-pham").priority(2).build(),
                NewsCategoryEntity.builder().name("Cẩm nang làng nghề").slug("lang-nghe").priority(3).build()));
    }
}
