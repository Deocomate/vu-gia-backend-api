package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.product.ProductCategoryBriefResponse;
import vn.springboot.dto.response.product.ProductImageResponse;
import vn.springboot.dto.response.product.ProductResponse;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;

/**
 * Maps product entities to their API representations. {@code images} is
 * populated manually by the service (detail views only), so it is ignored here.
 * MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "category", source = "productCategory")
    @Mapping(target = "images", ignore = true)
    ProductResponse toResponse(ProductEntity entity);

    ProductCategoryBriefResponse toBrief(ProductCategoryEntity entity);

    ProductImageResponse toImageResponse(ProductImageEntity entity);
}
