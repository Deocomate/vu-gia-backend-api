package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.altar.AltarDesignResponse;
import vn.springboot.entity.altar.AltarDesignEntity;

/**
 * Maps the persisted scalar fields only. {@code altarModelSizeLabel}, {@code altarStyleId},
 * {@code items}/{@code accessories} (post drop-filter), {@code currentTotalPrice}, and
 * {@code droppedItemCount} all depend on a live-catalog recompute and are populated by
 * {@code AltarDesignServiceImpl#toDetailResponse}, so they're ignored here.
 */
@Mapper(componentModel = "spring")
public interface AltarDesignMapper {

    @Mapping(target = "altarModelSizeId", source = "altarModelSize.id")
    @Mapping(target = "altarModelSizeLabel", ignore = true)
    @Mapping(target = "altarStyleId", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "accessories", ignore = true)
    @Mapping(target = "currentTotalPrice", ignore = true)
    @Mapping(target = "droppedItemCount", ignore = true)
    AltarDesignResponse toResponse(AltarDesignEntity entity);
}
