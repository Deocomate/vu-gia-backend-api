package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.cart.CartItemResponse;
import vn.springboot.entity.cart.CartItemEntity;

/**
 * Maps {@link CartItemEntity} to its API representation. The {@code product}
 * relation is flattened to the fields the storefront needs, and {@code lineTotal}
 * is derived from the current unit price × quantity. MapStruct generates the
 * Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productThumb", source = "product.thumb")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "unitPrice", source = "product.price")
    @Mapping(target = "lineTotal", expression = "java(entity.getProduct().getPrice() * entity.getQuantity())")
    CartItemResponse toResponse(CartItemEntity entity);
}
