package vn.springboot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.sepay.webhook.*}. The {@code secret} is the shared key used to
 * verify the {@code X-SePay-Signature} HMAC-SHA256 header. Leave it empty in dev
 * to skip verification.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.sepay.webhook")
public class SepayProperties {

    /** Shared secret for HMAC-SHA256 signature verification (empty = skip, dev only). */
    private String secret = "";
}
