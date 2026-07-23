package vn.springboot.dto.request.shipping;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingMethodUpdateRequest {

    @Size(max = 100)
    private String name;

    @PositiveOrZero
    private Long fee;

    private Integer sortOrder;

    private Boolean isActive;
}
