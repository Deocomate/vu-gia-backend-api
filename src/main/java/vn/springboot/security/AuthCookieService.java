package vn.springboot.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;

import java.util.UUID;

/**
 * Owns the two auth-related cookies:
 * <ul>
 *   <li>{@code refresh_token} — httpOnly, holds the opaque refresh token, never exposed
 *       to JS or the JSON response body;</li>
 *   <li>{@code XSRF-TOKEN} — not httpOnly (JS-readable), used for the double-submit
 *       CSRF check on {@code POST /api/auth/refresh}.</li>
 * </ul>
 * Both share the same {@code Secure}/{@code SameSite}/{@code Domain}/{@code Path}
 * attributes, bound from {@link AuthCookieProperties}.
 */
@Component
@RequiredArgsConstructor
public class AuthCookieService {

    private final AuthCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    /** Issue (or rotate) both cookies after a successful login/register-login/refresh. */
    public void issueAuthCookies(HttpServletResponse response, String refreshToken) {
        long maxAgeSeconds = jwtProperties.getRefreshTokenExpiration() / 1000;
        addCookie(response, cookieProperties.getRefreshTokenName(), refreshToken, true,
                cookieProperties.getRefreshTokenPath(), maxAgeSeconds);
        addCookie(response, cookieProperties.getCsrfTokenName(), UUID.randomUUID().toString(), false,
                cookieProperties.getCsrfTokenPath(), maxAgeSeconds);
    }

    /** Expire both cookies on logout (matching attributes so the browser actually overwrites them). */
    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, cookieProperties.getRefreshTokenName(), "", true,
                cookieProperties.getRefreshTokenPath(), 0);
        addCookie(response, cookieProperties.getCsrfTokenName(), "", false,
                cookieProperties.getCsrfTokenPath(), 0);
    }

    /** Reads the refresh token from the httpOnly cookie; {@code null} if absent. */
    public String readRefreshToken(HttpServletRequest request) {
        return readCookie(request, cookieProperties.getRefreshTokenName());
    }

    /**
     * Double-submit CSRF check: the {@code X-XSRF-TOKEN} header must be present and equal
     * the {@code XSRF-TOKEN} cookie value. An attacker site can trigger the cookie-bearing
     * request but — cookies being domain-scoped — cannot read the cookie value via JS to
     * echo it back into the header, so the two can only match on a legitimate first-party call.
     */
    public void verifyCsrf(HttpServletRequest request) {
        String cookieValue = readCookie(request, cookieProperties.getCsrfTokenName());
        String headerValue = request.getHeader(cookieProperties.getCsrfHeaderName());
        if (cookieValue == null || cookieValue.isBlank()
                || headerValue == null || !cookieValue.equals(headerValue)) {
            throw new AppException(ErrorCode.CSRF_TOKEN_INVALID);
        }
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value,
                            boolean httpOnly, String path, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(cookieProperties.isSecure())
                .path(path)
                .maxAge(maxAgeSeconds)
                .sameSite(cookieProperties.getSameSite());
        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
