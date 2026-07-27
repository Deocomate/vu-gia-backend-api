package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.setting.SiteSettingEntity;
import vn.springboot.repository.SiteSettingRepository;

/**
 * Guarantees the singleton {@code site_setting} row (id=1, {@code cartEnabled=true})
 * exists before any traffic is served. Runs standalone (not through {@link SeedRunner},
 * which orders catalog-domain seeders by FK dependency) since this table has no FK
 * relationships, so run order relative to it doesn't matter.
 *
 * <p>Deliberately a startup seeder rather than a lazy {@code findById(1L).orElseGet(create)}
 * read-path fallback: {@code BaseEntity} uses {@code GenerationType.IDENTITY} with no
 * uniqueness guard, so concurrent first-time anonymous reads could otherwise both miss and
 * both insert, risking the admin's write silently landing on an orphaned second row.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SiteSettingSeeder implements ApplicationRunner {

    private final SiteSettingRepository siteSettingRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (siteSettingRepository.count() > 0) {
            return;
        }
        siteSettingRepository.save(SiteSettingEntity.builder()
                .cartEnabled(true)
                .build());
        log.info("Seeded default site_setting row (cartEnabled=true)");
    }
}
