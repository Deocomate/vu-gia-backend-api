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
import vn.springboot.dto.request.product.ProductCreateRequest;
import vn.springboot.dto.request.product.ProductImageRequest;
import vn.springboot.dto.request.product.ProductSearchRequest;
import vn.springboot.dto.request.product.ProductUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductImageResponse;
import vn.springboot.dto.response.product.ProductResponse;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.ProductMapper;
import vn.springboot.repository.AltarItemGroupRepository;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.AltarPresetItemRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductCategoryRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.specification.ProductSpecification;
import vn.springboot.service.FileStorageService;
import vn.springboot.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "name", "price", "priority", "soldCount", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final AltarItemGroupRepository altarItemGroupRepository;
    private final AltarStyleRepository altarStyleRepository;
    private final AltarPresetItemRepository altarPresetItemRepository;
    private final AltarPlacementRepository altarPlacementRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(ProductSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<ProductEntity> specification = ProductSpecification.build(request);

        Page<ProductEntity> page = productRepository.findAll(specification, pageable);

        // Images are intentionally omitted from list responses.
        List<ProductResponse> content = page.getContent().stream()
                .map(productMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toDetailResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        ProductEntity entity = productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toDetailResponse(entity);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        ProductCategoryEntity category = productCategoryRepository.findById(request.getProductCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (productRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.PRODUCT_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getName()));
        }

        String sku = null;
        if (request.getSku() != null && !request.getSku().isBlank()) {
            sku = request.getSku().trim();
            if (productRepository.existsBySku(sku)) {
                throw new AppException(ErrorCode.PRODUCT_SKU_EXISTED);
            }
        }

        ProductEntity entity = productMapper.toEntity(request);
        entity.setSku(sku);
        entity.setSlug(slug);
        entity.setFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT);
        entity.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        entity.setProductCategory(category);
        entity.setAltarItemGroup(resolveAltarItemGroup(request.getAltarItemGroupId()));
        entity.setAltarStyle(resolveAltarStyle(request.getAltarStyleId()));

        ProductEntity saved = productRepository.save(entity);
        persistImages(saved, request.getImages());
        return toDetailResponse(saved);
    }

    /** Persists gallery images supplied inline at create time (URLs already uploaded). */
    private void persistImages(ProductEntity product, List<ProductImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        List<ProductImageEntity> entities = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            ProductImageRequest img = images.get(i);
            if (img == null || img.getUrl() == null || img.getUrl().isBlank()) {
                continue;
            }
            entities.add(ProductImageEntity.builder()
                    .url(img.getUrl().trim())
                    .priority(img.getPriority() != null ? img.getPriority() : i)
                    .product(product)
                    .build());
        }
        if (!entities.isEmpty()) {
            productImageRepository.saveAll(entities);
        }
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getProductCategoryId() != null
                && (entity.getProductCategory() == null
                        || !entity.getProductCategory().getId().equals(request.getProductCategoryId()))) {
            ProductCategoryEntity category = productCategoryRepository.findById(request.getProductCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
            entity.setProductCategory(category);
        }

        if (request.getAltarItemGroupId() != null
                && (entity.getAltarItemGroup() == null
                        || !entity.getAltarItemGroup().getId().equals(request.getAltarItemGroupId()))) {
            entity.setAltarItemGroup(resolveAltarItemGroup(request.getAltarItemGroupId()));
        }

        if (request.getAltarStyleId() != null
                && (entity.getAltarStyle() == null
                        || !entity.getAltarStyle().getId().equals(request.getAltarStyleId()))) {
            entity.setAltarStyle(resolveAltarStyle(request.getAltarStyleId()));
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (productRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.PRODUCT_SLUG_EXISTED);
                }
                entity.setSlug(newSlug);
            }
        }

        if (request.getSku() != null && !request.getSku().isBlank()) {
            String newSku = request.getSku().trim();
            if (!newSku.equals(entity.getSku())) {
                if (productRepository.existsBySkuAndIdNot(newSku, id)) {
                    throw new AppException(ErrorCode.PRODUCT_SKU_EXISTED);
                }
                entity.setSku(newSku);
            }
        }

        productMapper.updateEntityFromRequest(request, entity);

        return toDetailResponse(productRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductResponse updateStatus(Long id, ProductStatus status) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        entity.setStatus(status);
        return toDetailResponse(productRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductResponse updateFeatured(Long id, boolean featured) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        entity.setFeatured(featured);
        return toDetailResponse(productRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Block, don't silently corrupt: a preset that references this product would otherwise
        // be left pointing at nothing. Checked before any mutation so a 409 leaves everything
        // untouched.
        if (altarPresetItemRepository.existsByProductId(id)) {
            List<String> presetNames = altarPresetItemRepository.findDistinctPresetNamesByProductId(id);
            throw new AppException(ErrorCode.ALTAR_PRESET_ITEM_REFERENCED,
                    "Product is referenced by altar preset(s): " + String.join(", ", presetNames));
        }

        var images = productImageRepository.findByProductIdOrderByPriorityAscIdAsc(id);

        // Each image's altar placement (0..1) isn't a DB-level cascade — clean up both halves
        // (row + overlay file) *before* any file deletion below, mirroring
        // ProductImageServiceImpl#deleteImage. Getting this order right matters: file deletion is
        // not part of the @Transactional rollback, so if it ran first and a later step failed, the
        // files would be gone even though the DB rolled back.
        images.forEach(img -> altarPlacementRepository.findByProductImageId(img.getId()).ifPresent(placement -> {
            fileStorageService.delete(placement.getOverlayImage());
            altarPlacementRepository.delete(placement);
        }));

        images.forEach(img -> fileStorageService.delete(img.getUrl()));
        productImageRepository.deleteAll(images);

        productRepository.delete(entity);
    }

    /** Builds a full response including the ordered image gallery. */
    private ProductResponse toDetailResponse(ProductEntity entity) {
        ProductResponse response = productMapper.toResponse(entity);
        List<ProductImageResponse> images =
                productImageRepository.findByProductIdOrderByPriorityAscIdAsc(entity.getId()).stream()
                        .map(productMapper::toImageResponse)
                        .toList();
        response.setImages(images);
        return response;
    }

    /** Optional FK; {@code null} id → {@code null} association (non-altar products leave it unset). */
    private AltarItemGroupEntity resolveAltarItemGroup(Long altarItemGroupId) {
        if (altarItemGroupId == null) {
            return null;
        }
        return altarItemGroupRepository.findById(altarItemGroupId)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_ITEM_GROUP_NOT_FOUND));
    }

    /** Optional FK; {@code null} id → {@code null} association (non-altar products leave it unset). */
    private AltarStyleEntity resolveAltarStyle(Long altarStyleId) {
        if (altarStyleId == null) {
            return null;
        }
        return altarStyleRepository.findById(altarStyleId)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_STYLE_NOT_FOUND));
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (productRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }
}
