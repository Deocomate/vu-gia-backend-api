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

    /**
     * Google OAuth Web client ID. Empty/blank fails closed unconditionally — see
     * {@link vn.springboot.security.oauth2.GoogleTokenVerifier#verify} — every Google
     * login attempt is rejected until this is set, in every profile.
     */
    private String clientId = "";
}
