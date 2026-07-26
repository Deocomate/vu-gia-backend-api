package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.banner.BannerResponse;
import vn.springboot.entity.banner.BannerEntity;

/**
 * Maps {@link BannerEntity} to its API representation. All exposed fields map
 * by name, except {@code isActive} which needs an explicit mapping: Lombok's
 * primitive-boolean getter derives bean property name "active", which
 * MapStruct's by-name matching can't reconcile with the DTO's "isActive"
 * builder method. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface BannerMapper {

    @Mapping(target = "isActive", source = "active")
    BannerResponse toResponse(BannerEntity entity);
}
