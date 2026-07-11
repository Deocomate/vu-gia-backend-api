package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.coupon.CouponCreateRequest;
import vn.springboot.dto.request.coupon.CouponSearchRequest;
import vn.springboot.dto.request.coupon.CouponValidateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.coupon.CouponResponse;
import vn.springboot.dto.response.coupon.CouponValidationResponse;
import vn.springboot.entity.coupon.CouponEntity;
import vn.springboot.entity.enums.DiscountType;
import vn.springboot.mapper.CouponMapper;
import vn.springboot.repository.CouponRepository;
import vn.springboot.service.impl.CouponServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private CouponEntity coupon(DiscountType type, long value) {
        CouponEntity c = CouponEntity.builder()
                .code("SALE10").discountType(type).discountValue(value).isActive(true).build();
        c.setId(1L);
        return c;
    }

    @Test
    void search_returnsPageResponse() {
        CouponEntity c = coupon(DiscountType.PERCENT, 10);
        Page<CouponEntity> page = new PageImpl<>(List.of(c), PageRequest.of(0, 10), 1);
        when(couponRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(couponMapper.toResponse(c)).thenReturn(CouponResponse.builder().id(1L).code("SALE10").build());

        PageResponse<CouponResponse> result = couponService.search(CouponSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isEqualTo(1); // 1-based
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getById_throwsNotFound() {
        when(couponRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> couponService.getById(9L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPON_NOT_FOUND);
    }

    @Test
    void create_normalizesCodeAndRejectsDuplicate() {
        when(couponRepository.existsByCode("SALE10")).thenReturn(true);
        CouponCreateRequest req = CouponCreateRequest.builder()
                .code(" sale10 ").discountType(DiscountType.PERCENT).discountValue(10L).build();

        assertThatThrownBy(() -> couponService.create(req))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPON_CODE_EXISTED);
        verify(couponRepository, never()).save(any());
    }

    // ---------- validate ----------

    @Test
    void validate_unknownCode_returnsInvalid() {
        when(couponRepository.findByCode("NOPE")).thenReturn(Optional.empty());
        CouponValidationResponse res = couponService.validate(new CouponValidateRequest("nope", 100_000L));
        assertThat(res.isValid()).isFalse();
        assertThat(res.getDiscountAmount()).isZero();
    }

    @Test
    void validate_percent_appliesCapAndClamp() {
        CouponEntity c = coupon(DiscountType.PERCENT, 20);
        c.setMaxDiscountAmount(30_000L);
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(c));

        // 20% of 500k = 100k, capped at 30k
        CouponValidationResponse res = couponService.validate(new CouponValidateRequest("sale10", 500_000L));
        assertThat(res.isValid()).isTrue();
        assertThat(res.getDiscountAmount()).isEqualTo(30_000L);
    }

    @Test
    void validate_expired_returnsInvalid() {
        CouponEntity c = coupon(DiscountType.FIXED, 50_000L);
        c.setEndsAt(Instant.now().minusSeconds(3600));
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(c));

        CouponValidationResponse res = couponService.validate(new CouponValidateRequest("SALE10", 100_000L));
        assertThat(res.isValid()).isFalse();
        assertThat(res.getMessage()).contains("hết hạn");
    }

    @Test
    void validate_minOrderNotMet_returnsInvalid() {
        CouponEntity c = coupon(DiscountType.FIXED, 50_000L);
        c.setMinOrderAmount(200_000L);
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(c));

        CouponValidationResponse res = couponService.validate(new CouponValidateRequest("SALE10", 100_000L));
        assertThat(res.isValid()).isFalse();
    }

    @Test
    void validate_fixed_clampsToOrderAmount() {
        CouponEntity c = coupon(DiscountType.FIXED, 200_000L);
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(c));

        CouponValidationResponse res = couponService.validate(new CouponValidateRequest("SALE10", 100_000L));
        assertThat(res.isValid()).isTrue();
        assertThat(res.getDiscountAmount()).isEqualTo(100_000L); // clamped to order amount
    }

    @Test
    void validate_freeShip_flagsFreeShippingWithZeroDiscount() {
        CouponEntity c = coupon(DiscountType.FREE_SHIP, 0L);
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(c));

        CouponValidationResponse res = couponService.validate(new CouponValidateRequest("SALE10", 100_000L));
        assertThat(res.isValid()).isTrue();
        assertThat(res.isFreeShipping()).isTrue();
        assertThat(res.getDiscountAmount()).isZero();
    }
}
