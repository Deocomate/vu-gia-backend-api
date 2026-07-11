package vn.springboot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.repository.UserRepository;

/**
 * Seeds a single super-admin account on startup. Idempotent: does nothing if the
 * account already exists. Toggle with {@code app.init.enabled=false}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.init.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin-username:admin}")
    private String adminUsername;

    @Value("${app.init.admin-email:admin@springboot.vn}")
    private String adminEmail;

    @Value("${app.init.admin-password:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        userRepository.save(UserEntity.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .name("Administrator")
                .role(Role.SUPERADMIN)
                .build());
        log.info("Seeded super-admin user '{}' (change the default password!)", adminUsername);
    }
}
