package vn.springboot.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleLoginRequest {

    /** The ID token (JWT) obtained by the front-end from Google Sign-In. */
    @NotBlank(message = "Google ID token is required")
    private String idToken;
}
