package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.gallery.GalleryImageResponse;
import vn.springboot.entity.gallery.GalleryImageEntity;

/**
 * Maps {@link GalleryImageEntity} to its API representation. All exposed
 * fields map by name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface GalleryImageMapper {

    GalleryImageResponse toResponse(GalleryImageEntity entity);
}
