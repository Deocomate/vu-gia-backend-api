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
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final ShippingMethodRepository shippingMethodRepository;
    private final ShippingMethodMapper shippingMethodMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShippingMethodResponse> search(ShippingMethodSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<ShippingMethodEntity> specification = ShippingMethodSpecification.build(request);

        Page<ShippingMethodEntity> page = shippingMethodRepository.findAll(specification, pageable);

        List<ShippingMethodResponse> content = page.getContent().stream()
                .map(shippingMethodMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
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
        if (shippingMethodRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.SHIPPING_METHOD_CODE_EXISTED);
        }

        ShippingMethodEntity entity = ShippingMethodEntity.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .estimatedDelivery(request.getEstimatedDelivery())
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
        if (request.getCode() != null) {
            if (!request.getCode().equals(entity.getCode())
                    && shippingMethodRepository.existsByCodeAndIdNot(request.getCode(), id)) {
                throw new AppException(ErrorCode.SHIPPING_METHOD_CODE_EXISTED);
            }
            entity.setCode(request.getCode());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getEstimatedDelivery() != null) {
            entity.setEstimatedDelivery(request.getEstimatedDelivery());
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
}
