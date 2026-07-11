package vn.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.dto.request.webhook.SepayWebhookRequest;
import vn.springboot.security.SepaySignatureVerifier;
import vn.springboot.service.PaymentWebhookService;

import java.util.Map;

/**
 * Payment gateway webhooks. Public (no JWT) — authenticated by the SePay HMAC
 * signature. SePay treats the call as successful only on HTTP 200/201 with body
 * exactly {@code {"success": true}} within 30s, so we ack fast and keep processing
 * light (email is async).
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private static final Map<String, Boolean> OK = Map.of("success", true);

    private final SepaySignatureVerifier signatureVerifier;
    private final PaymentWebhookService paymentWebhookService;
    private final ObjectMapper objectMapper;

    @PostMapping("/sepay")
    public ResponseEntity<Map<String, Boolean>> sepay(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-SePay-Signature", required = false) String signature) {

        if (!signatureVerifier.verify(rawBody, signature)) {
            log.warn("Rejected SePay webhook: invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        }

        SepayWebhookRequest payload;
        try {
            payload = objectMapper.readValue(rawBody, SepayWebhookRequest.class);
        } catch (Exception ex) {
            log.warn("Rejected SePay webhook: malformed body", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }

        try {
            paymentWebhookService.handleSepay(payload);
        } catch (DataIntegrityViolationException dup) {
            // Concurrent duplicate lost the UNIQUE(sepay_id) race — the winner handled it.
            log.info("Duplicate SePay txn {} ignored", payload.getId());
        }

        return ResponseEntity.ok(OK);
    }
}
