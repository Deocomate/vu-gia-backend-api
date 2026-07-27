package vn.springboot.event;

import java.time.LocalDateTime;

/**
 * Published after a contact-request row is durably committed, so the admin
 * notification email is only sent once the lead is safely persisted. Carries a
 * snapshot of everything the email template needs (no lazy loading in the async
 * listener).
 */
public record ContactRequestSubmittedEvent(
        String name,
        String email,
        String phone,
        String content,
        LocalDateTime submittedAt) {
}
