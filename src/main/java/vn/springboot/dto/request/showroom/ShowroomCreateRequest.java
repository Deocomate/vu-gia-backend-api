package vn.springboot.dto.request.showroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowroomCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 20)
    private String phone;

    @NotBlank
    private String address;

    @Size(max = 500)
    private String mapEmbedUrl;

    private String openingHours;

    private Integer sortOrder;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;
}
