package vn.springboot.dto.request.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Public contact-form submission. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequestCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    private String content;
}
