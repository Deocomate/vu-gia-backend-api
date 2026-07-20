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

    /** Cookie {@code Path} attribute — scoped to the auth endpoints that need it. */
    private String path = "/api/auth";

    /** Cookie {@code Domain} attribute; blank = host-only cookie (default, safest for dev). */
    private String domain = "";

    /** Cookie {@code Secure} attribute. Must be {@code true} whenever {@code sameSite=None}. */
    private boolean secure = true;

    /** Cookie {@code SameSite} attribute: {@code Lax}, {@code Strict}, or {@code None}. */
    private String sameSite = "Lax";
}
