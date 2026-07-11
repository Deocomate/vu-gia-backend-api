package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.auth.ChangePasswordRequest;
import vn.springboot.dto.request.auth.GoogleLoginRequest;
import vn.springboot.dto.request.auth.LoginRequest;
import vn.springboot.dto.request.auth.RefreshTokenRequest;
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
import vn.springboot.service.impl.AuthServiceImpl;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private UserMapper userMapper;
    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<UserEntity> userCaptor;

    private UserEntity user() {
        UserEntity u = UserEntity.builder()
                .username("john").email("john@example.com").password("hashed").role(Role.CUSTOMER).build();
        u.setId(1L);
        return u;
    }

    // ---------- register ----------

    @Test
    void register_persistsCustomerAndReturnsResponse() {
        RegisterRequest req = RegisterRequest.builder()
                .username("john").email("john@example.com").password("secret").name("John").build();
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(UserEntity.class)))
                .thenReturn(UserResponse.builder().username("john").role(Role.CUSTOMER).build());

        UserResponse res = authService.register(req);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashed");
        assertThat(res.getUsername()).isEqualTo("john");
    }

    @Test
    void register_throwsWhenUsernameExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);
        RegisterRequest req = RegisterRequest.builder().username("john").email("e@e.com").password("secret").build();

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERNAME_EXISTED);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenEmailExists() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        RegisterRequest req = RegisterRequest.builder()
                .username("john").email("john@example.com").password("secret").build();

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EMAIL_EXISTED);
    }

    // ---------- login ----------

    @Test
    void login_returnsTokens() {
        UserEntity user = user();
        Authentication auth =
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().username("john").build());

        AuthResponse res = authService.login(LoginRequest.builder().username("john").password("secret").build());

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isNotBlank();
        assertThat(res.getExpiresIn()).isEqualTo(3600);
    }

    // ---------- google login ----------

    @Test
    void loginWithGoogle_returnsExistingProviderUser() {
        UserEntity user = user();
        user.setProvider("GOOGLE");
        user.setProviderId("google-sub-1");
        GoogleUserInfo info = new GoogleUserInfo("google-sub-1", "john@example.com", true, "John", null);
        when(googleTokenVerifier.verify("id-token")).thenReturn(info);
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-sub-1")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().username("john").build());

        AuthResponse res = authService.loginWithGoogle(new GoogleLoginRequest("id-token"));

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        verify(userRepository, never()).save(any()); // returning user is not re-persisted
    }

    @Test
    void loginWithGoogle_provisionsNewCustomer_whenUnknown() {
        GoogleUserInfo info = new GoogleUserInfo("google-sub-2", "jane.doe@example.com", true, "Jane", "http://pic");
        when(googleTokenVerifier.verify("id-token")).thenReturn(info);
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-sub-2")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(UserEntity.class))).thenReturn(UserResponse.builder().username("janedoe").build());

        authService.loginWithGoogle(new GoogleLoginRequest("id-token"));

        verify(userRepository).save(userCaptor.capture());
        UserEntity created = userCaptor.getValue();
        assertThat(created.getProvider()).isEqualTo("GOOGLE");
        assertThat(created.getProviderId()).isEqualTo("google-sub-2");
        assertThat(created.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(created.getPassword()).isNull();
        assertThat(created.getUsername()).isEqualTo("janedoe");
        assertThat(created.getAvatar()).isEqualTo("http://pic");
    }

    @Test
    void loginWithGoogle_linksGoogleToExistingEmailAccount() {
        UserEntity existing = user(); // local account, no provider yet
        GoogleUserInfo info = new GoogleUserInfo("google-sub-3", "john@example.com", true, "John", "http://pic");
        when(googleTokenVerifier.verify("id-token")).thenReturn(info);
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-sub-3")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(existing)).thenReturn(UserResponse.builder().username("john").build());

        authService.loginWithGoogle(new GoogleLoginRequest("id-token"));

        assertThat(existing.getProvider()).isEqualTo("GOOGLE");
        assertThat(existing.getProviderId()).isEqualTo("google-sub-3");
    }

    @Test
    void loginWithGoogle_propagatesInvalidToken() {
        when(googleTokenVerifier.verify("bad")).thenThrow(new AppException(ErrorCode.INVALID_GOOGLE_TOKEN));

        assertThatThrownBy(() -> authService.loginWithGoogle(new GoogleLoginRequest("bad")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_GOOGLE_TOKEN);
    }

    // ---------- refresh ----------

    @Test
    void refresh_throwsWhenTokenMissing() {
        when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("x")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    void refresh_throwsWhenRevoked() {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token("x").user(user()).revoked(true).expiresAt(Instant.now().plusSeconds(60)).build();
        when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("x")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);
    }

    @Test
    void refresh_throwsWhenExpired() {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token("x").user(user()).revoked(false).expiresAt(Instant.now().minusSeconds(60)).build();
        when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("x")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    void refresh_rotatesTokenOnSuccess() {
        UserEntity user = user();
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token("old").user(user).revoked(false).expiresAt(Instant.now().plusSeconds(3600)).build();
        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(token));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().username("john").build());

        AuthResponse res = authService.refresh(new RefreshTokenRequest("old"));

        assertThat(token.isRevoked()).isTrue(); // old token rotated out
        assertThat(res.getAccessToken()).isEqualTo("access-token");
    }

    // ---------- change password (self) ----------

    @org.junit.jupiter.api.AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(UserEntity user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));
    }

    @Test
    void changePassword_updatesWhenOldMatches() {
        UserEntity user = user(); // password "hashed"
        authenticate(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("new-hashed");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.changePassword(new ChangePasswordRequest("old", "newpass"));

        assertThat(user.getPassword()).isEqualTo("new-hashed");
    }

    @Test
    void changePassword_throwsWhenOldWrong() {
        UserEntity user = user();
        authenticate(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(new ChangePasswordRequest("wrong", "newpass")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_OLD_PASSWORD);
        verify(userRepository, never()).save(any());
    }

    // ---------- logout ----------

    @Test
    void logout_revokesToken() {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token("x").user(user()).revoked(false).expiresAt(Instant.now().plusSeconds(3600)).build();
        when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.of(token));

        authService.logout(new RefreshTokenRequest("x"));

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }
}
