package vn.springboot.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code app.oauth2.google.*} configuration block.
 * The {@code clientId} (a.k.a. audience) is the Google OAuth 2.0 Web client ID;
 * every Google ID token presented for login must have been issued for it.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.oauth2.google")
public class GoogleOAuthProperties {

    /** Google OAuth Web client ID; empty disables the audience check (dev only). */
    private String clientId = "";
}
