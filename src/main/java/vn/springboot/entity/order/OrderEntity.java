package vn.springboot.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import vn.springboot.common.entity.BaseEntity;
import vn.springboot.entity.coupon.CouponEntity;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentMethod;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.user.UserEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(
        name = "uidx_orders_user_idempotency", columnNames = {"user_id", "idempotency_key"}))
public class OrderEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "order_code", unique = true, nullable = false, length = 50)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "coupon_id", nullable = true)
    private CouponEntity coupon;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Builder.Default
    @Column(name = "discount_amount", nullable = false)
    private Long discountAmount = 0L;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", columnDefinition = "TEXT")
    private String receiverAddress;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * Client-supplied idempotency token. Combined with {@code user_id} in a unique
     * index so a retried/duplicate checkout returns the original order instead of
     * creating a second one.
     */
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;
}
