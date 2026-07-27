package vn.springboot.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import vn.springboot.service.EmailService;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Sends the admin contact-request notification email off the request thread, only
 * after the contact-request transaction commits. Renders the
 * {@code email/contact-notification.html} Thymeleaf template and sends it as HTML.
 * Failures are logged and swallowed — a missed notification must not affect the
 * already-persisted lead.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContactRequestEmailListener {

    private static final String TEMPLATE = "email/contact-notification";
    private static final DateTimeFormatter SUBMITTED_AT_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.contact-notify-to:}")
    private String contactNotifyTo;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContactRequestSubmitted(ContactRequestSubmittedEvent event) {
        if (contactNotifyTo == null || contactNotifyTo.isBlank()) {
            log.info("app.mail.contact-notify-to is not configured; skipping contact-request notification email");
            return;
        }
        try {
            Context context = new Context(Locale.forLanguageTag("vi"));
            context.setVariable("name", event.name());
            context.setVariable("email", event.email());
            context.setVariable("phone", event.phone());
            context.setVariable("content", event.content());
            context.setVariable("submittedAt", event.submittedAt().format(SUBMITTED_AT_FORMAT));

            String html = templateEngine.process(TEMPLATE, context);
            emailService.sendHtml(contactNotifyTo, "Yêu cầu liên hệ mới từ " + event.name(), html);
        } catch (RuntimeException ex) {
            log.error("Failed to send contact-request notification email", ex);
        }
    }
}
