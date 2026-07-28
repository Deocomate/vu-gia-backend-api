package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.common.util.PaginationUtils;
import vn.springboot.common.util.SlugUtils;
import vn.springboot.dto.request.altar.AltarPresetItemRequest;
import vn.springboot.dto.request.altar.AltarPresetRequest;
import vn.springboot.dto.request.altar.AltarPresetSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.dto.response.altar.AltarPresetItemResponse;
import vn.springboot.dto.response.altar.AltarPresetResponse;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.altar.AltarPresetEntity;
import vn.springboot.entity.altar.AltarPresetItemEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.AltarPlacementMapper;
import vn.springboot.mapper.AltarPresetMapper;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.AltarPresetItemRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.specification.AltarPresetSpecification;
import vn.springboot.service.AltarPresetService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AltarPresetServiceImpl implements AltarPresetService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "priority", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "priority";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final AltarPresetRepository altarPresetRepository;
    private final AltarPresetItemRepository altarPresetItemRepository;
    private final AltarModelSizeRepository altarModelSizeRepository;
    private final AltarStyleRepository altarStyleRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final AltarPlacementRepository altarPlacementRepository;
    private final AltarPresetMapper altarPresetMapper;
    private final AltarPlacementMapper altarPlacementMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AltarPresetResponse> search(AltarPresetSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<AltarPresetEntity> specification = AltarPresetSpecification.build(request);

        Page<AltarPresetEntity> page = altarPresetRepository.findAll(specification, pageable);

        List<AltarPresetResponse> content = page.getContent().stream()
                .map(this::toDetailResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public AltarPresetResponse getById(Long id) {
        return toDetailResponse(findPresetOrThrow(id));
    }

    @Override
    @Transactional
    public AltarPresetResponse create(AltarPresetRequest request) {
        AltarModelSizeEntity altarModelSize = findAltarModelSizeOrThrow(request.getAltarModelSizeId());
        AltarStyleEntity altarStyle = resolveAltarStyle(request.getAltarStyleId());

        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (altarPresetRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.ALTAR_PRESET_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getName()));
        }

        AltarPresetEntity entity = AltarPresetEntity.builder()
                .name(request.getName())
                .slug(slug)
                .thumb(request.getThumb())
                .description(request.getDescription())
                .altarModelSize(altarModelSize)
                .altarStyle(altarStyle)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        AltarPresetEntity saved = altarPresetRepository.save(entity);
        replaceItems(saved, request.getItems());

        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public AltarPresetResponse update(Long id, AltarPresetRequest request) {
        AltarPresetEntity entity = findPresetOrThrow(id);

        entity.setAltarModelSize(findAltarModelSizeOrThrow(request.getAltarModelSizeId()));
        entity.setAltarStyle(resolveAltarStyle(request.getAltarStyleId()));
        entity.setName(request.getName());
        entity.setThumb(request.getThumb());
        entity.setDescription(request.getDescription());
        entity.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        entity.setActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (altarPresetRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.ALTAR_PRESET_SLUG_EXISTED);
                }
                entity.setSlug(newSlug);
            }
        }

        AltarPresetEntity saved = altarPresetRepository.save(entity);

        // Full-replace semantics: delete every existing item row, then insert the new set —
        // simplest correct semantics for a canvas save (see phase-03 API surface note).
        List<AltarPresetItemEntity> existingItems =
                altarPresetItemRepository.findByPresetIdOrderBySortOrderAscIdAsc(id);
        altarPresetItemRepository.deleteAll(existingItems);
        replaceItems(saved, request.getItems());

        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AltarPresetEntity entity = findPresetOrThrow(id);

        // Cascade: items carry a not-null FK to the preset, so they must go first (same
        // unidirectional-FK + explicit-service-delete style as AltarModelServiceImpl#delete).
        List<AltarPresetItemEntity> items =
                altarPresetItemRepository.findByPresetIdOrderBySortOrderAscIdAsc(id);
        altarPresetItemRepository.deleteAll(items);

        altarPresetRepository.delete(entity);
    }

    /** Resolves FKs, validates the invariant, and persists the requested items for {@code preset}. */
    private void replaceItems(AltarPresetEntity preset, List<AltarPresetItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return;
        }

        List<AltarPresetItemEntity> items = new ArrayList<>(itemRequests.size());
        for (int index = 0; index < itemRequests.size(); index++) {
            AltarPresetItemRequest itemRequest = itemRequests.get(index);
            validateItemInvariant(itemRequest);

            ProductEntity product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            ProductImageEntity productImage = null;
            if (itemRequest.getProductImageId() != null) {
                productImage = productImageRepository.findById(itemRequest.getProductImageId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
                if (productImage.getProduct() == null || !productImage.getProduct().getId().equals(product.getId())) {
                    throw new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
                }
            }

            items.add(AltarPresetItemEntity.builder()
                    .preset(preset)
                    .product(product)
                    .productImage(productImage)
                    .quantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1)
                    .x(itemRequest.getX())
                    .y(itemRequest.getY())
                    .scaleAdjust(itemRequest.getScaleAdjust() != null ? itemRequest.getScaleAdjust() : 1.0)
                    .flipped(itemRequest.getFlipped() != null && itemRequest.getFlipped())
                    .zIndexOverride(itemRequest.getZIndexOverride())
                    .sortOrder(itemRequest.getSortOrder() != null ? itemRequest.getSortOrder() : index)
                    .build());
        }

        altarPresetItemRepository.saveAll(items);
    }

    /**
     * Cross-field rule bean validation can't express: {@code productImageId != null <=>
     * (x != null && y != null)}. A canvas item without coordinates is meaningless; an accessory
     * with coordinates is misleading — both are a 400.
     */
    private void validateItemInvariant(AltarPresetItemRequest item) {
        boolean hasImage = item.getProductImageId() != null;
        boolean hasX = item.getX() != null;
        boolean hasY = item.getY() != null;
        if (hasX != hasY) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "x and y must both be set or both be null");
        }
        boolean hasCoords = hasX && hasY;
        if (hasImage && !hasCoords) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "productImageId requires both x and y (canvas item)");
        }
        if (!hasImage && hasCoords) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "x/y require productImageId — accessory items must not carry coordinates");
        }
    }

    private AltarPresetEntity findPresetOrThrow(Long id) {
        return altarPresetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_PRESET_NOT_FOUND));
    }

    private AltarModelSizeEntity findAltarModelSizeOrThrow(Long id) {
        return altarModelSizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_MODEL_SIZE_NOT_FOUND));
    }

    private AltarStyleEntity resolveAltarStyle(Long id) {
        if (id == null) {
            return null;
        }
        return altarStyleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_STYLE_NOT_FOUND));
    }

    /** Builds a full response including the ordered item list and the derived price summary. */
    private AltarPresetResponse toDetailResponse(AltarPresetEntity entity) {
        AltarPresetResponse response = altarPresetMapper.toResponse(entity);

        List<AltarPresetItemEntity> itemEntities =
                altarPresetItemRepository.findByPresetIdOrderBySortOrderAscIdAsc(entity.getId());

        List<Long> imageIds = itemEntities.stream()
                .map(AltarPresetItemEntity::getProductImage)
                .filter(Objects::nonNull)
                .map(ProductImageEntity::getId)
                .toList();
        Map<Long, AltarPlacementEntity> placementByImageId = imageIds.isEmpty()
                ? Map.of()
                : altarPlacementRepository.findByProductImageIdIn(imageIds).stream()
                        .collect(Collectors.toMap(placement -> placement.getProductImage().getId(), p -> p));

        List<AltarPresetItemResponse> items = itemEntities.stream()
                .map(item -> toItemResponse(item, placementByImageId))
                .toList();

        long priceSummary = items.stream()
                .mapToLong(item -> (item.getProductPrice() != null ? item.getProductPrice() : 0L)
                        * (item.getQuantity() != null ? item.getQuantity() : 0))
                .sum();

        response.setItems(items);
        response.setPriceSummary(priceSummary);
        return response;
    }

    private AltarPresetItemResponse toItemResponse(
            AltarPresetItemEntity item, Map<Long, AltarPlacementEntity> placementByImageId) {
        ProductEntity product = item.getProduct();
        Long productImageId = item.getProductImage() != null ? item.getProductImage().getId() : null;
        AltarPlacementEntity placementEntity = productImageId != null ? placementByImageId.get(productImageId) : null;
        AltarPlacementResponse placement = placementEntity != null ? altarPlacementMapper.toResponse(placementEntity) : null;

        return AltarPresetItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .productThumb(product.getThumb())
                .productPrice(product.getPrice())
                .productImageId(productImageId)
                .placement(placement)
                .quantity(item.getQuantity())
                .x(item.getX())
                .y(item.getY())
                .scaleAdjust(item.getScaleAdjust())
                .flipped(item.isFlipped())
                .zIndexOverride(item.getZIndexOverride())
                .sortOrder(item.getSortOrder())
                .build();
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (altarPresetRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }
}
