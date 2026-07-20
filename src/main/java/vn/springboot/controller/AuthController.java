package vn.springboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.auth.ChangePasswordRequest;
import vn.springboot.dto.request.auth.GoogleLoginRequest;
import vn.springboot.dto.request.auth.LoginRequest;
import vn.springboot.dto.request.auth.RegisterRequest;
import vn.springboot.dto.response.auth.AuthResponse;
import vn.springboot.dto.response.user.UserResponse;
import vn.springboot.security.AuthCookieService;
import vn.springboot.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletResponse response) {
        AuthResponse auth = authService.login(request);
        authCookieService.issueAuthCookies(response, auth.getRefreshToken());
        return ApiResponse.success("Login successful", auth);
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request,
                                                       HttpServletResponse response) {
        AuthResponse auth = authService.loginWithGoogle(request);
        authCookieService.issueAuthCookies(response, auth.getRefreshToken());
        return ApiResponse.success("Login successful", auth);
    }

    /**
     * Reads the refresh token from the httpOnly cookie (no request body). Requires the
     * {@code X-XSRF-TOKEN} header to match the {@code XSRF-TOKEN} cookie (double-submit
     * CSRF check) — see {@link AuthCookieService#verifyCsrf}.
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        authCookieService.verifyCsrf(request);
        AuthResponse auth = authService.refresh(authCookieService.readRefreshToken(request));
        authCookieService.issueAuthCookies(response, auth.getRefreshToken());
        return ApiResponse.success("Token refreshed", auth);
    }

    /** Reads the refresh token from the httpOnly cookie and clears both auth cookies. */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(authCookieService.readRefreshToken(request));
        authCookieService.clearAuthCookies(response);
        return ApiResponse.success("Logout successful", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> currentUser() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success("Password changed", null);
    }
}
