package vn.springboot.dto.response.showroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowroomResponse {

    private Long id;

    private String name;

    private String phone;

    private String address;

    private String mapEmbedUrl;

    private String openingHours;

    private Integer sortOrder;

    private boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
