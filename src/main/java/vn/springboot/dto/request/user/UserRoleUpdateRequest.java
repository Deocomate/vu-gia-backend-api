package vn.springboot.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.Role;

/** Change a user's role (e.g. promote CUSTOMER → ADMIN). SUPERADMIN-only. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
