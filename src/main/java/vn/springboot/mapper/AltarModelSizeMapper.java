package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.altar.AltarModelSizeResponse;
import vn.springboot.entity.altar.AltarModelSizeEntity;

/**
 * Maps {@link AltarModelSizeEntity} to its API representation. {@code isActive} needs an
 * explicit mapping — see {@link AltarItemGroupMapper} for the full explanation of why.
 */
@Mapper(componentModel = "spring")
public interface AltarModelSizeMapper {

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "altarModelId", source = "altarModel.id")
    AltarModelSizeResponse toResponse(AltarModelSizeEntity entity);
}
