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
import vn.springboot.dto.request.coupon.CouponCreateRequest;
import vn.springboot.dto.request.coupon.CouponSearchRequest;
import vn.springboot.dto.request.coupon.CouponUpdateRequest;
import vn.springboot.dto.request.coupon.CouponValidateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.coupon.CouponResponse;
import vn.springboot.dto.response.coupon.CouponValidationResponse;
import vn.springboot.entity.coupon.CouponEntity;
import vn.springboot.entity.enums.DiscountType;
import vn.springboot.mapper.CouponMapper;
import vn.springboot.repository.CouponRepository;
import vn.springboot.repository.specification.CouponSpecification;
import vn.springboot.service.CouponService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "code", "discountValue", "usedCount", "startsAt", "endsAt", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> search(CouponSearchRequest request) {
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<CouponEntity> specification = CouponSpecification.build(request);

        Page<CouponEntity> page = couponRepository.findAll(specification, pageable);

        List<CouponResponse> content = page.getContent().stream()
                .map(couponMapper::toResponse)
                .toList();

        return PageResponse.<CouponResponse>builder()
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
    public CouponResponse getById(Long id) {
        return couponMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public CouponResponse create(CouponCreateRequest request) {
        String code = normalizeCode(request.getCode());
        if (couponRepository.existsByCode(code)) {
            throw new AppException(ErrorCode.COUPON_CODE_EXISTED);
        }

        CouponEntity entity = CouponEntity.builder()
                .code(code)
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usageLimitPerUser(request.getUsageLimitPerUser())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        return couponMapper.toResponse(couponRepository.save(entity));
    }

    @Override
    @Transactional
    public CouponResponse update(Long id, CouponUpdateRequest request) {
        CouponEntity entity = findOrThrow(id);

        if (request.getCode() != null) {
            String code = normalizeCode(request.getCode());
            if (!code.equals(entity.getCode()) && couponRepository.existsByCodeAndIdNot(code, id)) {
                throw new AppException(ErrorCode.COUPON_CODE_EXISTED);
            }
            entity.setCode(code);
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getDiscountType() != null) {
            entity.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) {
            entity.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMinOrderAmount() != null) {
            entity.setMinOrderAmount(request.getMinOrderAmount());
        }
        if (request.getMaxDiscountAmount() != null) {
            entity.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getUsageLimit() != null) {
            entity.setUsageLimit(request.getUsageLimit());
        }
        if (request.getUsageLimitPerUser() != null) {
            entity.setUsageLimitPerUser(request.getUsageLimitPerUser());
        }
        if (request.getStartsAt() != null) {
            entity.setStartsAt(request.getStartsAt());
        }
        if (request.getEndsAt() != null) {
            entity.setEndsAt(request.getEndsAt());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return couponMapper.toResponse(couponRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        couponRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validate(CouponValidateRequest request) {
        String code = normalizeCode(request.getCode());
        long orderAmount = request.getOrderAmount();

        Optional<CouponEntity> found = couponRepository.findByCode(code);
        if (found.isEmpty()) {
            return invalid(code, "Mã giảm giá không tồn tại");
        }
        CouponEntity coupon = found.get();

        if (!coupon.isActive()) {
            return invalid(code, "Mã giảm giá đã bị vô hiệu hoá");
        }
        Instant now = Instant.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            return invalid(code, "Mã giảm giá chưa có hiệu lực");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            return invalid(code, "Mã giảm giá đã hết hạn");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return invalid(code, "Mã giảm giá đã hết lượt sử dụng");
        }
        if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
            return invalid(code, "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã");
        }

        long discount = computeDiscount(coupon, orderAmount);
        boolean freeShipping = coupon.getDiscountType() == DiscountType.FREE_SHIP;

        return CouponValidationResponse.builder()
                .valid(true)
                .code(code)
                .discountType(coupon.getDiscountType())
                .discountAmount(discount)
                .freeShipping(freeShipping)
                .message("Áp dụng mã giảm giá thành công")
                .build();
    }

    /** Discount (VND) applied to the order subtotal, clamped to [0, orderAmount]. */
    private long computeDiscount(CouponEntity coupon, long orderAmount) {
        long discount = switch (coupon.getDiscountType()) {
            case PERCENT -> {
                long raw = orderAmount * coupon.getDiscountValue() / 100;
                yield coupon.getMaxDiscountAmount() != null
                        ? Math.min(raw, coupon.getMaxDiscountAmount())
                        : raw;
            }
            case FIXED -> coupon.getDiscountValue();
            case FREE_SHIP -> 0L; // shipping fee is handled by the order/shipping layer
        };
        return Math.clamp(discount, 0L, orderAmount);
    }

    private CouponValidationResponse invalid(String code, String message) {
        return CouponValidationResponse.builder()
                .valid(false)
                .code(code)
                .discountAmount(0)
                .freeShipping(false)
                .message(message)
                .build();
    }

    private CouponEntity findOrThrow(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private Sort resolveSort(CouponSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
