package vn.springboot.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import vn.springboot.service.EmailService;

import java.util.Locale;

/**
 * Sends the order-confirmation email off the request thread, only after the order
 * transaction commits. Renders the {@code email/order-confirmation.html} Thymeleaf
 * template and sends it as HTML. Failures are logged and swallowed — a missed email
 * must not affect the already-placed order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEmailListener {

    private static final String TEMPLATE = "email/order-confirmation";

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            return;
        }
        try {
            Context context = new Context(Locale.forLanguageTag("vi"));
            context.setVariable("receiverName",
                    event.receiverName() == null ? "quý khách" : event.receiverName());
            context.setVariable("receiverPhone", event.receiverPhone());
            context.setVariable("receiverAddress", event.receiverAddress());
            context.setVariable("orderCode", event.orderCode());
            context.setVariable("items", event.items());
            context.setVariable("subtotalAmount", event.subtotalAmount());
            context.setVariable("discountAmount", event.discountAmount());
            context.setVariable("totalAmount", event.totalAmount());

            String html = templateEngine.process(TEMPLATE, context);
            emailService.sendHtml(event.recipientEmail(), "Xác nhận đặt hàng #" + event.orderCode(), html);
        } catch (RuntimeException ex) {
            log.error("Failed to send order-confirmation email for {}", event.orderCode(), ex);
        }
    }
}
