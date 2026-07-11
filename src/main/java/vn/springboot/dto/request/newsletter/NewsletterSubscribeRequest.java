package vn.springboot.dto.request.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Public subscribe payload. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscribeRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
}
