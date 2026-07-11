package vn.springboot.security.oauth2;

/**
 * Verified identity extracted from a Google ID token.
 *
 * @param subject       Google's stable user id ({@code sub}) → stored as {@code provider_id}
 * @param email         verified email address
 * @param emailVerified whether Google has verified the email
 * @param name          display name (may be null)
 * @param picture       avatar URL (may be null)
 */
public record GoogleUserInfo(
        String subject,
        String email,
        boolean emailVerified,
        String name,
        String picture) {
}
