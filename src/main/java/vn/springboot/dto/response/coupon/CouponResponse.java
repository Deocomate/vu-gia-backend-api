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
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
