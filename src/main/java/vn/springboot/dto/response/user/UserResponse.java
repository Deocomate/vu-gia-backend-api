package vn.springboot.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;
import vn.springboot.entity.enums.Role;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String name;

    private String phone;

    private String gender;

    private LocalDate dob;

    @StorageUrl
    private String avatar;

    /** {@code null} = local account; otherwise the OAuth provider (e.g. GOOGLE). */
    private String provider;

    private Role role;

    private Instant createdAt;
}
