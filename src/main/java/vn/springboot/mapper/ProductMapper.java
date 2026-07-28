package vn.springboot.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.springboot.dto.request.product.ProductCreateRequest;
import vn.springboot.dto.request.product.ProductUpdateRequest;
import vn.springboot.dto.response.product.AltarItemGroupBriefResponse;
import vn.springboot.dto.response.product.AltarStyleBriefResponse;
import vn.springboot.dto.response.product.ProductCategoryBriefResponse;
import vn.springboot.dto.response.product.ProductImageResponse;
import vn.springboot.dto.response.product.ProductResponse;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
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
    @Mapping(target = "altarItemGroup", source = "altarItemGroup")
    @Mapping(target = "altarStyle", source = "altarStyle")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "isFeatured", source = "featured")
    ProductResponse toResponse(ProductEntity entity);

    ProductCategoryBriefResponse toBrief(ProductCategoryEntity entity);

    AltarItemGroupBriefResponse toBrief(AltarItemGroupEntity entity);

    AltarStyleBriefResponse toBrief(AltarStyleEntity entity);

    ProductImageResponse toImageResponse(ProductImageEntity entity);

    /**
     * Straight-copy fields only. The service still resolves derived fields
     * after calling this: {@code sku}/{@code slug} uniqueness, {@code
     * productCategory}/{@code altarItemGroup}/{@code altarStyle} lookups, and
     * the {@code featured}/{@code status}/{@code priority} create-time defaults.
     */
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "productCategory", ignore = true)
    @Mapping(target = "altarItemGroup", ignore = true)
    @Mapping(target = "altarStyle", ignore = true)
    ProductEntity toEntity(ProductCreateRequest request);

    /**
     * Straight-copy, null-safe patch of an existing entity (null request
     * fields keep their current value). The service still resolves {@code
     * sku}/{@code slug} uniqueness checks and {@code productCategory}/{@code
     * altarItemGroup}/{@code altarStyle} lookups separately, since those
     * require repository calls.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "featured", source = "isFeatured")
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "productCategory", ignore = true)
    @Mapping(target = "altarItemGroup", ignore = true)
    @Mapping(target = "altarStyle", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget ProductEntity entity);
}
