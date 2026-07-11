package vn.springboot.dto.response.newsletter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriberResponse {

    private Long id;

    private String email;

    private boolean isActive;

    private Instant createdAt;
}
