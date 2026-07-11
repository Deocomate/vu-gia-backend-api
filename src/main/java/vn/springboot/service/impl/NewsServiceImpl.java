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
import vn.springboot.dto.request.news.NewsCreateRequest;
import vn.springboot.dto.request.news.NewsSearchRequest;
import vn.springboot.dto.request.news.NewsUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsResponse;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.news.NewsCategoryEntity;
import vn.springboot.entity.news.NewsEntity;
import vn.springboot.mapper.NewsMapper;
import vn.springboot.repository.NewsCategoryRepository;
import vn.springboot.repository.NewsRepository;
import vn.springboot.repository.specification.NewsSpecification;
import vn.springboot.service.NewsService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input. {@code title} is a TEXT column and is not sortable.
     */
    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "priority", "viewCount", "publishedAt", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final NewsRepository newsRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final NewsMapper newsMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsResponse> search(NewsSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<NewsEntity> specification = NewsSpecification.build(request);

        // EntityGraph on findAll eager-joins the category, so mapping avoids N+1.
        Page<NewsEntity> page = newsRepository.findAll(specification, pageable);

        List<NewsResponse> content = page.getContent().stream()
                .map(newsMapper::toResponse)
                .toList();

        return PageResponse.<NewsResponse>builder()
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
    public NewsResponse getById(Long id) {
        NewsEntity entity = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        return newsMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public NewsResponse getBySlug(String slug) {
        NewsEntity entity = newsRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));

        // Atomic bump in the DB; reflect it in the response without a re-read.
        newsRepository.incrementViewCount(entity.getId());
        NewsResponse response = newsMapper.toResponse(entity);
        response.setViewCount(entity.getViewCount() + 1);
        return response;
    }

    @Override
    @Transactional
    public NewsResponse create(NewsCreateRequest request) {
        NewsCategoryEntity category = newsCategoryRepository.findById(request.getNewsCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_CATEGORY_NOT_FOUND));

        String slug;
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            slug = request.getSlug().trim();
            if (newsRepository.existsBySlug(slug)) {
                throw new AppException(ErrorCode.NEWS_SLUG_EXISTED);
            }
        } else {
            slug = generateUniqueSlug(SlugUtils.toSlug(request.getTitle()));
        }

        ContentStatus status = request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT;

        NewsEntity entity = NewsEntity.builder()
                .title(request.getTitle())
                .thumb(request.getThumb())
                .shortContent(request.getShortContent())
                .des(request.getDes())
                .slug(slug)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .status(status)
                .publishedAt(status == ContentStatus.PUBLISHED ? Instant.now() : null)
                .newsCategory(category)
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .seoImage(request.getSeoImage())
                .build();

        return newsMapper.toResponse(newsRepository.save(entity));
    }

    @Override
    @Transactional
    public NewsResponse update(Long id, NewsUpdateRequest request) {
        NewsEntity entity = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));

        if (request.getNewsCategoryId() != null
                && (entity.getNewsCategory() == null
                        || !entity.getNewsCategory().getId().equals(request.getNewsCategoryId()))) {
            NewsCategoryEntity category = newsCategoryRepository.findById(request.getNewsCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.NEWS_CATEGORY_NOT_FOUND));
            entity.setNewsCategory(category);
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(entity.getSlug())) {
                if (newsRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new AppException(ErrorCode.NEWS_SLUG_EXISTED);
                }
                entity.setSlug(newSlug);
            }
        }

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getThumb() != null) {
            entity.setThumb(request.getThumb());
        }
        if (request.getShortContent() != null) {
            entity.setShortContent(request.getShortContent());
        }
        if (request.getDes() != null) {
            entity.setDes(request.getDes());
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
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
            if (request.getStatus() == ContentStatus.PUBLISHED && entity.getPublishedAt() == null) {
                entity.setPublishedAt(Instant.now());
            }
        }

        return newsMapper.toResponse(newsRepository.save(entity));
    }

    @Override
    @Transactional
    public NewsResponse updateStatus(Long id, ContentStatus status) {
        NewsEntity entity = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        entity.setStatus(status);
        if (status == ContentStatus.PUBLISHED && entity.getPublishedAt() == null) {
            entity.setPublishedAt(Instant.now());
        }
        return newsMapper.toResponse(newsRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NewsEntity entity = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        // thumb/seo images are shared URLs, so they are not removed from MinIO here.
        newsRepository.delete(entity);
    }

    /** Ensures slug uniqueness by appending {@code -2, -3, ...} on collision. */
    private String generateUniqueSlug(String base) {
        String candidate = base;
        int suffix = 2;
        while (newsRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private Sort resolveSort(NewsSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
