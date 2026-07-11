package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import vn.springboot.config.VietQrProperties;
import vn.springboot.dto.response.order.PaymentInfoResponse;
import vn.springboot.service.PaymentQrService;

/**
 * Builds a VietQR image URL like:
 * {@code https://vietqr.app/img?bank=MBBank&acc=...&template=compact&amount=...&des=<orderCode>&showinfo=true&holder=...}
 * The order code is used as the transfer memo ({@code des}) so payments reconcile.
 */
@Service
@RequiredArgsConstructor
public class VietQrPaymentService implements PaymentQrService {

    private final VietQrProperties properties;

    @Override
    public PaymentInfoResponse buildQr(String orderCode, long amount) {
        String qrImageUrl = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .queryParam("bank", properties.getBank())
                .queryParam("acc", properties.getAccount())
                .queryParam("template", properties.getTemplate())
                .queryParam("amount", amount)
                .queryParam("des", orderCode)
                .queryParam("showinfo", true)
                .queryParam("holder", properties.getHolder())
                .encode()
                .toUriString();

        return PaymentInfoResponse.builder()
                .qrImageUrl(qrImageUrl)
                .amount(amount)
                .bankName(properties.getBank())
                .accountNumber(properties.getAccount())
                .accountHolder(properties.getHolder())
                .transferContent(orderCode)
                .build();
    }
}
