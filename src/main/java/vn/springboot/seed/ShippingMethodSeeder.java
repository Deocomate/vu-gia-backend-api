package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.shipping.ShippingMethodEntity;
import vn.springboot.repository.ShippingMethodRepository;

import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "SHIPPING METHODS" section (2 rows). */
@Component
@RequiredArgsConstructor
public class ShippingMethodSeeder implements DomainSeeder {

    private final ShippingMethodRepository shippingMethodRepository;

    @Override
    public boolean isEmpty() {
        return shippingMethodRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        shippingMethodRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        shippingMethodRepository.saveAll(List.of(
                ShippingMethodEntity.builder()
                        .name("Giao hàng tiêu chuẩn")
                        .code("STANDARD")
                        .description("Giao hàng tận nơi từ 2 - 4 ngày làm việc")
                        .fee(30_000L)
                        .estimatedDelivery("2 - 4 ngày")
                        .isActive(true)
                        .sortOrder(1)
                        .build(),
                ShippingMethodEntity.builder()
                        .name("Giao hàng hỏa tốc")
                        .code("EXPRESS")
                        .description("Giao hàng nhanh nội thành trong 24h")
                        .fee(50_000L)
                        .estimatedDelivery("Trong 24h")
                        .isActive(true)
                        .sortOrder(2)
                        .build()));
    }
}
