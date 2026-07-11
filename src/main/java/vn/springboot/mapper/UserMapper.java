package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.user.UserResponse;
import vn.springboot.entity.user.UserEntity;

/**
 * Maps {@link UserEntity} to its API representation. All exposed fields
 * (id, username, email, name, phone, gender, dob, avatar, provider, role,
 * createdAt) map by name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserEntity user);
}
