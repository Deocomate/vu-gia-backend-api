package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.altar.AltarStyleResponse;
import vn.springboot.entity.altar.AltarStyleEntity;

/**
 * Maps {@link AltarStyleEntity} to its API representation. {@code isActive} needs an
 * explicit mapping — see {@link AltarItemGroupMapper} for the full explanation of why.
 */
@Mapper(componentModel = "spring")
public interface AltarStyleMapper {

    @Mapping(target = "isActive", source = "active")
    AltarStyleResponse toResponse(AltarStyleEntity entity);
}
