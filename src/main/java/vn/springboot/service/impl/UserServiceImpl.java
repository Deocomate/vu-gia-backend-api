package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.common.util.PaginationUtils;
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
import vn.springboot.repository.specification.UserSpecification;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.service.UserService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "username", "email", "name", "role", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(UserSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<UserEntity> specification = UserSpecification.build(request);

        Page<UserEntity> userPage = userRepository.findAll(specification, pageable);

        List<UserResponse> content = userPage.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(userPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
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
                .role(request.getRole())
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long id, UserRoleUpdateRequest request) {
        UserEntity user = findOrThrow(id);
        user.setRole(request.getRole());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        UserEntity target = findOrThrow(id);

        // An ADMIN may only reset non-staff accounts; only a SUPERADMIN can reset
        // an ADMIN/SUPERADMIN password (prevents privilege escalation).
        if (currentRole() != Role.SUPERADMIN && target.getRole() != Role.CUSTOMER) {
            throw new AppException(ErrorCode.ACCESS_DENIED,
                    "Only SUPERADMIN can reset the password of a staff account");
        }

        target.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(target);
    }

    private Role currentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUser().getRole();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    private UserEntity findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
