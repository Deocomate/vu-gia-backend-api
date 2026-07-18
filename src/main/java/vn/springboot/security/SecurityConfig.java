package vn.springboot.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.springboot.security.jwt.JwtAuthenticationFilter;

/**
 * Central Spring Security configuration: stateless JWT, method-level security,
 * and unified JSON error responses for auth failures.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/google",
            // Public coupon preview (apply code in cart); other coupon endpoints are admin-only
            "/api/coupons/validate",
            // Payment gateway webhooks (authenticated by signature, not JWT)
            "/api/webhooks/**",
            // API docs / Swagger UI
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            // Actuator health probe
            "/actuator/health",
            "/actuator/health/**",
            // Uploaded files served from local disk (public reads, no JWT)
            "/files/**"
    };

    /** GET-only public storefront endpoints (product catalog reads). */
    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/products/**",
            "/api/product-categories/**",
            "/api/banners/**",
            "/api/showrooms/**",
            "/api/gallery-images/**",
            "/api/faqs/**",
            "/api/redirects/**",
            "/api/news/**",
            "/api/news-categories/**",
            "/api/pages/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Public storefront reads; writes on the same paths stay protected
                        // and are further guarded by @PreAuthorize on the handlers.
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        // Public newsletter subscribe; managing subscribers stays admin-only
                        .requestMatchers(HttpMethod.POST, "/api/newsletter-subscribers").permitAll()
                        // Public contact form submit; managing requests stays admin-only
                        .requestMatchers(HttpMethod.POST, "/api/contact-requests").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
