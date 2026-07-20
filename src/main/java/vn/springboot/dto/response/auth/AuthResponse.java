package vn.springboot.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.dto.response.user.UserResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    /**
     * Opaque refresh token — kept on this object only so the controller can read it
     * to set the httpOnly {@code refresh_token} cookie. Never serialized to the client;
     * the cookie is the sole transport for it.
     */
    @JsonIgnore
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    /** Access token lifetime in seconds. */
    private long expiresIn;

    private UserResponse user;
}
