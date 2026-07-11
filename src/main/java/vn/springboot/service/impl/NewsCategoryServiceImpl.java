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
import vn.springboot.dto.request.news.NewsCategoryCreateRequest;
import vn.springboot.dto.request.news.NewsCategorySearchRequest;
import vn.springboot.dto.request.news.NewsCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsCategoryResponse;
import vn.springboot.entity.news.NewsCategoryEntity;
import vn.springboot.mapper.NewsCategoryMapper;
import vn.springboot.repository.NewsCategoryRepository;
import vn.springboot.repository.NewsRepository;
import vn.springboot.repository.specification.NewsCategorySpecification;
import vn.springboot.service.NewsCategoryService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewsCategoryServiceImpl implements NewsCategoryService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "priority", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final NewsCategoryRepository newsCategoryRepository;
    private final NewsRepository newsRepository;
    private final NewsCategoryMapper newsCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsCategoryResponse> search(NewsCategorySearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<NewsCategoryEntity> specification = NewsCategorySpecification.build(request);

        Page<NewsCategoryEntity> page = newsCategoryRepository.findAll(specification, pageable);

        List<NewsCategoryResponse> content = page.getContent().stream()
                .map(newsCategoryMapper::toResponse)
                .toList();

        return PageResponse.<NewsCategoryResponse>builder()
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
    public NewsCategoryResponse getById(Long id) {
        return newsCategoryRepository.findById(id)
                .map(newsCategoryMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public NewsCategoryResponse getBySlug(String slug) {
        return newsCategoryRepository.findBySlug(slug)
                .map(newsCategoryMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional
    public NewsCategoryResponse create(NewsCategoryCreateRequest request) {
        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (newsCategoryRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.NEWS_CATEGORY_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getName()));
        }

        NewsCategoryEntity entity = NewsCategoryEntity.builder()
                .name(request.getName())
                .slug(slug)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .build();

        return newsCategoryMapper.toResponse(newsCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public NewsCategoryResponse update(Long id, NewsCategoryUpdateRequest request) {
        NewsCategoryEntity entity = newsCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_CATEGORY_NOT_FOUND));

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (newsCategoryRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.NEWS_CATEGORY_SLUG_EXISTED);
                }
                entity.setSlug(newSlug);
            }
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }

        return newsCategoryMapper.toResponse(newsCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NewsCategoryEntity entity = newsCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_CATEGORY_NOT_FOUND));

        if (newsRepository.existsByNewsCategoryId(id)) {
            throw new AppException(ErrorCode.NEWS_CATEGORY_HAS_NEWS);
        }

        newsCategoryRepository.delete(entity);
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (newsCategoryRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private Sort resolveSort(NewsCategorySearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
