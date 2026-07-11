package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.banner.BannerResponse;
import vn.springboot.entity.banner.BannerEntity;

/**
 * Maps {@link BannerEntity} to its API representation. All exposed fields map
 * by name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface BannerMapper {

    BannerResponse toResponse(BannerEntity entity);
}
