package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.entity.altar.AltarPlacementEntity;

/**
 * Maps {@link AltarPlacementEntity} to its API representation.
 *
 * <p>{@code flippable} does NOT need an explicit mapping, by the same reasoning as
 * {@code renderOnAltar} in {@link AltarItemGroupMapper}: since the field name doesn't start
 * with "is", Lombok's getter is {@code isFlippable()}, whose bean property name (per JavaBean
 * introspection) is "flippable" — an exact match with the target field, so MapStruct's by-name
 * matching works automatically. Verified by {@code AltarPlacementMapperTest}.
 */
@Mapper(componentModel = "spring")
public interface AltarPlacementMapper {

    @Mapping(target = "productImageId", source = "productImage.id")
    AltarPlacementResponse toResponse(AltarPlacementEntity entity);
}
