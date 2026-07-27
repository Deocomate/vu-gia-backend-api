package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.repository.UserRepository;

/**
 * Ported 1:1 from {@code V2__seed_db.sql} "USERS" section (1 admin row), then extended
 * with a second, higher-privileged {@code SUPERADMIN} row — the source SQL never had
 * one, so every prior seed only ever produced an {@code ADMIN} account even though
 * {@link Role#SUPERADMIN} exists and gates several endpoints ({@code UserController}
 * account creation/role changes, {@code SiteSettingController}). Without a seeded
 * superadmin, those endpoints were unreachable on a fresh environment. Replaces
 * {@code DataInitializer} — same idempotent-by-username check, same {@code @Value}
 * overrides.
 *
 * <p>{@code app.init.enabled} is preserved as this seeder's own kill-switch (its only
 * scope is these two staff rows — other domains still seed normally when this is
 * {@code false}), matching {@code DataInitializer}'s original {@code @ConditionalOnProperty}
 * behavior. It can't be re-expressed as a bean condition here since {@link SeedRunner}
 * injects this seeder unconditionally as a concrete type.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder implements DomainSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.superadmin-username:superadmin}")
    private String superadminUsername;

    @Value("${app.init.superadmin-email:superadmin@gomvugia.vn}")
    private String superadminEmail;

    @Value("${app.init.superadmin-password:Superadmin@123}")
    private String superadminPassword;

    @Value("${app.init.admin-username:admin}")
    private String adminUsername;

    @Value("${app.init.admin-email:admin@gomvugia.vn}")
    private String adminEmail;

    @Value("${app.init.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.init.enabled:true}")
    private boolean initEnabled;

    @Override
    public boolean isEmpty() {
        return userRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        userRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        if (!initEnabled) {
            return;
        }
        if (!userRepository.existsByUsername(superadminUsername)) {
            userRepository.save(UserEntity.builder()
                    .username(superadminUsername)
                    .password(passwordEncoder.encode(superadminPassword))
                    .name("Chủ sở hữu Vũ Gia")
                    .phone("0917777247")
                    .email(superadminEmail)
                    .gender("Nam")
                    .role(Role.SUPERADMIN)
                    .build());
            log.info("Seeded superadmin user '{}' (change the default password!)", superadminUsername);
        }
        if (!userRepository.existsByUsername(adminUsername)) {
            userRepository.save(UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("Nhân viên Vũ Gia")
                    .phone("0966558808")
                    .email(adminEmail)
                    .gender("Nữ")
                    .role(Role.ADMIN)
                    .build());
            log.info("Seeded admin user '{}' (change the default password!)", adminUsername);
        }
    }
}
