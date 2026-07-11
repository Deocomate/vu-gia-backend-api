package vn.springboot.dto.response.redirect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedirectResponse {

    private Long id;

    private String fromPath;

    private String toPath;

    private int statusCode;

    private int hitCount;

    private boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
