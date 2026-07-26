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
 * Ported 1:1 from {@code V2__seed_db.sql} "USERS" section (1 admin row). Replaces
 * {@code DataInitializer} — same idempotent-by-username check, same {@code @Value}
 * overrides, but seeds {@code role=ADMIN} (the real seeded value) instead of the old
 * dead-code default of {@code SUPERADMIN}, which never actually ran once the SQL seed
 * had already inserted the "admin" username first.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder implements DomainSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin-username:admin}")
    private String adminUsername;

    @Value("${app.init.admin-email:admin@gomvugia.vn}")
    private String adminEmail;

    @Value("${app.init.admin-password:admin123}")
    private String adminPassword;

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
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
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
