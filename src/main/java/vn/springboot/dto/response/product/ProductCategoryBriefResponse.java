package vn.springboot.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Compact category view embedded inside a product response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryBriefResponse {

    private Long id;

    private String name;

    private String slug;
}
