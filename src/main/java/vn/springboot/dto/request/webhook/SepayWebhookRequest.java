package vn.springboot.dto.request.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload SePay POST tới webhook khi có giao dịch khớp. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookRequest {

    /** ID giao dịch trên SePay — khóa chống trùng. */
    private Long id;

    private String gateway;

    private String transactionDate;

    private String accountNumber;

    private String subAccount;

    /** Mã thanh toán trích từ nội dung theo tiền tố (vd mã đơn OD...). Có thể null. */
    private String code;

    private String content;

    /** {@code in} = tiền vào, {@code out} = tiền ra. */
    private String transferType;

    private String description;

    private long transferAmount;

    private long accumulated;

    private String referenceCode;
}
