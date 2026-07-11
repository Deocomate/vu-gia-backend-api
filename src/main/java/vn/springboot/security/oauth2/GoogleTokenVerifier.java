package vn.springboot.security.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.security.GoogleOAuthProperties;

/**
 * Verifies a Google ID token via Google's {@code tokeninfo} endpoint.
 *
 * <p>Google validates the token signature and expiry server-side; on top of that
 * we enforce that the token was issued for our own client id ({@code aud}) and
 * that the email is verified. This keeps the backend dependency-free (uses only
 * Spring's {@link RestClient}); it can later be swapped for local JWKS verification
 * without touching callers.
 */
@Slf4j
@Component
public class GoogleTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final GoogleOAuthProperties properties;
    private final RestClient restClient;

    public GoogleTokenVerifier(GoogleOAuthProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public GoogleUserInfo verify(String idToken) {
        TokenInfo info = fetch(idToken);
        if (info == null || info.sub == null || info.email == null) {
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        // Reject tokens minted for a different app.
        String expectedAud = properties.getClientId();
        if (expectedAud != null && !expectedAud.isBlank() && !expectedAud.equals(info.aud)) {
            log.warn("Google token audience mismatch: expected={}, actual={}", expectedAud, info.aud);
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        if (!"true".equalsIgnoreCase(info.emailVerified)) {
            throw new AppException(ErrorCode.GOOGLE_EMAIL_NOT_VERIFIED);
        }

        return new GoogleUserInfo(info.sub, info.email, true, info.name, info.picture);
    }

    private TokenInfo fetch(String idToken) {
        try {
            return restClient.get()
                    .uri(TOKENINFO_URL, uri -> uri.queryParam("id_token", idToken).build())
                    .retrieve()
                    .body(TokenInfo.class);
        } catch (Exception ex) {
            // Non-2xx (invalid/expired token) or transport error -> treat as invalid.
            log.debug("Google tokeninfo verification failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }
    }

    /** Subset of the tokeninfo response we care about. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TokenInfo {
        @JsonProperty("aud")
        String aud;
        @JsonProperty("sub")
        String sub;
        @JsonProperty("email")
        String email;
        @JsonProperty("email_verified")
        String emailVerified;
        @JsonProperty("name")
        String name;
        @JsonProperty("picture")
        String picture;
    }
}
