package vn.springboot.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import vn.springboot.security.jwt.JwtAuthenticationFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 1 security hardening: CORS must come from an explicit config-driven allow-list
 * (no {@code "*"} origin pattern / wildcard headers), and must refuse to build with a
 * blank {@code app.security.cors.allowed-origins}.
 */
class SecurityConfigTest {

    private SecurityConfig newConfig(String allowedOrigins) {
        SecurityConfig config = new SecurityConfig(
                Mockito.mock(JwtAuthenticationFilter.class),
                Mockito.mock(JwtAuthenticationEntryPoint.class),
                Mockito.mock(CustomAccessDeniedHandler.class),
                Mockito.mock(UserDetailsService.class));
        ReflectionTestUtils.setField(config, "allowedOrigins", allowedOrigins);
        return config;
    }

    @Test
    void corsConfigurationSource_usesExplicitAllowListNotWildcard() {
        SecurityConfig config = newConfig("http://localhost:5173, https://app.example.com");
        CorsConfigurationSource source = config.corsConfigurationSource();

        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        CorsConfiguration resolved = source.getCorsConfiguration(request);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getAllowedOrigins())
                .containsExactlyInAnyOrder("http://localhost:5173", "https://app.example.com")
                .doesNotContain("*");
        assertThat(resolved.getAllowCredentials()).isTrue();
    }

    @Test
    void corsConfigurationSource_usesExplicitHeaderListNotWildcard() {
        SecurityConfig config = newConfig("http://localhost:5173");
        CorsConfigurationSource source = config.corsConfigurationSource();

        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        CorsConfiguration resolved = source.getCorsConfiguration(request);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getAllowedHeaders())
                .containsExactlyInAnyOrder("Authorization", "Content-Type", "X-XSRF-TOKEN")
                .doesNotContain("*");
    }

    @Test
    void corsConfigurationSource_throwsWhenAllowedOriginsBlank() {
        SecurityConfig config = newConfig("");
        assertThatThrownBy(config::corsConfigurationSource)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_CORS_ALLOWED_ORIGINS");
    }

    @Test
    void corsConfigurationSource_throwsWhenAllowedOriginsOnlyCommasAndSpaces() {
        SecurityConfig config = newConfig(" , ,");
        assertThatThrownBy(config::corsConfigurationSource)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_CORS_ALLOWED_ORIGINS");
    }
}
