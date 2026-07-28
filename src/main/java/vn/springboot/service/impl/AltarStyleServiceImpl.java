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
import vn.springboot.dto.request.altar.AltarStyleCreateRequest;
import vn.springboot.dto.request.altar.AltarStyleSearchRequest;
import vn.springboot.dto.request.altar.AltarStyleUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarStyleResponse;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.mapper.AltarStyleMapper;
import vn.springboot.repository.AltarDesignRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.specification.AltarStyleSpecification;
import vn.springboot.service.AltarStyleService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AltarStyleServiceImpl implements AltarStyleService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "priority", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "priority";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final AltarStyleRepository altarStyleRepository;
    private final ProductRepository productRepository;
    private final AltarPresetRepository altarPresetRepository;
    private final AltarDesignRepository altarDesignRepository;
    private final AltarStyleMapper altarStyleMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AltarStyleResponse> search(AltarStyleSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<AltarStyleEntity> specification = AltarStyleSpecification.build(request);

        Page<AltarStyleEntity> page = altarStyleRepository.findAll(specification, pageable);

        List<AltarStyleResponse> content = page.getContent().stream()
                .map(altarStyleMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public AltarStyleResponse getById(Long id) {
        return altarStyleRepository.findById(id)
                .map(altarStyleMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_STYLE_NOT_FOUND));
    }

    @Override
    @Transactional
    public AltarStyleResponse create(AltarStyleCreateRequest request) {
        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (altarStyleRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.ALTAR_STYLE_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getName()));
        }

        AltarStyleEntity entity = AltarStyleEntity.builder()
                .name(request.getName())
                .slug(slug)
                .thumb(request.getThumb())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return altarStyleMapper.toResponse(altarStyleRepository.save(entity));
    }

    @Override
    @Transactional
    public AltarStyleResponse update(Long id, AltarStyleUpdateRequest request) {
        AltarStyleEntity entity = altarStyleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_STYLE_NOT_FOUND));

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (altarStyleRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.ALTAR_STYLE_SLUG_EXISTED);
                }
                entity.setSlug(newSlug);
            }
        }
        if (request.getThumb() != null) {
            entity.setThumb(request.getThumb());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return altarStyleMapper.toResponse(altarStyleRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AltarStyleEntity entity = altarStyleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_STYLE_NOT_FOUND));

        // Presets/designs pin to a style as curated/saved intent, unlike a product's style tag —
        // block, don't silently strand a preset or a customer's saved design pointing at nothing.
        if (altarPresetRepository.existsByAltarStyle_Id(id) || altarDesignRepository.existsByAltarStyle_Id(id)) {
            throw new AppException(ErrorCode.ALTAR_STYLE_REFERENCED);
        }

        // ON DELETE SET NULL semantics: referencing products survive with a null FK.
        productRepository.nullifyAltarStyleReferences(id);
        altarStyleRepository.delete(entity);
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (altarStyleRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }
}
