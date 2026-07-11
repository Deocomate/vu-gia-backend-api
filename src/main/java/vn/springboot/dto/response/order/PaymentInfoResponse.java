package vn.springboot.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Online-payment (VietQR) info returned when an order is placed with method ONL.
 * The frontend renders {@code qrImageUrl}; the customer transfers {@code amount}
 * with {@code transferContent} (= order code) so the payment can be reconciled.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfoResponse {

    /** Ready-to-render VietQR image URL. */
    private String qrImageUrl;

    private long amount;

    private String bankName;

    private String accountNumber;

    private String accountHolder;

    /** Transfer memo the customer must keep — equals the order code. */
    private String transferContent;
}
