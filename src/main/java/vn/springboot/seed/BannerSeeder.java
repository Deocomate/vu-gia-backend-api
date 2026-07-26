package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.banner.BannerEntity;
import vn.springboot.entity.enums.BannerPosition;
import vn.springboot.repository.BannerRepository;

import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "BANNERS" section (6 rows, no FK). */
@Component
@RequiredArgsConstructor
public class BannerSeeder implements DomainSeeder {

    private final BannerRepository bannerRepository;

    @Override
    public boolean isEmpty() {
        return bannerRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        bannerRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        bannerRepository.saveAll(List.of(
                banner("Ưu đãi tháng 6 - 299.000đ - Sản phẩm gốm Bát Tràng",
                        "assets/images/home/hero-image-1-top.png", "/san-pham", BannerPosition.HOME_HERO, 1),
                banner("Các sản phẩm nổi bật",
                        "assets/images/home/hero-image-2-left.png", "/san-pham", BannerPosition.HOME_HERO, 2),
                banner("Phong thuỷ, Trang trí",
                        "assets/images/home/hero-image-3-right.png", "/san-pham", BannerPosition.HOME_HERO, 3),
                banner("Ấm chén Bát Tràng",
                        "assets/images/nha-xuong/slider-image-1.png", "/san-pham", BannerPosition.HOME_CATEGORY, 1),
                banner("Chum sành ngâm rượu",
                        "assets/images/nha-xuong/slider-image-2.png", "/san-pham", BannerPosition.HOME_CATEGORY, 2),
                banner("Quà tặng Bát Tràng",
                        "assets/images/nha-xuong/slider-image-3.png", "/san-pham", BannerPosition.HOME_CATEGORY, 3)));
    }

    private BannerEntity banner(String title, String imageUrl, String linkUrl, BannerPosition position, int sortOrder) {
        return BannerEntity.builder()
                .title(title)
                .imageUrl(imageUrl)
                .linkUrl(linkUrl)
                .position(position)
                .sortOrder(sortOrder)
                .isActive(true)
                .build();
    }
}
