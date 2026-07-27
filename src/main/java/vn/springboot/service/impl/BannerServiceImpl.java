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
import vn.springboot.dto.request.banner.BannerCreateRequest;
import vn.springboot.dto.request.banner.BannerSearchRequest;
import vn.springboot.dto.request.banner.BannerUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.banner.BannerResponse;
import vn.springboot.entity.banner.BannerEntity;
import vn.springboot.mapper.BannerMapper;
import vn.springboot.repository.BannerRepository;
import vn.springboot.repository.specification.BannerSpecification;
import vn.springboot.service.BannerService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "sortOrder", "position", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BannerResponse> search(BannerSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<BannerEntity> specification = BannerSpecification.build(request);

        Page<BannerEntity> page = bannerRepository.findAll(specification, pageable);

        List<BannerResponse> content = page.getContent().stream()
                .map(bannerMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public BannerResponse getById(Long id) {
        return bannerRepository.findById(id)
                .map(bannerMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND));
    }

    @Override
    @Transactional
    public BannerResponse create(BannerCreateRequest request) {
        BannerEntity entity = BannerEntity.builder()
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .position(request.getPosition())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        return bannerMapper.toResponse(bannerRepository.save(entity));
    }

    @Override
    @Transactional
    public BannerResponse update(Long id, BannerUpdateRequest request) {
        BannerEntity entity = bannerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND));

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getImageUrl() != null) {
            entity.setImageUrl(request.getImageUrl());
        }
        if (request.getLinkUrl() != null) {
            entity.setLinkUrl(request.getLinkUrl());
        }
        if (request.getPosition() != null) {
            entity.setPosition(request.getPosition());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }
        if (request.getStartsAt() != null) {
            entity.setStartsAt(request.getStartsAt());
        }
        if (request.getEndsAt() != null) {
            entity.setEndsAt(request.getEndsAt());
        }

        return bannerMapper.toResponse(bannerRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BannerEntity entity = bannerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND));

        bannerRepository.delete(entity);
    }
}
