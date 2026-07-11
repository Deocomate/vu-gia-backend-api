package vn.springboot.dto.request.coupon;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.DiscountType;

import java.time.Instant;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUpdateRequest {

    @Size(max = 50, message = "Code must be less than 50 characters")
    private String code;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    private DiscountType discountType;

    @Positive(message = "Discount value must be positive")
    private Long discountValue;

    @PositiveOrZero(message = "Min order amount must be >= 0")
    private Long minOrderAmount;

    @PositiveOrZero(message = "Max discount amount must be >= 0")
    private Long maxDiscountAmount;

    @Positive(message = "Usage limit must be positive")
    private Integer usageLimit;

    @Positive(message = "Usage limit per user must be positive")
    private Integer usageLimitPerUser;

    private Instant startsAt;

    private Instant endsAt;

    private Boolean isActive;
}
