package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.repository.AltarStyleRepository;

import java.util.List;

/**
 * Seeds the glaze/finish "style" catalog products can be tagged with (Phase 6 of the
 * altar-customizer build). Five rows, matching the real glaze families already used in
 * {@link ProductCategorySeeder}'s and {@link ProductSeeder}'s existing product copy (men
 * lam / men rạn / vẽ vàng): Men lam, Men rạn, Men lam vẽ vàng, Men rạn dát vàng, Men màu
 * theo mệnh.
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
                        .thumb("assets/images/altar-customizer/similar-product-1.jpg")
                        .description("Men lam cổ điển, dùng oxit coban vẽ họa tiết xanh lam trên nền men trắng, nét vẽ tinh tế, màu sắc trong trẻo.")
                        .priority(1)
                        .isActive(true)
                        .build(),
                AltarStyleEntity.builder()
                        .name("Men rạn")
                        .slug("men-ran")
                        .thumb("assets/images/altar-customizer/similar-product-2.jpg")
                        .description("Men rạn cổ kính, hình thành từ sự chênh lệch độ co giãn giữa men và xương gốm khi nung, mang vẻ đẹp hoài cổ, độc bản.")
                        .priority(2)
                        .isActive(true)
                        .build(),
                AltarStyleEntity.builder()
                        .name("Men lam vẽ vàng")
                        .slug("men-lam-ve-vang")
                        .thumb("assets/images/altar-customizer/similar-product-3.jpg")
                        .description("Men lam phối họa tiết vẽ tay bằng vàng 24k, tôn lên vẻ sang trọng, quý phái cho không gian thờ cúng.")
                        .priority(3)
                        .isActive(true)
                        .build(),
                AltarStyleEntity.builder()
                        .name("Men rạn dát vàng")
                        .slug("men-ran-dat-vang")
                        .thumb("assets/images/altar-customizer/accessories-sprite.png")
                        .description("Men rạn cổ kết hợp dát vàng thủ công trên các đường nét hoa văn, vừa hoài cổ vừa lộng lẫy.")
                        .priority(4)
                        .isActive(true)
                        .build(),
                AltarStyleEntity.builder()
                        .name("Men màu theo mệnh")
                        .slug("men-mau-theo-menh")
                        .thumb("assets/images/altar-customizer/altar-preview.png")
                        .description("Bộ men màu tuyển chọn theo ngũ hành bản mệnh gia chủ (Kim, Mộc, Thủy, Hỏa, Thổ), giúp gia tăng vượng khí.")
                        .priority(5)
                        .isActive(true)
                        .build()));
    }
}
