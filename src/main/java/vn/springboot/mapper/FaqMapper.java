package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.faq.FaqResponse;
import vn.springboot.entity.faq.FaqEntity;

/**
 * Maps {@link FaqEntity} to its API representation. All exposed fields map by
 * name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface FaqMapper {

    FaqResponse toResponse(FaqEntity entity);
}
