package vn.springboot.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import vn.springboot.service.EmailService;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEmailListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private OrderEmailListener listener;

    private OrderPlacedEvent event(String email) {
        return new OrderPlacedEvent(
                email, "ODTEST123", 2_400_000L, 200_000L, 2_200_000L,
                "Nguyễn Văn A", "0900123456", "123 Lê Lợi, HCM",
                List.of(new OrderPlacedEvent.Item("Bát hương men rạn", 2, 1_200_000L, 2_400_000L)));
    }

    @Test
    void onOrderPlaced_rendersTemplate_andSendsHtml() {
        when(templateEngine.process(eq("email/order-confirmation"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        listener.onOrderPlaced(event("buyer@example.com"));

        verify(emailService).sendHtml(eq("buyer@example.com"), contains("ODTEST123"), eq("<html>ok</html>"));
    }

    @Test
    void onOrderPlaced_skips_whenNoRecipient() {
        listener.onOrderPlaced(event("  "));

        verifyNoInteractions(emailService, templateEngine);
    }

    @Test
    void onOrderPlaced_swallowsErrors_soOrderIsUnaffected() {
        when(templateEngine.process(eq("email/order-confirmation"), any(Context.class)))
                .thenThrow(new RuntimeException("smtp down"));

        // Must NOT propagate — a failed email cannot break the placed order.
        listener.onOrderPlaced(event("buyer@example.com"));

        verify(emailService, never()).sendHtml(any(), any(), any());
    }

    /** Renders the real HTML template to prove its Thymeleaf syntax and output are valid. */
    @Test
    void realTemplate_rendersExpectedContent() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        OrderPlacedEvent e = event("buyer@example.com");
        Context ctx = new Context(Locale.forLanguageTag("vi"));
        ctx.setVariable("receiverName", e.receiverName());
        ctx.setVariable("receiverPhone", e.receiverPhone());
        ctx.setVariable("receiverAddress", e.receiverAddress());
        ctx.setVariable("orderCode", e.orderCode());
        ctx.setVariable("items", e.items());
        ctx.setVariable("subtotalAmount", e.subtotalAmount());
        ctx.setVariable("discountAmount", e.discountAmount());
        ctx.setVariable("totalAmount", e.totalAmount());

        String html = engine.process("email/order-confirmation", ctx);

        assertThat(html).contains("ODTEST123");
        assertThat(html).contains("Bát hương men rạn");
        assertThat(html).contains("Nguyễn Văn A");
        assertThat(html).contains("2.200.000"); // totalAmount formatted (POINT grouping)
        assertThat(html).contains("2.400.000"); // subtotal
        assertThat(html).contains("200.000");   // discount row shown (> 0)
    }
}
