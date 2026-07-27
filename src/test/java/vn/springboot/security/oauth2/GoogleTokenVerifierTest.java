package vn.springboot.security.oauth2;

import org.junit.jupiter.api.Test;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.security.GoogleOAuthProperties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 1 security hardening: a blank {@code app.oauth2.google.client-id} must fail closed
 * unconditionally (no dev/prod branching), matching {@code SepaySignatureVerifier}'s behavior.
 * The blank-check runs before any network call to Google, so no HTTP mocking is needed here.
 */
class GoogleTokenVerifierTest {

    @Test
    void verify_rejectsUnconditionally_whenClientIdBlank() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        properties.setClientId("");
        GoogleTokenVerifier verifier = new GoogleTokenVerifier(properties);

        assertThatThrownBy(() -> verifier.verify("any-id-token"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_GOOGLE_TOKEN);
    }

    @Test
    void verify_rejectsUnconditionally_whenClientIdNull() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        properties.setClientId(null);
        GoogleTokenVerifier verifier = new GoogleTokenVerifier(properties);

        assertThatThrownBy(() -> verifier.verify("any-id-token"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_GOOGLE_TOKEN);
    }

    @Test
    void verify_rejectsUnconditionally_whenClientIdBlankWhitespace() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        properties.setClientId("   ");
        GoogleTokenVerifier verifier = new GoogleTokenVerifier(properties);

        assertThatThrownBy(() -> verifier.verify("any-id-token"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_GOOGLE_TOKEN);
    }
}
