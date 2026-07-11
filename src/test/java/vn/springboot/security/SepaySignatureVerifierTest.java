package vn.springboot.security;

import org.junit.jupiter.api.Test;
import vn.springboot.config.SepayProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class SepaySignatureVerifierTest {

    private SepaySignatureVerifier verifier(String secret) {
        SepayProperties props = new SepayProperties();
        props.setSecret(secret);
        return new SepaySignatureVerifier(props);
    }

    private String sign(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void validSignature_passes() throws Exception {
        String body = "{\"id\":92704,\"transferAmount\":5000000}";
        String secret = "s3cr3t-key";
        String header = "sha256=" + sign(body, secret);

        assertThat(verifier(secret).verify(body, header)).isTrue();
    }

    @Test
    void invalidSignature_fails() {
        assertThat(verifier("s3cr3t-key").verify("{\"id\":1}", "sha256=deadbeef")).isFalse();
    }

    @Test
    void missingHeader_withSecret_fails() {
        assertThat(verifier("s3cr3t-key").verify("{\"id\":1}", null)).isFalse();
    }

    @Test
    void noSecretConfigured_blocks() {
        // Fail-closed: without a secret, even a request that "looks signed" is rejected.
        assertThat(verifier("").verify("{\"id\":1}", "sha256=whatever")).isFalse();
    }
}
