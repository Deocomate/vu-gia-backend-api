package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.auth.ChangePasswordRequest;
import vn.springboot.dto.request.auth.GoogleLoginRequest;
import vn.springboot.dto.request.auth.LoginRequest;
import vn.springboot.dto.request.auth.RegisterRequest;
import vn.springboot.dto.response.auth.AuthResponse;
import vn.springboot.dto.response.user.UserResponse;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.user.RefreshTokenEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.mapper.UserMapper;
import vn.springboot.repository.RefreshTokenRepository;
import vn.springboot.repository.UserRepository;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.security.JwtProperties;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.security.oauth2.GoogleTokenVerifier;
import vn.springboot.security.oauth2.GoogleUserInfo;
import vn.springboot.service.AuthService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String PROVIDER_GOOGLE = "GOOGLE";
    private static final int MAX_USERNAME_LENGTH = 50;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return issueTokens(principal.getUser(), principal);
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo info = googleTokenVerifier.verify(request.getIdToken());
        UserEntity user = resolveGoogleUser(info);
        return issueTokens(user, new CustomUserDetails(user));
    }

    /**
     * Find-or-create the account behind a verified Google identity:
     * <ol>
     *   <li>match on (provider, providerId) — a returning Google user;</li>
     *   <li>otherwise match on the verified email and link Google to that account;</li>
     *   <li>otherwise provision a new CUSTOMER account.</li>
     * </ol>
     */
    private UserEntity resolveGoogleUser(GoogleUserInfo info) {
        return userRepository.findByProviderAndProviderId(PROVIDER_GOOGLE, info.subject())
                .orElseGet(() -> userRepository.findByEmail(info.email())
                        .map(existing -> linkGoogle(existing, info))
                        .orElseGet(() -> createGoogleUser(info)));
    }

    private UserEntity linkGoogle(UserEntity user, GoogleUserInfo info) {
        user.setProvider(PROVIDER_GOOGLE);
        user.setProviderId(info.subject());
        if (user.getName() == null && info.name() != null) {
            user.setName(truncate(info.name(), MAX_USERNAME_LENGTH));
        }
        if (user.getAvatar() == null) {
            user.setAvatar(info.picture());
        }
        return userRepository.save(user);
    }

    private UserEntity createGoogleUser(GoogleUserInfo info) {
        UserEntity user = UserEntity.builder()
                .username(generateUniqueUsername(info.email()))
                .email(info.email())
                .password(null) // OAuth-only account
                .name(truncate(info.name(), MAX_USERNAME_LENGTH))
                .avatar(info.picture())
                .provider(PROVIDER_GOOGLE)
                .providerId(info.subject())
                .role(Role.CUSTOMER)
                .build();
        return userRepository.save(user);
    }

    /** Derive a unique username from the email local-part, suffixing on collision. */
    private String generateUniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9]", "");
        if (base.isEmpty()) {
            base = "user";
        }
        base = truncate(base, MAX_USERNAME_LENGTH - 6);
        String candidate = base;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + UUID.randomUUID().toString().substring(0, 5);
        }
        return candidate;
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        RefreshTokenEntity stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (stored.isRevoked()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }
        if (stored.isExpired()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // Rotate: revoke the presented token before issuing a fresh pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        UserEntity user = stored.getUser();
        return issueTokens(user, new CustomUserDetails(user));
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return userMapper.toResponse(currentUser());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UserEntity principalUser = currentUser();
        // Reload as a managed entity so the update is persisted.
        UserEntity user = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // OAuth-only accounts have no local password to verify against.
        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal.getUser();
    }

    private AuthResponse issueTokens(UserEntity user, CustomUserDetails principal) {
        String accessToken = jwtService.generateAccessToken(principal);
        RefreshTokenEntity refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    private RefreshTokenEntity createRefreshToken(UserEntity user) {
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
