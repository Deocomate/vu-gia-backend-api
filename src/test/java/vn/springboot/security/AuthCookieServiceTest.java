package vn.springboot.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthCookieServiceTest {

    private AuthCookieProperties cookieProperties;
    private JwtProperties jwtProperties;
    private AuthCookieService service;

    @BeforeEach
    void setUp() {
        cookieProperties = new AuthCookieProperties();
        jwtProperties = new JwtProperties();
        jwtProperties.setRefreshTokenExpiration(604_800_000L); // 7 days
        service = new AuthCookieService(cookieProperties, jwtProperties);
    }

    @Test
    void issueAuthCookies_setsHttpOnlyRefreshCookieAndReadableCsrfCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.issueAuthCookies(response, "raw-refresh-token");

        Cookie refreshCookie = response.getCookie("refresh_token");
        Cookie csrfCookie = response.getCookie("XSRF-TOKEN");

        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo("raw-refresh-token");
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getSecure()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/api/auth");
        assertThat(refreshCookie.getMaxAge()).isEqualTo(604_800L);

        assertThat(csrfCookie).isNotNull();
        assertThat(csrfCookie.isHttpOnly()).isFalse(); // must be JS-readable for double-submit
        assertThat(csrfCookie.getValue()).isNotBlank();
        assertThat(csrfCookie.getPath()).isEqualTo("/"); // app-wide so document.cookie can see it anywhere
    }

    @Test
    void clearAuthCookies_expiresBothCookies() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.clearAuthCookies(response);

        assertThat(response.getCookie("refresh_token").getMaxAge()).isZero();
        assertThat(response.getCookie("XSRF-TOKEN").getMaxAge()).isZero();
    }

    @Test
    void readRefreshToken_returnsCookieValue_whenPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh_token", "abc-123"));

        assertThat(service.readRefreshToken(request)).isEqualTo("abc-123");
    }

    @Test
    void readRefreshToken_returnsNull_whenAbsent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(service.readRefreshToken(request)).isNull();
    }

    @Test
    void verifyCsrf_passes_whenHeaderMatchesCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("XSRF-TOKEN", "token-value"));
        request.addHeader("X-XSRF-TOKEN", "token-value");

        service.verifyCsrf(request); // does not throw
    }

    @Test
    void verifyCsrf_throws_whenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("XSRF-TOKEN", "token-value"));

        assertThatThrownBy(() -> service.verifyCsrf(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CSRF_TOKEN_INVALID);
    }

    @Test
    void verifyCsrf_throws_whenCookieMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-XSRF-TOKEN", "token-value");

        assertThatThrownBy(() -> service.verifyCsrf(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CSRF_TOKEN_INVALID);
    }

    @Test
    void verifyCsrf_throws_whenHeaderDoesNotMatchCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("XSRF-TOKEN", "token-value"));
        request.addHeader("X-XSRF-TOKEN", "different-value");

        assertThatThrownBy(() -> service.verifyCsrf(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CSRF_TOKEN_INVALID);
    }
}
