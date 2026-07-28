package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.altar.AltarPresetResponse;
import vn.springboot.entity.altar.AltarPresetEntity;

/**
 * Maps {@link AltarPresetEntity} to its API representation. {@code isActive} needs an explicit
 * mapping — see {@link AltarItemGroupMapper} for the full explanation of why. {@code items} and
 * {@code priceSummary} are populated manually by the service (joined product/placement data plus
 * a derived sum), so both are ignored here.
 */
@Mapper(componentModel = "spring")
public interface AltarPresetMapper {

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "altarModelSizeId", source = "altarModelSize.id")
    @Mapping(target = "altarStyleId", source = "altarStyle.id")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "priceSummary", ignore = true)
    AltarPresetResponse toResponse(AltarPresetEntity entity);
}
