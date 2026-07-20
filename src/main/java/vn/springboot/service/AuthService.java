package vn.springboot.service;

import vn.springboot.dto.request.auth.ChangePasswordRequest;
import vn.springboot.dto.request.auth.GoogleLoginRequest;
import vn.springboot.dto.request.auth.LoginRequest;
import vn.springboot.dto.request.auth.RegisterRequest;
import vn.springboot.dto.response.auth.AuthResponse;
import vn.springboot.dto.response.user.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    /**
     * Rotates the refresh token read from the httpOnly cookie.
     * @param refreshToken raw token value, or {@code null}/blank if the cookie was absent
     */
    AuthResponse refresh(String refreshToken);

    /**
     * Revokes the refresh token read from the httpOnly cookie. Idempotent: a {@code null}/blank
     * or already-unknown token is a no-op (the cookie is cleared by the caller regardless).
     */
    void logout(String refreshToken);

    UserResponse getCurrentUser();

    /** The current user changes their own password (verifies the old one). */
    void changePassword(ChangePasswordRequest request);
}
