package vn.springboot.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A gallery image supplied inline when creating a product. The {@code url} is
 * obtained beforehand from {@code POST /api/media/upload}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {

    @NotBlank
    @Size(max = 255)
    private String url;

    /** Display order; {@code null} → falls back to the position in the list. */
    private Integer priority;
}
