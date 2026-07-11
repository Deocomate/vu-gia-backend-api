package vn.springboot.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentMethod;
import vn.springboot.entity.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    private String orderCode;

    private OrderStatus status;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    /** VietQR payment info — present only for unpaid ONL orders, otherwise null. */
    private PaymentInfoResponse payment;

    /** Amount payable after discount. */
    private long totalAmount;

    private long discountAmount;

    /** Snapshot of the coupon code used (null if none). */
    private String couponCode;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String note;

    private List<OrderItemResponse> items;

    /** Order placement time (created_at). */
    private Instant createdAt;

    private Instant updatedAt;
}
