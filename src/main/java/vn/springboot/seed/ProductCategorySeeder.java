package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.enums.CategoryType;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.repository.ProductCategoryRepository;

import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "PRODUCT CATEGORIES" section (6 rows, one per {@link CategoryType}). */
@Component
@RequiredArgsConstructor
public class ProductCategorySeeder implements DomainSeeder {

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public boolean isEmpty() {
        return productCategoryRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        productCategoryRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        productCategoryRepository.saveAll(List.of(
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.BO_DO_THO)
                        .name("Bộ đồ thờ")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(1)
                        .shortDescription("Bộ đồ thờ gốm sứ Bát Tràng chế tác thủ công, trang nghiêm cho không gian thờ cúng gia tiên.")
                        .slug("bo-do-tho")
                        .isActive(true)
                        .seoTitle("Bộ đồ thờ Bát Tràng")
                        .seoDescription("Bộ đồ thờ gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.BINH_PHONG_THUY)
                        .name("Bình phong thủy")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(2)
                        .shortDescription("Bình phong thủy gốm sứ Bát Tràng, hoạ tiết tài lộc phú quý, hợp mệnh gia chủ.")
                        .slug("binh-phong-thuy")
                        .isActive(true)
                        .seoTitle("Bình phong thủy Bát Tràng")
                        .seoDescription("Bình phong thủy gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.LUC_BINH_GOM_SU)
                        .name("Lục bình gốm sứ")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(3)
                        .shortDescription("Lục bình gốm sứ Bát Tràng dáng cổ, hoạ tiết tinh xảo, trang trí không gian sang trọng.")
                        .slug("luc-binh-gom-su")
                        .isActive(true)
                        .seoTitle("Lục bình gốm sứ Bát Tràng")
                        .seoDescription("Lục bình gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.AM_CHEN_BAT_TRANG)
                        .name("Ấm chén Bát Tràng")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(4)
                        .shortDescription("Ấm chén Bát Tràng men lam, men rạn cổ, phù hợp dùng hàng ngày hoặc làm quà tặng.")
                        .slug("am-chen-bat-trang")
                        .isActive(true)
                        .seoTitle("Ấm chén Bát Tràng")
                        .seoDescription("Ấm chén gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.QUA_TANG_GOM_SU)
                        .name("Quà tặng gốm sứ")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(5)
                        .shortDescription("Quà tặng gốm sứ Bát Tràng cao cấp, phù hợp biếu tặng dịp lễ, tết, khai trương.")
                        .slug("qua-tang-gom-su")
                        .isActive(true)
                        .seoTitle("Quà tặng gốm sứ Bát Tràng")
                        .seoDescription("Quà tặng gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.CHUM_SANH_NGAM_RUOU)
                        .name("Chum sành ngâm rượu")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(6)
                        .shortDescription("Chum sành ngâm rượu Bát Tràng, chất sành tự nhiên giúp rượu êm và tròn vị hơn theo thời gian.")
                        .slug("chum-sanh-ngam-ruou")
                        .isActive(true)
                        .seoTitle("Chum sành ngâm rượu Bát Tràng")
                        .seoDescription("Chum sành ngâm rượu Bát Tràng chính hãng Vũ Gia.")
                        .build()));
    }
}
