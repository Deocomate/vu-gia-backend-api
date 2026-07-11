package vn.springboot.dto.request.showroom;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowroomUpdateRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 20)
    private String phone;

    private String address;

    @Size(max = 500)
    private String mapEmbedUrl;

    private String openingHours;

    private Integer sortOrder;

    private Boolean isActive;
}
