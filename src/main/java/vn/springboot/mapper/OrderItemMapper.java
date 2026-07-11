package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.order.OrderItemResponse;
import vn.springboot.entity.order.OrderItemEntity;

/**
 * Maps {@link OrderItemEntity} to its API representation. All values are snapshots
 * stored on the row (name/price/subtotal), so no live product lookup is needed —
 * only the product id is exposed. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse toResponse(OrderItemEntity entity);
}
