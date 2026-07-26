package vn.springboot.dto.response.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.DiscountType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {

    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private Long discountValue;
    private Long minOrderAmount;
    private Long maxDiscountAmount;
    private Integer usageLimit;
    private Integer usageLimitPerUser;
    private int usedCount;
    private Instant startsAt;
    private Instant endsAt;
    /**
     * Wrapper {@code Boolean} (not primitive) deliberately: a primitive {@code boolean}
     * field named {@code isActive} makes Lombok's getter/JSON property name "active"
     * (the "is" prefix is stripped for primitive booleans), which silently mismatches
     * the FE-facing "isActive" wire contract. The wrapper type keeps the standard
     * {@code getIsActive()}/{@code isActive} JSON-key pairing with no ambiguity.
     */
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
