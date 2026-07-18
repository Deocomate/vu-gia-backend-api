package vn.springboot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Serves uploaded files from {@code app.storage.root} under
 * {@code app.storage.url-prefix}. Spring's {@code PathResourceResolver} rejects
 * {@code ../} escapes by default, so no custom traversal handling is needed here.
 *
 * <p>Also stamps {@code X-Content-Type-Options: nosniff} on those responses
 * (defense-in-depth on top of the content-type whitelist enforced at upload
 * time in {@code LocalFileStorageService}) so browsers cannot be tricked into
 * rendering a stored file as a different MIME type.
 *
 * <p>{@code @EnableConfigurationProperties} (rather than relying solely on
 * {@code @ConfigurationPropertiesScan} on {@code Application}) is required here:
 * as a {@code WebMvcConfigurer}, this class is always pulled into every
 * {@code @WebMvcTest} slice, but slice tests exclude beans registered purely via
 * {@code @ConfigurationPropertiesScan} — without this, every unrelated
 * {@code @WebMvcTest} in the app would fail to start.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class WebStorageConfig implements WebMvcConfigurer {

    private final StorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path base = Paths.get(properties.getRoot()).toAbsolutePath().normalize();
        // Must exist before being registered as a resource location: on a fresh
        // checkout (no prior upload yet) this directory doesn't exist, and an
        // absent location makes Spring silently resolve nothing under it.
        try {
            Files.createDirectories(base);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to create storage root " + base, ex);
        }

        registry.addResourceHandler(resourcePattern())
                .addResourceLocations(base.toUri().toString())
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> storageNosniffFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain chain) throws ServletException, IOException {
                response.setHeader("X-Content-Type-Options", "nosniff");
                chain.doFilter(request, response);
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns(urlPattern());
        return registration;
    }

    private String resourcePattern() {
        String prefix = properties.getUrlPrefix();
        return (prefix.endsWith("/") ? prefix : prefix + "/") + "**";
    }

    private String urlPattern() {
        String prefix = properties.getUrlPrefix();
        return (prefix.endsWith("/") ? prefix : prefix + "/") + "*";
    }
}
