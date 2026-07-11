package vn.springboot.service;

import vn.springboot.dto.request.user.ResetPasswordRequest;
import vn.springboot.dto.request.user.UserCreateRequest;
import vn.springboot.dto.request.user.UserRoleUpdateRequest;
import vn.springboot.dto.request.user.UserSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.user.UserResponse;

public interface UserService {

    PageResponse<UserResponse> search(UserSearchRequest request);

    UserResponse getById(Long id);

    /** Create an account with an explicit role (SUPERADMIN-only). */
    UserResponse create(UserCreateRequest request);

    /** Change a user's role (SUPERADMIN-only). */
    UserResponse changeRole(Long id, UserRoleUpdateRequest request);

    /**
     * Admin resets a user's password. An ADMIN may only reset non-staff
     * (CUSTOMER) accounts; resetting an ADMIN/SUPERADMIN requires SUPERADMIN.
     */
    void resetPassword(Long id, ResetPasswordRequest request);
}
