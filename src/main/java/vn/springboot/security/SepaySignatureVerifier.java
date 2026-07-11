package vn.springboot.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.springboot.config.SepayProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Verifies the {@code X-SePay-Signature: sha256=<hex>} header — an HMAC-SHA256 of
 * the raw request body keyed by the shared secret. Fail-closed: if no secret is
 * configured, every webhook is rejected (so a missing secret can never let forged
 * "paid" events through).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SepaySignatureVerifier {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String PREFIX = "sha256=";

    private final SepayProperties properties;

    public boolean verify(String rawBody, String signatureHeader) {
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            log.error("SePay webhook secret not configured (app.sepay.webhook.secret / SEPAY_WEBHOOK_SECRET) "
                    + "— rejecting webhook. Set the secret to enable it.");
            return false;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        String provided = signatureHeader.startsWith(PREFIX)
                ? signatureHeader.substring(PREFIX.length())
                : signatureHeader;

        String expected = hmacSha256Hex(rawBody, secret);
        // Constant-time comparison to avoid timing attacks.
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot compute HMAC-SHA256", ex);
        }
    }
}
