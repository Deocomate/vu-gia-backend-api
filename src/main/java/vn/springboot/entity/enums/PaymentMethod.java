package vn.springboot.entity.enums;

/** How the customer pays for an order. */
public enum PaymentMethod {
    /** Cash on delivery — confirmed on delivery (email sent immediately at placement). */
    COD,
    /** Online transfer via VietQR — a QR is returned; email is sent after payment is confirmed. */
    ONL
}
