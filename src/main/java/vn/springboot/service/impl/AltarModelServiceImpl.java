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
import vn.springboot.dto.request.altar.AltarModelCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSearchRequest;
import vn.springboot.dto.request.altar.AltarModelSizeCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSizeUpdateRequest;
import vn.springboot.dto.request.altar.AltarModelUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarModelResponse;
import vn.springboot.dto.response.altar.AltarModelSizeResponse;
import vn.springboot.entity.altar.AltarModelEntity;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.mapper.AltarModelMapper;
import vn.springboot.mapper.AltarModelSizeMapper;
import vn.springboot.repository.AltarDesignRepository;
import vn.springboot.repository.AltarModelRepository;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.specification.AltarModelSpecification;
import vn.springboot.service.AltarModelService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AltarModelServiceImpl implements AltarModelService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "priority", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "priority";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final AltarModelRepository altarModelRepository;
    private final AltarModelSizeRepository altarModelSizeRepository;
    private final AltarPresetRepository altarPresetRepository;
    private final AltarDesignRepository altarDesignRepository;
    private final AltarModelMapper altarModelMapper;
    private final AltarModelSizeMapper altarModelSizeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AltarModelResponse> search(AltarModelSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<AltarModelEntity> specification = AltarModelSpecification.build(request);

        Page<AltarModelEntity> page = altarModelRepository.findAll(specification, pageable);

        List<AltarModelResponse> content = page.getContent().stream()
                .map(this::toDetailResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public AltarModelResponse getById(Long id) {
        AltarModelEntity entity = findModelOrThrow(id);
        return toDetailResponse(entity);
    }

    @Override
    @Transactional
    public AltarModelResponse create(AltarModelCreateRequest request) {
        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (altarModelRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.ALTAR_MODEL_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getName()));
        }

        AltarModelEntity entity = AltarModelEntity.builder()
                .name(request.getName())
                .slug(slug)
                .thumb(request.getThumb())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return toDetailResponse(altarModelRepository.save(entity));
    }

    @Override
    @Transactional
    public AltarModelResponse update(Long id, AltarModelUpdateRequest request) {
        AltarModelEntity entity = findModelOrThrow(id);

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (altarModelRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.ALTAR_MODEL_SLUG_EXISTED);
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

        return toDetailResponse(altarModelRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AltarModelEntity entity = findModelOrThrow(id);

        // Cascade: sizes carry a not-null FK to the model, so they must go first. But a preset or
        // a customer's saved design can pin to one of those sizes (not-null FK on both) — block,
        // don't silently corrupt, before any mutation, exactly like the product/preset-item guard.
        List<AltarModelSizeEntity> sizes =
                altarModelSizeRepository.findByAltarModelIdOrderByPriorityAscIdAsc(id);
        sizes.forEach(size -> guardSizeNotReferenced(size.getId()));
        altarModelSizeRepository.deleteAll(sizes);

        altarModelRepository.delete(entity);
    }

    /** See {@link #delete(Long)} and {@link #deleteSize(Long, Long)} — a size referenced by a
     * preset or saved design must not be deletable. */
    private void guardSizeNotReferenced(Long sizeId) {
        if (altarPresetRepository.existsByAltarModelSize_Id(sizeId)
                || altarDesignRepository.existsByAltarModelSize_Id(sizeId)) {
            throw new AppException(ErrorCode.ALTAR_MODEL_SIZE_REFERENCED);
        }
    }

    @Override
    @Transactional
    public AltarModelSizeResponse createSize(Long modelId, AltarModelSizeCreateRequest request) {
        AltarModelEntity model = findModelOrThrow(modelId);

        validateSurfaceRect(
                request.getSurfaceLeft(), request.getSurfaceTop(),
                request.getSurfaceRight(), request.getSurfaceBottom(), request.getSurfaceWidthCm());

        AltarModelSizeEntity entity = AltarModelSizeEntity.builder()
                .altarModel(model)
                .label(request.getLabel())
                .widthCm(request.getWidthCm())
                .depthCm(request.getDepthCm())
                .backgroundImage(request.getBackgroundImage())
                .surfaceLeft(request.getSurfaceLeft())
                .surfaceTop(request.getSurfaceTop())
                .surfaceRight(request.getSurfaceRight())
                .surfaceBottom(request.getSurfaceBottom())
                .surfaceWidthCm(request.getSurfaceWidthCm())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return altarModelSizeMapper.toResponse(altarModelSizeRepository.save(entity));
    }

    @Override
    @Transactional
    public AltarModelSizeResponse updateSize(Long modelId, Long sizeId, AltarModelSizeUpdateRequest request) {
        findModelOrThrow(modelId);
        AltarModelSizeEntity entity = findSizeOrThrow(modelId, sizeId);

        if (request.getLabel() != null) {
            entity.setLabel(request.getLabel());
        }
        if (request.getWidthCm() != null) {
            entity.setWidthCm(request.getWidthCm());
        }
        if (request.getDepthCm() != null) {
            entity.setDepthCm(request.getDepthCm());
        }
        if (request.getBackgroundImage() != null) {
            entity.setBackgroundImage(request.getBackgroundImage());
        }
        if (request.getSurfaceLeft() != null) {
            entity.setSurfaceLeft(request.getSurfaceLeft());
        }
        if (request.getSurfaceTop() != null) {
            entity.setSurfaceTop(request.getSurfaceTop());
        }
        if (request.getSurfaceRight() != null) {
            entity.setSurfaceRight(request.getSurfaceRight());
        }
        if (request.getSurfaceBottom() != null) {
            entity.setSurfaceBottom(request.getSurfaceBottom());
        }
        if (request.getSurfaceWidthCm() != null) {
            entity.setSurfaceWidthCm(request.getSurfaceWidthCm());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        // Re-validate the merged (existing + patched) rect — a partial update could still
        // produce a malformed rect (e.g. only surfaceRight patched below the existing left).
        validateSurfaceRect(
                entity.getSurfaceLeft(), entity.getSurfaceTop(),
                entity.getSurfaceRight(), entity.getSurfaceBottom(), entity.getSurfaceWidthCm());

        return altarModelSizeMapper.toResponse(altarModelSizeRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteSize(Long modelId, Long sizeId) {
        findModelOrThrow(modelId);
        AltarModelSizeEntity entity = findSizeOrThrow(modelId, sizeId);
        guardSizeNotReferenced(sizeId);
        altarModelSizeRepository.delete(entity);
    }

    private AltarModelEntity findModelOrThrow(Long id) {
        return altarModelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_MODEL_NOT_FOUND));
    }

    private AltarModelSizeEntity findSizeOrThrow(Long modelId, Long sizeId) {
        AltarModelSizeEntity entity = altarModelSizeRepository.findById(sizeId)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_MODEL_SIZE_NOT_FOUND));
        if (entity.getAltarModel() == null || !entity.getAltarModel().getId().equals(modelId)) {
            throw new AppException(ErrorCode.ALTAR_MODEL_SIZE_NOT_FOUND);
        }
        return entity;
    }

    /**
     * Cross-field surface-rect validation the surface-rect fields aren't bean-validation-friendly
     * for (relations between fields): {@code surfaceLeft < surfaceRight}, {@code surfaceTop <
     * surfaceBottom}, {@code surfaceWidthCm > 0}. A malformed rect divides by zero downstream in
     * the placement renderer.
     */
    private void validateSurfaceRect(Double left, Double top, Double right, Double bottom, Integer surfaceWidthCm) {
        if (left == null || top == null || right == null || bottom == null || surfaceWidthCm == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Surface rect fields are required");
        }
        if (left >= right) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "surfaceLeft must be less than surfaceRight");
        }
        if (top >= bottom) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "surfaceTop must be less than surfaceBottom");
        }
        if (surfaceWidthCm <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "surfaceWidthCm must be greater than 0");
        }
    }

    /** Builds a full response including the embedded, priority-ordered size list. */
    private AltarModelResponse toDetailResponse(AltarModelEntity entity) {
        AltarModelResponse response = altarModelMapper.toResponse(entity);
        List<AltarModelSizeResponse> sizes =
                altarModelSizeRepository.findByAltarModelIdOrderByPriorityAscIdAsc(entity.getId()).stream()
                        .map(altarModelSizeMapper::toResponse)
                        .toList();
        response.setSizes(sizes);
        return response;
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (altarModelRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }
}
