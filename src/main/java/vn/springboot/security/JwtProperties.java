package vn.springboot.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code app.jwt.*} configuration block.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Base64-encoded signing secret (>= 512 bits recommended for HS512). */
    private String secret;

    private String issuer = "vu-gia";

    /** Access token lifetime in milliseconds. */
    private long accessTokenExpiration = 3_600_000L;

    /** Refresh token lifetime in milliseconds. */
    private long refreshTokenExpiration = 604_800_000L;
}
