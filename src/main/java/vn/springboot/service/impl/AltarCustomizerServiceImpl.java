package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.dto.response.altar.AltarCustomizerItemResponse;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.enums.CategoryType;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.AltarPlacementMapper;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.AltarCustomizerService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AltarCustomizerServiceImpl implements AltarCustomizerService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final AltarPlacementRepository altarPlacementRepository;
    private final AltarPlacementMapper altarPlacementMapper;

    /**
     * Three bounded queries total, independent of result size (no per-product loop):
     * (1) products + productCategory/altarItemGroup/altarStyle via fetch join,
     * (2) every image for those products in one IN query, reduced in-memory to the first
     *     (priority ASC, id ASC) image per product,
     * (3) placements for those first-image ids in one IN query.
     * A true single-query "first image per product" join needs a window function / correlated
     * subquery that JPQL doesn't support cleanly; three fixed round trips is the documented
     * trade-off (see phase-02 backend report).
     */
    @Override
    @Transactional(readOnly = true)
    public List<AltarCustomizerItemResponse> getItems(Long altarItemGroupId, Long altarStyleId) {
        List<ProductEntity> products = productRepository.findAltarCustomizerItems(
                ProductStatus.PUBLISHED, CategoryType.BO_DO_THO, altarItemGroupId, altarStyleId);

        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream().map(ProductEntity::getId).toList();

        Map<Long, ProductImageEntity> firstImageByProductId = new LinkedHashMap<>();
        for (ProductImageEntity image :
                productImageRepository.findByProductIdInOrderByProductIdAscPriorityAscIdAsc(productIds)) {
            firstImageByProductId.putIfAbsent(image.getProduct().getId(), image);
        }

        List<Long> firstImageIds = firstImageByProductId.values().stream().map(ProductImageEntity::getId).toList();

        Map<Long, AltarPlacementEntity> placementByImageId = firstImageIds.isEmpty()
                ? Map.of()
                : altarPlacementRepository.findByProductImageIdIn(firstImageIds).stream()
                        .collect(Collectors.toMap(placement -> placement.getProductImage().getId(), p -> p));

        return products.stream()
                .map(product -> toItemResponse(product, firstImageByProductId.get(product.getId()), placementByImageId))
                .toList();
    }

    private AltarCustomizerItemResponse toItemResponse(
            ProductEntity product,
            ProductImageEntity firstImage,
            Map<Long, AltarPlacementEntity> placementByImageId) {
        AltarPlacementEntity placementEntity = firstImage != null ? placementByImageId.get(firstImage.getId()) : null;
        AltarPlacementResponse placement = placementEntity != null ? altarPlacementMapper.toResponse(placementEntity) : null;

        return AltarCustomizerItemResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .price(product.getPrice())
                .thumb(product.getThumb())
                .groupId(product.getAltarItemGroup() != null ? product.getAltarItemGroup().getId() : null)
                .styleId(product.getAltarStyle() != null ? product.getAltarStyle().getId() : null)
                .renderOnAltar(product.getAltarItemGroup() != null && product.getAltarItemGroup().isRenderOnAltar())
                .placement(placement)
                .build();
    }
}
