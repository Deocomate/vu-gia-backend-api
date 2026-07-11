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
import vn.springboot.dto.request.gallery.GalleryImageCreateRequest;
import vn.springboot.dto.request.gallery.GalleryImageSearchRequest;
import vn.springboot.dto.request.gallery.GalleryImageUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.gallery.GalleryImageResponse;
import vn.springboot.entity.gallery.GalleryImageEntity;
import vn.springboot.mapper.GalleryImageMapper;
import vn.springboot.repository.GalleryImageRepository;
import vn.springboot.repository.specification.GalleryImageSpecification;
import vn.springboot.service.GalleryImageService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GalleryImageServiceImpl implements GalleryImageService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "sortOrder", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final GalleryImageRepository galleryImageRepository;
    private final GalleryImageMapper galleryImageMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GalleryImageResponse> search(GalleryImageSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<GalleryImageEntity> specification = GalleryImageSpecification.build(request);

        Page<GalleryImageEntity> page = galleryImageRepository.findAll(specification, pageable);

        List<GalleryImageResponse> content = page.getContent().stream()
                .map(galleryImageMapper::toResponse)
                .toList();

        return PageResponse.<GalleryImageResponse>builder()
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
    public GalleryImageResponse getById(Long id) {
        return galleryImageRepository.findById(id)
                .map(galleryImageMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.GALLERY_IMAGE_NOT_FOUND));
    }

    @Override
    @Transactional
    public GalleryImageResponse create(GalleryImageCreateRequest request) {
        GalleryImageEntity entity = GalleryImageEntity.builder()
                .imageUrl(request.getImageUrl())
                .title(request.getTitle())
                .category(request.getCategory())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return galleryImageMapper.toResponse(galleryImageRepository.save(entity));
    }

    @Override
    @Transactional
    public GalleryImageResponse update(Long id, GalleryImageUpdateRequest request) {
        GalleryImageEntity entity = galleryImageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GALLERY_IMAGE_NOT_FOUND));

        if (request.getImageUrl() != null) {
            entity.setImageUrl(request.getImageUrl());
        }
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return galleryImageMapper.toResponse(galleryImageRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GalleryImageEntity entity = galleryImageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GALLERY_IMAGE_NOT_FOUND));

        galleryImageRepository.delete(entity);
    }

    private Sort resolveSort(GalleryImageSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
