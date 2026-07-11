package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.news.NewsCategoryResponse;
import vn.springboot.entity.news.NewsCategoryEntity;

/**
 * Maps {@link NewsCategoryEntity} to its API representation. All exposed
 * fields map by name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface NewsCategoryMapper {

    NewsCategoryResponse toResponse(NewsCategoryEntity entity);
}
