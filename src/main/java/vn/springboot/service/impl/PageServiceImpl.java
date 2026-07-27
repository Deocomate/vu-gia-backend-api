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
import vn.springboot.dto.request.page.PageCreateRequest;
import vn.springboot.dto.request.page.PageSearchRequest;
import vn.springboot.dto.request.page.PageUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.page.PageDetailResponse;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.page.PageEntity;
import vn.springboot.mapper.PageMapper;
import vn.springboot.repository.PageRepository;
import vn.springboot.repository.specification.PageSpecification;
import vn.springboot.service.PageService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "key", "title", "status", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PageDetailResponse> search(PageSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<PageEntity> specification = PageSpecification.build(request);

        Page<PageEntity> page = pageRepository.findAll(specification, pageable);

        List<PageDetailResponse> content = page.getContent().stream()
                .map(pageMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDetailResponse getById(Long id) {
        return pageRepository.findById(id)
                .map(pageMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDetailResponse getByKey(String key) {
        return pageRepository.findByKey(key)
                .map(pageMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));
    }

    @Override
    @Transactional
    public PageDetailResponse create(PageCreateRequest request) {
        if (pageRepository.existsByKey(request.getKey())) {
            throw new AppException(ErrorCode.PAGE_KEY_EXISTED);
        }

        PageEntity entity = PageEntity.builder()
                .key(request.getKey())
                .title(request.getTitle())
                .content(request.getContent())
                .heroTitle(request.getHeroTitle())
                .heroSubtitle(request.getHeroSubtitle())
                .heroDes(request.getHeroDes())
                .heroImage(request.getHeroImage())
                .status(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT)
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .seoImage(request.getSeoImage())
                .build();

        return pageMapper.toResponse(pageRepository.save(entity));
    }

    @Override
    @Transactional
    public PageDetailResponse update(Long id, PageUpdateRequest request) {
        PageEntity entity = pageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));

        if (request.getKey() != null && !request.getKey().equals(entity.getKey())) {
            if (pageRepository.existsByKey(request.getKey())) {
                throw new AppException(ErrorCode.PAGE_KEY_EXISTED);
            }
            entity.setKey(request.getKey());
        }
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            entity.setContent(request.getContent());
        }
        if (request.getHeroTitle() != null) {
            entity.setHeroTitle(request.getHeroTitle());
        }
        if (request.getHeroSubtitle() != null) {
            entity.setHeroSubtitle(request.getHeroSubtitle());
        }
        if (request.getHeroDes() != null) {
            entity.setHeroDes(request.getHeroDes());
        }
        if (request.getHeroImage() != null) {
            entity.setHeroImage(request.getHeroImage());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
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

        return pageMapper.toResponse(pageRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PageEntity entity = pageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));

        pageRepository.delete(entity);
    }
}
