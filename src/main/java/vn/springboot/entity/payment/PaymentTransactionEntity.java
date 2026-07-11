package vn.springboot.entity.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.Instant;

/**
 * A recorded bank-transfer webhook from SePay. {@code sepayId} is UNIQUE so the
 * same transaction (retried/replayed) is processed exactly once.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name = "payment_transactions")
public class PaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sepay_id", unique = true, nullable = false)
    private Long sepayId;

    @Column(name = "order_code", length = 50)
    private String orderCode;

    @Column(name = "gateway", length = 100)
    private String gateway;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "transfer_type", length = 10)
    private String transferType;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "transaction_date", length = 30)
    private String transactionDate;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "matched", nullable = false)
    private boolean matched = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
