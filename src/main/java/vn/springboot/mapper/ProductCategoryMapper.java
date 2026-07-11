package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.product.ProductCategoryResponse;
import vn.springboot.entity.product.ProductCategoryEntity;

/**
 * Maps {@link ProductCategoryEntity} to its API representation. All exposed
 * fields map by name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {

    ProductCategoryResponse toResponse(ProductCategoryEntity entity);
}
