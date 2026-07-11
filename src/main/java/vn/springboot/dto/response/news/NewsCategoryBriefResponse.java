package vn.springboot.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Compact category view embedded inside a news response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCategoryBriefResponse {

    private Long id;

    private String name;

    private String slug;
}
