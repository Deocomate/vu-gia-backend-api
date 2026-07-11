package vn.springboot.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCategoryResponse {

    private Long id;

    private String name;

    private String slug;

    private Integer priority;

    private Instant createdAt;

    private Instant updatedAt;
}
