package vn.springboot.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code app.security.cookie.*} configuration block: attributes for the
 * httpOnly refresh-token cookie and its accompanying (JS-readable) CSRF cookie.
 *
 * <p>{@code secure} / {@code sameSite} / {@code domain} are environment-dependent —
 * see {@code docs/AUTH_USER_API.md} for the dev vs. prod trade-offs (cross-origin
 * dev setups need a same-origin proxy for {@code SameSite=Lax} cookies to be sent).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.cookie")
public class AuthCookieProperties {

    /** httpOnly cookie carrying the opaque refresh token. */
    private String refreshTokenName = "refresh_token";

    /** Non-httpOnly cookie carrying the double-submit CSRF token. */
    private String csrfTokenName = "XSRF-TOKEN";

    /** Request header the client must echo the CSRF cookie value into. */
    private String csrfHeaderName = "X-XSRF-TOKEN";

    /**
     * {@code Path} attribute for the httpOnly refresh-token cookie — scoped narrowly to
     * the auth endpoints that ever need to receive it (never sent to storefront/admin pages).
     */
    private String refreshTokenPath = "/api/auth";

    /**
     * {@code Path} attribute for the JS-readable CSRF cookie — MUST be app-wide ({@code "/"}).
     * {@code document.cookie} filters by the *current document's own path*, not the path that
     * set the cookie (RFC 6265) — so a cookie scoped to {@code /api/auth} would never appear in
     * {@code document.cookie} on any storefront/admin page (none of which are under
     * {@code /api/auth}), making the double-submit CSRF flow documented in
     * {@code docs/AUTH_USER_API.md} §1.1 impossible for the FE to complete.
     */
    private String csrfTokenPath = "/";

    /** Cookie {@code Domain} attribute; blank = host-only cookie (default, safest for dev). */
    private String domain = "";

    /** Cookie {@code Secure} attribute. Must be {@code true} whenever {@code sameSite=None}. */
    private boolean secure = true;

    /** Cookie {@code SameSite} attribute: {@code Lax}, {@code Strict}, or {@code None}. */
    private String sameSite = "Lax";
}
