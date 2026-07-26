package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.coupon.CouponResponse;
import vn.springboot.entity.coupon.CouponEntity;

/**
 * {@code isActive} needs an explicit mapping: Lombok's primitive-boolean
 * getter derives bean property name "active", which MapStruct's by-name
 * matching can't reconcile with the DTO's "isActive" builder method.
 */
@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(target = "isActive", source = "active")
    CouponResponse toResponse(CouponEntity coupon);
}
