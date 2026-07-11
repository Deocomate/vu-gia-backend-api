package vn.springboot.service;

import vn.springboot.dto.request.webhook.SepayWebhookRequest;

/** Reconciles a SePay bank-transfer webhook against an order. */
public interface PaymentWebhookService {

    /** Idempotent by {@code payload.id}; marks the matched order PAID and triggers its email. */
    void handleSepay(SepayWebhookRequest payload);
}
