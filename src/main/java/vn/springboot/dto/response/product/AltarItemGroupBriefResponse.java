package vn.springboot.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Compact altar item group view embedded inside a product response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarItemGroupBriefResponse {

    private Long id;

    private String name;
}
