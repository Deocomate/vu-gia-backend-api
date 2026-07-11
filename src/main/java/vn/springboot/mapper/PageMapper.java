package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.page.PageDetailResponse;
import vn.springboot.entity.page.PageEntity;

/**
 * Maps {@link PageEntity} to its API representation. All exposed fields map by
 * name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface PageMapper {

    PageDetailResponse toResponse(PageEntity entity);
}
