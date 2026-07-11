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
import vn.springboot.dto.request.product.ProductCategoryCreateRequest;
import vn.springboot.dto.request.product.ProductCategorySearchRequest;
import vn.springboot.dto.request.product.ProductCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductCategoryResponse;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.mapper.ProductCategoryMapper;
import vn.springboot.repository.ProductCategoryRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.specification.ProductCategorySpecification;
import vn.springboot.service.ProductCategoryService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "priority", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductCategoryResponse> search(ProductCategorySearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<ProductCategoryEntity> specification = ProductCategorySpecification.build(request);

        Page<ProductCategoryEntity> page = productCategoryRepository.findAll(specification, pageable);

        List<ProductCategoryResponse> content = page.getContent().stream()
                .map(productCategoryMapper::toResponse)
                .toList();

        return PageResponse.<ProductCategoryResponse>builder()
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
    public ProductCategoryResponse getById(Long id) {
        return productCategoryRepository.findById(id)
                .map(productCategoryMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryResponse getBySlug(String slug) {
        return productCategoryRepository.findBySlug(slug)
                .map(productCategoryMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional
    public ProductCategoryResponse create(ProductCategoryCreateRequest request) {
        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (productCategoryRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.PRODUCT_CATEGORY_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getName()));
        }

        ProductCategoryEntity entity = ProductCategoryEntity.builder()
                .name(request.getName())
                .thumb(request.getThumb())
                .slug(slug)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .longContent(request.getLongContent())
                .des(request.getDes())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .seoImage(request.getSeoImage())
                .build();

        return productCategoryMapper.toResponse(productCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductCategoryResponse update(Long id, ProductCategoryUpdateRequest request) {
        ProductCategoryEntity entity = productCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (productCategoryRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.PRODUCT_CATEGORY_SLUG_EXISTED);
                }
                entity.setSlug(newSlug);
            }
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getThumb() != null) {
            entity.setThumb(request.getThumb());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        if (request.getLongContent() != null) {
            entity.setLongContent(request.getLongContent());
        }
        if (request.getDes() != null) {
            entity.setDes(request.getDes());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
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

        return productCategoryMapper.toResponse(productCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ProductCategoryEntity entity = productCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        if (productRepository.existsByProductCategoryId(id)) {
            throw new AppException(ErrorCode.PRODUCT_CATEGORY_HAS_PRODUCTS);
        }

        productCategoryRepository.delete(entity);
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (productCategoryRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private Sort resolveSort(ProductCategorySearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
