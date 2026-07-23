package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.order.OrderResponse;
import vn.springboot.entity.order.OrderEntity;

/**
 * Maps {@link OrderEntity} scalar fields to its API representation. {@code items}
 * are attached by the service (order lines are queried separately, since the entity
 * holds no items collection). MapStruct generates the Spring bean at compile time.
 *
 * <p>{@code shippingMethodName} is also attached by the service (via a fresh
 * repository lookup), not mapped from {@code entity.shippingMethod.name} directly:
 * {@code shippingMethod} is a lazy {@code @ManyToOne}, and the idempotency-retry path
 * in {@code OrderServiceImpl.placeOrder} deliberately re-reads the order outside any
 * transaction (see that method's javadoc) — touching the lazy proxy there throws
 * {@code LazyInitializationException}.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "shippingMethodName", ignore = true)
    OrderResponse toResponse(OrderEntity entity);
}
