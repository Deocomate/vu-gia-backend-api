package vn.springboot.service;

import org.junit.jupiter.api.Test;
import vn.springboot.config.VietQrProperties;
import vn.springboot.dto.response.order.PaymentInfoResponse;
import vn.springboot.service.impl.VietQrPaymentService;

import static org.assertj.core.api.Assertions.assertThat;

class VietQrPaymentServiceTest {

    private VietQrPaymentService service() {
        VietQrProperties props = new VietQrProperties();
        props.setBaseUrl("https://vietqr.app/img");
        props.setBank("MBBank");
        props.setAccount("686804076868");
        props.setHolder("NGUYEN DUY DAT");
        props.setTemplate("compact");
        return new VietQrPaymentService(props);
    }

    @Test
    void buildQr_buildsVietQrUrl_withOrderCodeAsMemo() {
        PaymentInfoResponse info = service().buildQr("OD123ABC", 250_000L);

        assertThat(info.getQrImageUrl())
                .startsWith("https://vietqr.app/img?")
                .contains("bank=MBBank")
                .contains("acc=686804076868")
                .contains("template=compact")
                .contains("amount=250000")
                .contains("des=OD123ABC")        // order code = transfer memo
                .contains("showinfo=true")
                .contains("holder=NGUYEN%20DUY%20DAT"); // spaces URL-encoded

        assertThat(info.getAmount()).isEqualTo(250_000L);
        assertThat(info.getTransferContent()).isEqualTo("OD123ABC");
        assertThat(info.getBankName()).isEqualTo("MBBank");
        assertThat(info.getAccountNumber()).isEqualTo("686804076868");
        assertThat(info.getAccountHolder()).isEqualTo("NGUYEN DUY DAT");
    }
}
