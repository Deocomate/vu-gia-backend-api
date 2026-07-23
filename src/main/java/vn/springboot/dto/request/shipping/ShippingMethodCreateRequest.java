package vn.springboot.dto.request.shipping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingMethodCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @PositiveOrZero
    private Long fee;

    /** {@code null} → defaults to {@code 0} on create. */
    private Integer sortOrder;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;
}
