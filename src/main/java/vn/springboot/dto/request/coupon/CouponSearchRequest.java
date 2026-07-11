package vn.springboot.dto.request.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.DiscountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponSearchRequest {

    private String code;

    private DiscountType discountType;

    private Boolean isActive;

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "ASC";
}
