package vn.springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Dev-only replacement for the removed {@code spring.flyway.clean-on-validation-error}
 * property (Flyway dropped it; see FlywayValidateException below). DB in this profile is
 * disposable: a checksum mismatch drops the whole schema and re-migrates from scratch
 * instead of blocking boot. NEVER enable outside {@code development} — it destroys data.
 */
@Slf4j
@Configuration
public class FlywayDevCleanConfig {

    @Bean
    @Profile("development")
    public FlywayMigrationStrategy cleanOnValidationErrorStrategy() {
        return flyway -> {
            try {
                flyway.migrate();
            } catch (FlywayValidateException ex) {
                log.warn("Flyway validate mismatch (dev profile) - cleaning schema and re-migrating", ex);
                flyway.clean();
                flyway.migrate();
            }
        };
    }
}
