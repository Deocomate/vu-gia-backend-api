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
import vn.springboot.dto.request.altar.AltarItemGroupCreateRequest;
import vn.springboot.dto.request.altar.AltarItemGroupSearchRequest;
import vn.springboot.dto.request.altar.AltarItemGroupUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarItemGroupResponse;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.mapper.AltarItemGroupMapper;
import vn.springboot.repository.AltarItemGroupRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.repository.specification.AltarItemGroupSpecification;
import vn.springboot.service.AltarItemGroupService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AltarItemGroupServiceImpl implements AltarItemGroupService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "priority", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "priority";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final AltarItemGroupRepository altarItemGroupRepository;
    private final ProductRepository productRepository;
    private final AltarItemGroupMapper altarItemGroupMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AltarItemGroupResponse> search(AltarItemGroupSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<AltarItemGroupEntity> specification = AltarItemGroupSpecification.build(request);

        Page<AltarItemGroupEntity> page = altarItemGroupRepository.findAll(specification, pageable);

        List<AltarItemGroupResponse> content = page.getContent().stream()
                .map(altarItemGroupMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public AltarItemGroupResponse getById(Long id) {
        return altarItemGroupRepository.findById(id)
                .map(altarItemGroupMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_ITEM_GROUP_NOT_FOUND));
    }

    @Override
    @Transactional
    public AltarItemGroupResponse create(AltarItemGroupCreateRequest request) {
        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (altarItemGroupRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.ALTAR_ITEM_GROUP_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getName()));
        }

        AltarItemGroupEntity entity = AltarItemGroupEntity.builder()
                .name(request.getName())
                .slug(slug)
                .thumb(request.getThumb())
                .renderOnAltar(request.getRenderOnAltar() != null ? request.getRenderOnAltar() : true)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return altarItemGroupMapper.toResponse(altarItemGroupRepository.save(entity));
    }

    @Override
    @Transactional
    public AltarItemGroupResponse update(Long id, AltarItemGroupUpdateRequest request) {
        AltarItemGroupEntity entity = altarItemGroupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_ITEM_GROUP_NOT_FOUND));

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (altarItemGroupRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.ALTAR_ITEM_GROUP_SLUG_EXISTED);
                }
                entity.setSlug(newSlug);
            }
        }
        if (request.getThumb() != null) {
            entity.setThumb(request.getThumb());
        }
        if (request.getRenderOnAltar() != null) {
            entity.setRenderOnAltar(request.getRenderOnAltar());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return altarItemGroupMapper.toResponse(altarItemGroupRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AltarItemGroupEntity entity = altarItemGroupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_ITEM_GROUP_NOT_FOUND));

        // ON DELETE SET NULL semantics: referencing products survive with a null FK.
        productRepository.nullifyAltarItemGroupReferences(id);
        altarItemGroupRepository.delete(entity);
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (altarItemGroupRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }
}
