package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.altar.AltarItemGroupResponse;
import vn.springboot.entity.altar.AltarItemGroupEntity;

/**
 * Maps {@link AltarItemGroupEntity} to its API representation.
 *
 * <p>{@code isActive} needs an explicit mapping: Lombok's {@code @Getter} on a
 * primitive {@code boolean} field named {@code isActive} generates {@code isActive()}
 * (bean property name "active"), while Lombok's {@code @Builder} on the response DTO
 * generates a builder method matching the raw field name {@code isActive(...)}.
 * MapStruct's automatic by-name matching can't reconcile "active" (source) with
 * "isActive" (target builder method) and silently drops the field — it would always
 * serialize as {@code false}. See {@code ShippingMethodMapper} for the reference fix.
 *
 * <p>{@code renderOnAltar} does NOT need this treatment: since the field name doesn't
 * start with "is", Lombok's getter is {@code isRenderOnAltar()}, whose bean property
 * name (per JavaBean introspection) is "renderOnAltar" — an exact match with the
 * target field, so MapStruct's by-name matching works automatically.
 */
@Mapper(componentModel = "spring")
public interface AltarItemGroupMapper {

    @Mapping(target = "isActive", source = "active")
    AltarItemGroupResponse toResponse(AltarItemGroupEntity entity);
}
