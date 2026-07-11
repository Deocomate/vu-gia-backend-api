package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.coupon.CouponResponse;
import vn.springboot.entity.coupon.CouponEntity;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    CouponResponse toResponse(CouponEntity coupon);
}
