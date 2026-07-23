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
import vn.springboot.dto.request.shipping.ShippingMethodCreateRequest;
import vn.springboot.dto.request.shipping.ShippingMethodSearchRequest;
import vn.springboot.dto.request.shipping.ShippingMethodUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.shipping.ShippingMethodResponse;
import vn.springboot.entity.shipping.ShippingMethodEntity;
import vn.springboot.mapper.ShippingMethodMapper;
import vn.springboot.repository.ShippingMethodRepository;
import vn.springboot.repository.specification.ShippingMethodSpecification;
import vn.springboot.service.ShippingMethodService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShippingMethodServiceImpl implements ShippingMethodService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "fee", "sortOrder", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "sortOrder";
    private static final int MAX_PAGE_SIZE = 100;

    private final ShippingMethodRepository shippingMethodRepository;
    private final ShippingMethodMapper shippingMethodMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShippingMethodResponse> search(ShippingMethodSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<ShippingMethodEntity> specification = ShippingMethodSpecification.build(request);

        Page<ShippingMethodEntity> page = shippingMethodRepository.findAll(specification, pageable);

        List<ShippingMethodResponse> content = page.getContent().stream()
                .map(shippingMethodMapper::toResponse)
                .toList();

        return PageResponse.<ShippingMethodResponse>builder()
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
    public ShippingMethodResponse getById(Long id) {
        return shippingMethodRepository.findById(id)
                .map(shippingMethodMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_METHOD_NOT_FOUND));
    }

    @Override
    @Transactional
    public ShippingMethodResponse create(ShippingMethodCreateRequest request) {
        ShippingMethodEntity entity = ShippingMethodEntity.builder()
                .name(request.getName())
                .fee(request.getFee())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return shippingMethodMapper.toResponse(shippingMethodRepository.save(entity));
    }

    @Override
    @Transactional
    public ShippingMethodResponse update(Long id, ShippingMethodUpdateRequest request) {
        ShippingMethodEntity entity = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_METHOD_NOT_FOUND));

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getFee() != null) {
            entity.setFee(request.getFee());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return shippingMethodMapper.toResponse(shippingMethodRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ShippingMethodEntity entity = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_METHOD_NOT_FOUND));

        shippingMethodRepository.delete(entity);
    }

    private Sort resolveSort(ShippingMethodSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
