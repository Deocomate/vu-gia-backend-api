package vn.springboot.service;

import vn.springboot.dto.response.order.PaymentInfoResponse;

/** Builds the VietQR payment info (image URL + bank details) for an online order. */
public interface PaymentQrService {

    PaymentInfoResponse buildQr(String orderCode, long amount);
}
