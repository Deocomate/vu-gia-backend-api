package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.user.ResetPasswordRequest;
import vn.springboot.dto.request.user.UserCreateRequest;
import vn.springboot.dto.request.user.UserRoleUpdateRequest;
import vn.springboot.dto.request.user.UserSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.user.UserResponse;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.mapper.UserMapper;
import vn.springboot.repository.UserRepository;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;
    @Captor
    private ArgumentCaptor<UserEntity> userCaptor;

    /** Puts an authenticated user of the given role into the security context. */
    private void authenticateAs(Role role) {
        UserEntity actor = UserEntity.builder().username("actor").role(role).build();
        actor.setId(99L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(actor), null));
    }

    @org.junit.jupiter.api.AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private UserEntity sampleUser() {
        UserEntity user = UserEntity.builder()
                .username("john")
                .email("john@example.com")
                .name("John")
                .role(Role.CUSTOMER)
                .build();
        user.setId(1L);
        return user;
    }

    private UserResponse sampleResponse() {
        return UserResponse.builder().id(1L).username("john").email("john@example.com").role(Role.CUSTOMER).build();
    }

    @Test
    void search_returnsPageResponse() {
        UserEntity user = sampleUser();
        Page<UserEntity> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.toResponse(user)).thenReturn(sampleResponse());

        PageResponse<UserResponse> result = userService.search(UserSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1); // 1-based
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void search_clampsSizeAndDefaultsUnknownSortField() {
        Page<UserEntity> empty = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(empty);

        UserSearchRequest request = UserSearchRequest.builder()
                .size(999)
                .sortBy("password; DROP TABLE users")
                .sortDirection("DESC")
                .build();
        userService.search(request);

        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(100); // clamped to MAX_PAGE_SIZE
        Sort.Order order = pageable.getSort().iterator().next();
        assertThat(order.getProperty()).isEqualTo("id"); // unknown field -> default
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getById_returnsUser_whenFound() {
        UserEntity user = sampleUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(sampleResponse());

        assertThat(userService.getById(1L).getUsername()).isEqualTo("john");
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ---------- create ----------

    @Test
    void create_persistsUserWithChosenRole() {
        UserCreateRequest req = UserCreateRequest.builder()
                .username("boss").email("boss@example.com").password("secret").name("Boss").role(Role.ADMIN).build();
        when(userRepository.existsByUsername("boss")).thenReturn(false);
        when(userRepository.existsByEmail("boss@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(UserEntity.class))).thenReturn(sampleResponse());

        userService.create(req);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashed");
    }

    @Test
    void create_rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("boss")).thenReturn(true);
        UserCreateRequest req = UserCreateRequest.builder()
                .username("boss").email("e@e.com").password("secret").role(Role.ADMIN).build();

        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERNAME_EXISTED);
        verify(userRepository, never()).save(any());
    }

    // ---------- changeRole ----------

    @Test
    void changeRole_updatesRole() {
        UserEntity user = sampleUser(); // CUSTOMER
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(user)).thenReturn(sampleResponse());

        userService.changeRole(1L, new UserRoleUpdateRequest(Role.ADMIN));

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    // ---------- resetPassword ----------

    @Test
    void resetPassword_superadminCanResetStaff() {
        authenticateAs(Role.SUPERADMIN);
        UserEntity target = sampleUser();
        target.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(passwordEncoder.encode("newpass")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.resetPassword(1L, new ResetPasswordRequest("newpass"));

        assertThat(target.getPassword()).isEqualTo("hashed");
    }

    @Test
    void resetPassword_adminCanResetCustomer() {
        authenticateAs(Role.ADMIN);
        UserEntity target = sampleUser(); // CUSTOMER
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(passwordEncoder.encode("newpass")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.resetPassword(1L, new ResetPasswordRequest("newpass"));

        assertThat(target.getPassword()).isEqualTo("hashed");
    }

    @Test
    void resetPassword_adminCannotResetStaff() {
        authenticateAs(Role.ADMIN);
        UserEntity target = sampleUser();
        target.setRole(Role.SUPERADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> userService.resetPassword(1L, new ResetPasswordRequest("newpass")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
        verify(userRepository, never()).save(any());
    }
}
