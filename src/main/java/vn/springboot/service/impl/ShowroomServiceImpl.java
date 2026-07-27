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
import vn.springboot.dto.request.showroom.ShowroomCreateRequest;
import vn.springboot.dto.request.showroom.ShowroomSearchRequest;
import vn.springboot.dto.request.showroom.ShowroomUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.showroom.ShowroomResponse;
import vn.springboot.entity.showroom.ShowroomEntity;
import vn.springboot.mapper.ShowroomMapper;
import vn.springboot.repository.ShowroomRepository;
import vn.springboot.repository.specification.ShowroomSpecification;
import vn.springboot.service.ShowroomService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShowroomServiceImpl implements ShowroomService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "sortOrder", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final ShowroomRepository showroomRepository;
    private final ShowroomMapper showroomMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShowroomResponse> search(ShowroomSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<ShowroomEntity> specification = ShowroomSpecification.build(request);

        Page<ShowroomEntity> page = showroomRepository.findAll(specification, pageable);

        List<ShowroomResponse> content = page.getContent().stream()
                .map(showroomMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public ShowroomResponse getById(Long id) {
        return showroomRepository.findById(id)
                .map(showroomMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWROOM_NOT_FOUND));
    }

    @Override
    @Transactional
    public ShowroomResponse create(ShowroomCreateRequest request) {
        ShowroomEntity entity = ShowroomEntity.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .mapEmbedUrl(request.getMapEmbedUrl())
                .openingHours(request.getOpeningHours())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return showroomMapper.toResponse(showroomRepository.save(entity));
    }

    @Override
    @Transactional
    public ShowroomResponse update(Long id, ShowroomUpdateRequest request) {
        ShowroomEntity entity = showroomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWROOM_NOT_FOUND));

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
        if (request.getMapEmbedUrl() != null) {
            entity.setMapEmbedUrl(request.getMapEmbedUrl());
        }
        if (request.getOpeningHours() != null) {
            entity.setOpeningHours(request.getOpeningHours());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return showroomMapper.toResponse(showroomRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ShowroomEntity entity = showroomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWROOM_NOT_FOUND));

        showroomRepository.delete(entity);
    }
}
