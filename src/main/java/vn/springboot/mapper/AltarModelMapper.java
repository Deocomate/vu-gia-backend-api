package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.altar.AltarModelResponse;
import vn.springboot.entity.altar.AltarModelEntity;

/**
 * Maps {@link AltarModelEntity} to its API representation. {@code isActive} needs an
 * explicit mapping — see {@link AltarItemGroupMapper} for the full explanation of why.
 * {@code sizes} is populated manually by the service (a separate repository query), so
 * it is ignored here.
 */
@Mapper(componentModel = "spring")
public interface AltarModelMapper {

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "sizes", ignore = true)
    AltarModelResponse toResponse(AltarModelEntity entity);
}
