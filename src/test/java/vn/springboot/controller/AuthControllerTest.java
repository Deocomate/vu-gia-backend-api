package vn.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.auth.ChangePasswordRequest;
import vn.springboot.dto.request.auth.GoogleLoginRequest;
import vn.springboot.dto.request.auth.LoginRequest;
import vn.springboot.dto.request.auth.RegisterRequest;
import vn.springboot.dto.response.auth.AuthResponse;
import vn.springboot.dto.response.user.UserResponse;
import vn.springboot.entity.enums.Role;
import vn.springboot.security.AuthCookieService;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private AuthCookieService authCookieService;

    @Test
    void register_returns1000_onValidBody() throws Exception {
        when(authService.register(any())).thenReturn(
                UserResponse.builder().id(1L).username("john").email("john@example.com").role(Role.CUSTOMER).build());

        RegisterRequest req = RegisterRequest.builder()
                .username("john").email("john@example.com").password("secret123").name("John").build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    void register_returns4001_onInvalidBody() throws Exception {
        // Blank username + malformed email -> bean validation failure.
        RegisterRequest req = RegisterRequest.builder()
                .username("").email("not-an-email").password("123").build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void googleLogin_returns1000WithTokens() throws Exception {
        when(authService.loginWithGoogle(any())).thenReturn(AuthResponse.builder()
                .accessToken("access-token").refreshToken("refresh-token").expiresIn(3600)
                .user(UserResponse.builder().username("john").role(Role.CUSTOMER).provider("GOOGLE").build())
                .build());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.user.provider").value("GOOGLE"));
    }

    @Test
    void googleLogin_returns4001_whenTokenMissing() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void login_returns1000WithTokens_andSetsCookieInsteadOfBody() throws Exception {
        when(authService.login(any())).thenReturn(AuthResponse.builder()
                .accessToken("access-token").refreshToken("refresh-token").expiresIn(3600)
                .user(UserResponse.builder().username("john").role(Role.CUSTOMER).build())
                .build());

        LoginRequest req = LoginRequest.builder().username("john").password("secret123").build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());

        // refresh token never leaves the service layer in the JSON body — it is only
        // handed to the cookie service to be set as an httpOnly cookie.
        verify(authCookieService).issueAuthCookies(any(), eq("refresh-token"));
    }

    // ---------- refresh (cookie-based, CSRF-protected) ----------

    @Test
    void refresh_readsCookie_rotatesTokenAndReissuesCookies() throws Exception {
        when(authCookieService.readRefreshToken(any())).thenReturn("old-refresh-cookie-value");
        when(authService.refresh("old-refresh-cookie-value")).thenReturn(AuthResponse.builder()
                .accessToken("new-access-token").refreshToken("new-refresh-token").expiresIn(3600)
                .user(UserResponse.builder().username("john").role(Role.CUSTOMER).build())
                .build());

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());

        verify(authCookieService).verifyCsrf(any());
        verify(authCookieService).issueAuthCookies(any(), eq("new-refresh-token"));
    }

    @Test
    void refresh_returns4031_whenCsrfHeaderMissingOrInvalid() throws Exception {
        doThrow(new AppException(ErrorCode.CSRF_TOKEN_INVALID)).when(authCookieService).verifyCsrf(any());

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4031));

        verify(authService, never()).refresh(any());
    }

    // ---------- logout (cookie-based) ----------

    @Test
    @WithMockUser
    void logout_revokesCookieTokenAndClearsCookies() throws Exception {
        when(authCookieService.readRefreshToken(any())).thenReturn("refresh-cookie-value");

        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));

        verify(authService).logout("refresh-cookie-value");
        verify(authCookieService).clearAuthCookies(any());
    }

    @Test
    void logout_returns401_whenAnonymous() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser
    void changePassword_ok_whenAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/change-password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("old", "newpass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void changePassword_returns401_whenAnonymous() throws Exception {
        mockMvc.perform(post("/api/auth/change-password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("old", "newpass123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }
}
