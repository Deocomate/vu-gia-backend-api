package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.order.OrderResponse;
import vn.springboot.entity.order.OrderEntity;

/**
 * Maps {@link OrderEntity} scalar fields to its API representation. {@code items}
 * are attached by the service (order lines are queried separately, since the entity
 * holds no items collection). MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payment", ignore = true)
    OrderResponse toResponse(OrderEntity entity);
}
