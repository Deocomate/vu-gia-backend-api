package vn.springboot.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.PaymentMethod;

import java.util.List;

/** Checkout payload. The server snapshots prices/names from live products. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlaceRequest {

    /** Client-generated token (e.g. a UUID) that makes re-submits idempotent. */
    @NotBlank
    @Size(max = 100)
    private String idempotencyKey;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    /** Optional coupon code; validated against all conditions before applying. */
    private String couponCode;

    /** Payment method: {@code COD} (default) or {@code ONL} (online VietQR transfer). */
    private PaymentMethod paymentMethod;

    /**
     * Optional shipping method id. {@code null} → no shipping fee added (kept
     * optional so older/omitting clients still work with {@code shippingFee = 0}).
     */
    private Long shippingMethodId;

    @NotBlank
    @Size(max = 100)
    private String receiverName;

    @NotBlank
    @Size(max = 20)
    private String receiverPhone;

    @NotBlank
    private String receiverAddress;

    private String note;
}
