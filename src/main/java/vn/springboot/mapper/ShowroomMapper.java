package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.showroom.ShowroomResponse;
import vn.springboot.entity.showroom.ShowroomEntity;

/**
 * Maps {@link ShowroomEntity} to its API representation. All exposed fields
 * map by name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface ShowroomMapper {

    ShowroomResponse toResponse(ShowroomEntity entity);
}
