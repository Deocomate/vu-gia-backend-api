package vn.springboot.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

    private Long id;

    @StorageUrl
    private String url;

    private Integer priority;
}
