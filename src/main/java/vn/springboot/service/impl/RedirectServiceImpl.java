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
import vn.springboot.dto.request.redirect.RedirectCreateRequest;
import vn.springboot.dto.request.redirect.RedirectSearchRequest;
import vn.springboot.dto.request.redirect.RedirectUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.redirect.RedirectResponse;
import vn.springboot.entity.redirect.RedirectEntity;
import vn.springboot.mapper.RedirectMapper;
import vn.springboot.repository.RedirectRepository;
import vn.springboot.repository.specification.RedirectSpecification;
import vn.springboot.service.RedirectService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedirectServiceImpl implements RedirectService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_STATUS_CODE = 301;

    private final RedirectRepository redirectRepository;
    private final RedirectMapper redirectMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RedirectResponse> search(RedirectSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<RedirectEntity> specification = RedirectSpecification.build(request);

        Page<RedirectEntity> page = redirectRepository.findAll(specification, pageable);

        List<RedirectResponse> content = page.getContent().stream()
                .map(redirectMapper::toResponse)
                .toList();

        return PageResponse.<RedirectResponse>builder()
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
    public RedirectResponse getById(Long id) {
        return redirectRepository.findById(id)
                .map(redirectMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.REDIRECT_NOT_FOUND));
    }

    @Override
    @Transactional
    public RedirectResponse create(RedirectCreateRequest request) {
        String fromPath = request.getFromPath().trim();
        if (redirectRepository.existsByFromPath(fromPath)) {
            throw new AppException(ErrorCode.REDIRECT_FROM_PATH_EXISTED);
        }

        RedirectEntity entity = RedirectEntity.builder()
                .fromPath(fromPath)
                .toPath(request.getToPath())
                .statusCode(request.getStatusCode() != null ? request.getStatusCode() : DEFAULT_STATUS_CODE)
                .hitCount(0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return redirectMapper.toResponse(redirectRepository.save(entity));
    }

    @Override
    @Transactional
    public RedirectResponse update(Long id, RedirectUpdateRequest request) {
        RedirectEntity entity = redirectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REDIRECT_NOT_FOUND));

        if (request.getFromPath() != null && !request.getFromPath().isBlank()) {
            String newFromPath = request.getFromPath().trim();
            if (!newFromPath.equals(entity.getFromPath())) {
                if (redirectRepository.existsByFromPathAndIdNot(newFromPath, id)) {
                    throw new AppException(ErrorCode.REDIRECT_FROM_PATH_EXISTED);
                }
                entity.setFromPath(newFromPath);
            }
        }

        if (request.getToPath() != null) {
            entity.setToPath(request.getToPath());
        }
        if (request.getStatusCode() != null) {
            entity.setStatusCode(request.getStatusCode());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return redirectMapper.toResponse(redirectRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RedirectEntity entity = redirectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REDIRECT_NOT_FOUND));

        redirectRepository.delete(entity);
    }

    private Sort resolveSort(RedirectSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
