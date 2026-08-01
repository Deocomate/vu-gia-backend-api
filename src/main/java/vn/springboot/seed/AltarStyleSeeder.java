package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.repository.AltarStyleRepository;

import java.util.List;

/**
 * Seeds the glaze/finish "style" catalog products can be tagged with. Two rows — only the
 * glazes actually present in the real 25-product photo catalog: Men lam (22 products) and Men
 * lam vẽ vàng (3 products). The palette's style chip row filters exclusively
 * ({@code item.styleId !== altarStyleId}), so seeding styles with zero matching products would
 * ship dead chips that always show "Không có sản phẩm phù hợp bộ lọc hiện tại."
 *
 * <p>No optional/nullable columns on this entity — every field is populated on every row.
 */
@Component
@RequiredArgsConstructor
public class AltarStyleSeeder implements DomainSeeder {

    private final AltarStyleRepository altarStyleRepository;

    @Override
    public boolean isEmpty() {
        return altarStyleRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        altarStyleRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        altarStyleRepository.saveAll(List.of(
                AltarStyleEntity.builder()
                        .name("Men lam")
                        .slug("men-lam")
                        .thumb("assets/images/altar-customizer/products/lo-hoa-men-lam-h30/01.png")
                        .description("Men lam cổ điển, dùng oxit coban vẽ họa tiết xanh lam trên nền men trắng, nung trên 1.200°C cho màu men trong trẻo, bền theo thời gian.")
                        .priority(1)
                        .isActive(true)
                        .build(),
                AltarStyleEntity.builder()
                        .name("Men lam vẽ vàng")
                        .slug("men-lam-ve-vang")
                        .thumb("assets/images/altar-customizer/products/nam-ruou-men-lam-ve-vang-h28/01.png")
                        .description("Men lam phối họa tiết vẽ tay bằng vàng 24k trên nền hoa văn cổ, tôn lên vẻ sang trọng cho không gian thờ cúng.")
                        .priority(2)
                        .isActive(true)
                        .build()));
    }
}
