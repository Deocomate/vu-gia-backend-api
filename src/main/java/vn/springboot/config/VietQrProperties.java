package vn.springboot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.payment.vietqr.*} — the receiving bank account used to build
 * the VietQR payment image for online (ONL) orders.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.payment.vietqr")
public class VietQrProperties {

    /** VietQR image endpoint. */
    private String baseUrl = "https://vietqr.app/img";

    /** Bank short name (e.g. MBBank). */
    private String bank = "MBBank";

    /** Receiving account number. */
    private String account = "";

    /** Account holder name (shown on the QR). */
    private String holder = "";

    /** QR template (compact, compact2, qr_only, print). */
    private String template = "compact";
}
