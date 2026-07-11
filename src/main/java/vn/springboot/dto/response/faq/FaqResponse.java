package vn.springboot.dto.response.faq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqResponse {

    private Long id;

    private String question;

    private String answer;

    private String category;

    private Integer sortOrder;

    private boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
