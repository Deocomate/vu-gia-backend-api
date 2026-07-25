-- ----------------------------
-- Khoá cứng product_categories thành 6 loại cố định (CategoryType), thay cho
-- bảng CRUD tự do trước đây. Thứ tự: thêm cột category_type (nullable) -> chèn
-- 6 category mới -> remap toàn bộ sản phẩm hiện có sang category mới -> xoá 5
-- category cũ theo dòng men (Men lam/Men rạn/...) -> khoá NOT NULL + UNIQUE.
-- Không sửa V1/V2 vì Flyway đã áp dụng trên các DB đang chạy.
-- ----------------------------

ALTER TABLE `product_categories`
    ADD COLUMN `category_type` VARCHAR(30) NULL AFTER `id`;

INSERT INTO
    `product_categories` (
        `category_type`,
        `name`,
        `thumb`,
        `priority`,
        `long_content`,
        `slug`,
        `is_active`,
        `seo_title`,
        `seo_description`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        'BO_DO_THO',
        'Bộ đồ thờ',
        'assets/images/products/product-category-thumb.png',
        1,
        'Bộ đồ thờ gốm sứ Bát Tràng chế tác thủ công, trang nghiêm cho không gian thờ cúng gia tiên.',
        'bo-do-tho',
        TRUE,
        'Bộ đồ thờ Bát Tràng',
        'Bộ đồ thờ gốm sứ Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'BINH_PHONG_THUY',
        'Bình phong thủy',
        'assets/images/products/product-category-thumb.png',
        2,
        'Bình phong thủy gốm sứ Bát Tràng, hoạ tiết tài lộc phú quý, hợp mệnh gia chủ.',
        'binh-phong-thuy',
        TRUE,
        'Bình phong thủy Bát Tràng',
        'Bình phong thủy gốm sứ Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'LUC_BINH_GOM_SU',
        'Lục bình gốm sứ',
        'assets/images/products/product-category-thumb.png',
        3,
        'Lục bình gốm sứ Bát Tràng dáng cổ, hoạ tiết tinh xảo, trang trí không gian sang trọng.',
        'luc-binh-gom-su',
        TRUE,
        'Lục bình gốm sứ Bát Tràng',
        'Lục bình gốm sứ Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'AM_CHEN_BAT_TRANG',
        'Ấm chén Bát Tràng',
        'assets/images/products/product-category-thumb.png',
        4,
        'Ấm chén Bát Tràng men lam, men rạn cổ, phù hợp dùng hàng ngày hoặc làm quà tặng.',
        'am-chen-bat-trang',
        TRUE,
        'Ấm chén Bát Tràng',
        'Ấm chén gốm sứ Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'QUA_TANG_GOM_SU',
        'Quà tặng gốm sứ',
        'assets/images/products/product-category-thumb.png',
        5,
        'Quà tặng gốm sứ Bát Tràng cao cấp, phù hợp biếu tặng dịp lễ, tết, khai trương.',
        'qua-tang-gom-su',
        TRUE,
        'Quà tặng gốm sứ Bát Tràng',
        'Quà tặng gốm sứ Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'CHUM_SANH_NGAM_RUOU',
        'Chum sành ngâm rượu',
        'assets/images/products/product-category-thumb.png',
        6,
        'Chum sành ngâm rượu Bát Tràng, chất sành tự nhiên giúp rượu êm và tròn vị hơn theo thời gian.',
        'chum-sanh-ngam-ruou',
        TRUE,
        'Chum sành ngâm rượu Bát Tràng',
        'Chum sành ngâm rượu Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    );

-- Toàn bộ sản phẩm demo (V2 seed, id 1-12) đều là đồ thờ cúng -> remap về
-- "Bộ đồ thờ". Match theo category_type thay vì id cứng vì id mới do
-- AUTO_INCREMENT sinh ra, không đoán trước được.
UPDATE `products`
SET `product_category_id` = (
    SELECT `id` FROM `product_categories` WHERE `category_type` = 'BO_DO_THO'
)
WHERE `product_category_id` IN (1, 2, 3, 4, 5);

-- 5 category cũ theo dòng men (Men lam/Men rạn/...) không còn sản phẩm nào
-- tham chiếu, xoá để chỉ còn đúng 6 category cố định.
DELETE FROM `product_categories` WHERE `id` IN (1, 2, 3, 4, 5);

ALTER TABLE `product_categories`
    MODIFY COLUMN `category_type` VARCHAR(30) NOT NULL,
    ADD CONSTRAINT `uq_product_categories_category_type` UNIQUE (`category_type`);
