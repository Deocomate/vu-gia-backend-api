package vn.springboot.event;

import java.util.List;

/**
 * Published inside the order-creation transaction and consumed after commit, so
 * the confirmation email is only sent once the order is durably persisted.
 * Carries a snapshot of everything the email template needs (no lazy loading in
 * the async listener).
 */
public record OrderPlacedEvent(
        String recipientEmail,
        String orderCode,
        long subtotalAmount,
        long discountAmount,
        long totalAmount,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        List<Item> items) {

    /** One line on the order for the email table. */
    public record Item(String productName, int quantity, long unitPrice, long subtotal) {
    }
}
