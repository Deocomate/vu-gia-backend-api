package vn.springboot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.SepaySignatureVerifier;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.PaymentWebhookService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentWebhookController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SepaySignatureVerifier signatureVerifier;
    @MockitoBean
    private PaymentWebhookService paymentWebhookService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final String BODY = """
            {"id":92704,"gateway":"MBBank","transactionDate":"2024-07-02 11:08:33",
             "accountNumber":"1017588888","code":"ODF97CD281BE0D","content":"ODF97CD281BE0D chuyen tien",
             "transferType":"in","transferAmount":1200000,"referenceCode":"FT24012345678"}
            """;

    @Test
    void validSignature_returns200_success() throws Exception {
        when(signatureVerifier.verify(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/webhooks/sepay")
                        .with(csrf())
                        .header("X-SePay-Signature", "sha256=abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentWebhookService).handleSepay(any());
    }

    @Test
    void invalidSignature_returns401_andSkipsProcessing() throws Exception {
        when(signatureVerifier.verify(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/webhooks/sepay")
                        .with(csrf())
                        .header("X-SePay-Signature", "sha256=wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(paymentWebhookService);
    }
}
