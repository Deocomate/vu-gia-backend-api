package vn.springboot.service;

/**
 * Sends transactional emails. Kept behind an interface so callers depend on the
 * abstraction (DIP) and the transport (SMTP, a provider API, a test double) can
 * be swapped without touching them.
 */
public interface EmailService {

    /** Plain-text email. */
    void send(String to, String subject, String body);

    /** HTML email ({@code htmlContent} is a fully-rendered HTML document). */
    void sendHtml(String to, String subject, String htmlContent);
}
