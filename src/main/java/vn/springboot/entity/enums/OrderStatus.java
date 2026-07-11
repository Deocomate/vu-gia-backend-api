package vn.springboot.entity.enums;

/** Fulfilment lifecycle of an order. */
public enum OrderStatus {
    PENDING_PAYMENT,
    PROCESSING,
    SHIPPING,
    COMPLETED,
    CANCELLED,
    RETURNED
}
