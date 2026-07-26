package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.coupon.CouponEntity;
import vn.springboot.entity.enums.DiscountType;
import vn.springboot.repository.CouponRepository;

import java.time.Instant;
import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "COUPONS" section (3 rows, no FK). */
@Component
@RequiredArgsConstructor
public class CouponSeeder implements DomainSeeder {

    private final CouponRepository couponRepository;

    @Override
    public boolean isEmpty() {
        return couponRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        couponRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        Instant now = Instant.now();
        couponRepository.saveAll(List.of(
                CouponEntity.builder()
                        .code("WELCOME10")
                        .description("Giảm 10% cho đơn hàng đầu tiên")
                        .discountType(DiscountType.PERCENT)
                        .discountValue(10L)
                        .minOrderAmount(500_000L)
                        .maxDiscountAmount(200_000L)
                        .usageLimit(1000)
                        .usageLimitPerUser(1)
                        .startsAt(now)
                        .isActive(true)
                        .build(),
                CouponEntity.builder()
                        .code("GIAM50K")
                        .description("Giảm 50.000đ cho đơn từ 1.000.000đ")
                        .discountType(DiscountType.FIXED)
                        .discountValue(50_000L)
                        .minOrderAmount(1_000_000L)
                        .startsAt(now)
                        .isActive(true)
                        .build(),
                CouponEntity.builder()
                        .code("FREESHIP")
                        .description("Miễn phí vận chuyển nội thành Hà Nội")
                        .discountType(DiscountType.FREE_SHIP)
                        .discountValue(0L)
                        .minOrderAmount(300_000L)
                        .startsAt(now)
                        .isActive(true)
                        .build()));
    }
}
