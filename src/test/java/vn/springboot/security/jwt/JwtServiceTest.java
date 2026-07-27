package vn.springboot.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import vn.springboot.security.JwtProperties;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 1 security hardening: JwtService signs with HS512 (see class javadoc + this project's
 * README), which JJWT requires a minimum 64-byte (512-bit) key for. The constructor must refuse
 * to build with a blank secret or one shorter than that, instead of silently accepting a weak
 * key (or delegating to JJWT's own, less descriptive {@code WeakKeyException}).
 */
class JwtServiceTest {

    private static final int HS512_MIN_BYTES = 64;

    private JwtProperties propertiesWithSecret(String base64Secret) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(base64Secret);
        properties.setIssuer("vu-gia-test");
        properties.setAccessTokenExpiration(3_600_000L);
        properties.setRefreshTokenExpiration(604_800_000L);
        return properties;
    }

    private String base64Of(int byteLength) {
        byte[] key = new byte[byteLength];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    @Test
    void constructor_throwsWhenSecretBlank() {
        JwtProperties properties = propertiesWithSecret("");
        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_JWT_SECRET");
    }

    @Test
    void constructor_throwsWhenSecretNull() {
        JwtProperties properties = propertiesWithSecret(null);
        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_JWT_SECRET");
    }

    @Test
    void constructor_throwsWhenSecretShorterThanHs512Minimum() {
        // 32 bytes decodes fine as Base64 but is well under HS512's 64-byte minimum.
        JwtProperties properties = propertiesWithSecret(base64Of(32));
        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("64")
                .hasMessageContaining("HS512");
    }

    @Test
    void constructor_succeedsAndTokenRoundTrips_whenSecretMeetsHs512Minimum() {
        JwtProperties properties = propertiesWithSecret(base64Of(HS512_MIN_BYTES));
        JwtService jwtService = new JwtService(properties);

        UserDetails userDetails = new User("john", "n/a", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("john");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }
}
