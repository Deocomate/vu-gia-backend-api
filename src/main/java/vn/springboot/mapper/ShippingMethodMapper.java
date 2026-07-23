package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.shipping.ShippingMethodResponse;
import vn.springboot.entity.shipping.ShippingMethodEntity;

/**
 * Maps {@link ShippingMethodEntity} to its API representation.
 *
 * <p>{@code isActive} needs an explicit mapping: Lombok's {@code @Getter} on a
 * primitive {@code boolean} field named {@code isActive} generates {@code isActive()}
 * (bean property name "active"), while Lombok's {@code @Builder} on the response DTO
 * generates a builder method matching the raw field name {@code isActive(...)}.
 * MapStruct's automatic by-name matching can't reconcile "active" (source) with
 * "isActive" (target builder method) and silently drops the field — it would always
 * serialize as {@code false}. Same latent gap exists in every other {@code *Mapper} in
 * this codebase using this field pattern (Banner, Coupon, Faq, GalleryImage,
 * Newsletter, Showroom, ...); fixed here only, flagged separately for the others.
 */
@Mapper(componentModel = "spring")
public interface ShippingMethodMapper {

    @Mapping(target = "isActive", source = "active")
    ShippingMethodResponse toResponse(ShippingMethodEntity entity);
}
