package vn.springboot.dto.request.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidateRequest {

    @NotBlank(message = "Code is required")
    private String code;

    /** Current order subtotal (VND) the discount is evaluated against. */
    @NotNull(message = "Order amount is required")
    @PositiveOrZero(message = "Order amount must be >= 0")
    private Long orderAmount;
}
