package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.common.util.SlugUtils;
import vn.springboot.dto.request.product.ProductCreateRequest;
import vn.springboot.dto.request.product.ProductImageRequest;
import vn.springboot.dto.request.product.ProductSearchRequest;
import vn.springboot.dto.request.product.ProductUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductImageResponse;
import vn.springboot.dto.response.product.ProductResponse;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.ProductMapper;
import vn.springboot.repository.ProductCategoryRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.specification.ProductSpecification;
import vn.springboot.service.FileStorageService;
import vn.springboot.service.ProductService;

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
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(ProductSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<ProductEntity> specification = ProductSpecification.build(request);

        Page<ProductEntity> page = productRepository.findAll(specification, pageable);

        // Images are intentionally omitted from list responses.
        List<ProductResponse> content = page.getContent().stream()
                .map(productMapper::toResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
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

        ProductEntity entity = ProductEntity.builder()
                .name(request.getName())
                .thumb(request.getThumb())
                .sku(sku)
                .type(request.getType())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT)
                .description(request.getDescription())
                .comboProducts(request.getComboProducts())
                .slug(slug)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .productCategory(category)
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .seoImage(request.getSeoImage())
                .build();

        ProductEntity saved = productRepository.save(entity);
        persistImages(saved, request.getImages());
        return toDetailResponse(saved);
    }

    /** Persists gallery images supplied inline at create time (URLs already uploaded). */
    private void persistImages(ProductEntity product, List<ProductImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            ProductImageRequest img = images.get(i);
            if (img == null || img.getUrl() == null || img.getUrl().isBlank()) {
                continue;
            }
            productImageRepository.save(ProductImageEntity.builder()
                    .url(img.getUrl().trim())
                    .priority(img.getPriority() != null ? img.getPriority() : i)
                    .product(product)
                    .build());
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

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getThumb() != null) {
            entity.setThumb(request.getThumb());
        }
        if (request.getType() != null) {
            entity.setType(request.getType());
        }
        if (request.getPrice() != null) {
            entity.setPrice(request.getPrice());
        }
        if (request.getCompareAtPrice() != null) {
            entity.setCompareAtPrice(request.getCompareAtPrice());
        }
        if (request.getIsFeatured() != null) {
            entity.setFeatured(request.getIsFeatured());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getComboProducts() != null) {
            entity.setComboProducts(request.getComboProducts());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        if (request.getSeoTitle() != null) {
            entity.setSeoTitle(request.getSeoTitle());
        }
        if (request.getSeoDescription() != null) {
            entity.setSeoDescription(request.getSeoDescription());
        }
        if (request.getSeoImage() != null) {
            entity.setSeoImage(request.getSeoImage());
        }

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

        var images = productImageRepository.findByProductIdOrderByPriorityAscIdAsc(id);
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

    private Sort resolveSort(ProductSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
