package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.user.ResetPasswordRequest;
import vn.springboot.dto.request.user.UserCreateRequest;
import vn.springboot.dto.request.user.UserRoleUpdateRequest;
import vn.springboot.dto.request.user.UserSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.user.UserResponse;
import vn.springboot.service.UserService;

/**
 * User administration. Reads and password resets are for staff
 * ({@code ADMIN}/{@code SUPERADMIN}); creating accounts and changing roles are
 * restricted to {@code SUPERADMIN}.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<PageResponse<UserResponse>> search(@ModelAttribute UserSearchRequest request) {
        return ApiResponse.success(userService.search(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    /** Create an account with an explicit role (e.g. an ADMIN). SUPERADMIN-only. */
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success("Created successfully", userService.create(request));
    }

    /** Promote/demote a user (e.g. CUSTOMER → ADMIN). SUPERADMIN-only. */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ApiResponse<UserResponse> changeRole(@PathVariable Long id,
                                                @Valid @RequestBody UserRoleUpdateRequest request) {
        return ApiResponse.success("Role updated", userService.changeRole(id, request));
    }

    /** Reset a user's password. ADMIN may reset CUSTOMER only; SUPERADMIN anyone. */
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ApiResponse.success("Password reset", null);
    }
}
