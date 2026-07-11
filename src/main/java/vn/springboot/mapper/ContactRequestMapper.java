package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.contact.ContactRequestResponse;
import vn.springboot.entity.contact.ContactRequestEntity;

/**
 * Maps {@link ContactRequestEntity} to its API representation. The {@code handledBy}
 * relation is flattened to its id/username (null-safe when unassigned). MapStruct
 * generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface ContactRequestMapper {

    @Mapping(target = "handledById", source = "handledBy.id")
    @Mapping(target = "handledByUsername", source = "handledBy.username")
    ContactRequestResponse toResponse(ContactRequestEntity entity);
}
