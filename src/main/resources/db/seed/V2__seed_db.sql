-- =====================================================================
-- V2__seed_data.sql
-- Seed dữ liệu cho Gốm Sứ Vũ Gia, trích từ nội dung tĩnh trong FE.
-- Ảnh chỉ lưu TÊN FILE (không kèm path), khớp với các file trong
-- src/assets/images/** và public/images/**.
-- Chạy SAU V1__init_db.sql.
-- =====================================================================
SET
    NAMES utf8mb4;

SET
    FOREIGN_KEY_CHECKS = 0;

-- Dọn dữ liệu cũ (giữ nguyên cấu trúc) để re-seed an toàn
TRUNCATE TABLE `product_images`;

TRUNCATE TABLE `products`;

TRUNCATE TABLE `product_categories`;

TRUNCATE TABLE `news`;

TRUNCATE TABLE `news_categories`;

TRUNCATE TABLE `banners`;

TRUNCATE TABLE `showrooms`;

TRUNCATE TABLE `gallery_images`;

TRUNCATE TABLE `faqs`;

TRUNCATE TABLE `pages`;

TRUNCATE TABLE `users`;

TRUNCATE TABLE `coupons`;

SET
    FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- USERS  (không có trong FE tĩnh — tạo sẵn tài khoản quản trị)
-- Mật khẩu mẫu bên dưới là bcrypt của "password" — ĐỔI trước khi dùng thật.
-- ---------------------------------------------------------------------
INSERT INTO
    `users` (
        `id`,
        `username`,
        `password`,
        `name`,
        `phone`,
        `email`,
        `gender`,
        `role`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        2,
        'admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Nhân viên Vũ Gia',
        '0966558808',
        'admin@gomvugia.vn',
        'Nữ',
        'ADMIN',
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- PRODUCT CATEGORIES  (từ CategoryNavigation.jsx / ProductToolbar.jsx)
-- ---------------------------------------------------------------------
INSERT INTO
    `product_categories` (
        `id`,
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
        1,
        'Men lam',
        'assets/images/products/product-category-thumb.png',
        1,
        'Dòng gốm men lam truyền thống Bát Tràng, sắc men xanh cổ điển vẽ tay tinh xảo.',
        'men-lam',
        TRUE,
        'Men lam',
        'Gốm sứ men lam Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        2,
        'Men rạn',
        'assets/images/products/product-category-thumb.png',
        2,
        'Dòng gốm men rạn cổ kính, vết rạn tự nhiên trang nghiêm cho không gian thờ tự.',
        'men-ran',
        TRUE,
        'Men rạn',
        'Gốm sứ men rạn cổ Bát Tràng chính hãng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        3,
        'Men lam vẽ vàng',
        'assets/images/products/product-category-thumb.png',
        3,
        'Men lam kết hợp vẽ vàng 24k cao cấp, sang trọng và bền màu theo thời gian.',
        'men-lam-ve-vang',
        TRUE,
        'Men lam vẽ vàng',
        'Gốm sứ men lam vẽ vàng 24k Bát Tràng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        4,
        'Men rạn dát vàng',
        'assets/images/products/product-category-thumb.png',
        4,
        'Men rạn cổ dát vàng, đắp nổi thủ công, đẳng cấp cho bộ đồ thờ gia tiên.',
        'men-ran-dat-vang',
        TRUE,
        'Men rạn dát vàng',
        'Gốm sứ men rạn dát vàng Bát Tràng Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        5,
        'Men màu theo mệnh',
        'assets/images/products/product-category-thumb.png',
        5,
        'Bộ sưu tập men màu phong thủy, phối màu tương sinh theo Ngũ hành bản mệnh gia chủ.',
        'men-mau-theo-menh',
        TRUE,
        'Men màu theo mệnh',
        'Gốm sứ men màu phong thủy theo mệnh Vũ Gia.',
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- PRODUCTS
--   id 1-8  : sản phẩm đơn (ALL_MOCK_PRODUCTS trong ProductsView.jsx)
--   id 9-11 : 3 sản phẩm con của combo (subItems trong ProductInfo.jsx)
--   id 12   : COMBO "Bộ đồ thờ ... DT026" (DEMO_PRODUCT)
-- ---------------------------------------------------------------------
INSERT INTO
    `products` (
        `id`,
        `name`,
        `thumb`,
        `sku`,
        `type`,
        `price`,
        `compare_at_price`,
        `sold_count`,
        `is_featured`,
        `status`,
        `description`,
        `combo_products`,
        `slug`,
        `priority`,
        `product_category_id`,
        `seo_title`,
        `seo_description`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        1,
        'Bát hương Men rạn Đắp nổi rồng chầu',
        'assets/images/products/product-image-thumb.png',
        'VG-DT001',
        'SINGLE',
        1200000,
        1500000,
        24,
        TRUE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Bát hương men rạn đắp nổi rồng chầu, chế tác thủ công tại làng nghề Bát Tràng, men rạn cổ trang nghiêm.'
                )
            )
        ),
        NULL,
        'bat-huong-men-ran-dap-noi-rong-chau',
        8,
        2,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        2,
        'Bình hoa cúc cổ Men lam vẽ vàng',
        'assets/images/products/product-new-thumb.png',
        'VG-DT002',
        'SINGLE',
        850000,
        1000000,
        15,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Bình hoa cúc cổ men lam vẽ vàng 24k, hoa văn tinh xảo, tôn lên vẻ sang trọng cho không gian thờ.'
                )
            )
        ),
        NULL,
        'binh-hoa-cuc-co-men-lam-ve-vang',
        7,
        3,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        3,
        'Kỷ 5 chén thờ Men lam vẽ vàng',
        'assets/images/products/product-image-thumb.png',
        'VG-DT003',
        'SINGLE',
        650000,
        800000,
        42,
        TRUE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Kỷ 5 chén thờ men lam vẽ vàng dùng đựng nước sạch hoặc rượu cúng trên bàn thờ gia tiên.'
                )
            )
        ),
        NULL,
        'ky-5-chen-tho-men-lam-ve-vang',
        6,
        3,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        4,
        'Mâm bồng đựng quả Men rạn đắp nổi',
        'assets/images/products/product-image-thumb.png',
        'VG-DT004',
        'SINGLE',
        950000,
        NULL,
        19,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Mâm bồng men rạn đắp nổi dùng bày hoa quả và lễ vật, đường nét chạm khắc thủ công sắc sảo.'
                )
            )
        ),
        NULL,
        'mam-bong-dung-qua-men-ran-dap-noi',
        5,
        4,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        5,
        'Đèn dầu thờ Men lam vẽ vàng kim',
        'assets/images/products/product-new-thumb.png',
        'VG-DT005',
        'SINGLE',
        450000,
        550000,
        31,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Đèn dầu thờ men lam vẽ vàng kim, thắp sáng và tạo sự trang nghiêm cho bàn thờ.'
                )
            )
        ),
        NULL,
        'den-dau-tho-men-lam-ve-vang-kim',
        4,
        1,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        6,
        'Ống hương thờ Men lam cổ điển',
        'assets/images/products/product-image-thumb.png',
        'VG-DT006',
        'SINGLE',
        380000,
        450000,
        8,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Ống hương thờ men lam cổ điển dùng cắm nhang chưa sử dụng, dáng thanh nhã.'
                )
            )
        ),
        NULL,
        'ong-huong-tho-men-lam-co-dien',
        3,
        1,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        7,
        'Nậm rượu thờ Men lam vẽ rồng chầu',
        'assets/images/products/product-image-thumb.png',
        'VG-DT007',
        'SINGLE',
        290000,
        350000,
        54,
        TRUE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Nậm rượu thờ men lam vẽ rồng chầu dùng đựng và dâng rượu cúng, họa tiết rồng uyển chuyển.'
                )
            )
        ),
        NULL,
        'nam-ruou-tho-men-lam-ve-rong-chau',
        2,
        1,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        8,
        'Chóe thờ đựng nước Men lam vẽ sen cổ',
        'assets/images/products/product-image-thumb.png',
        'VG-DT008',
        'SINGLE',
        320000,
        400000,
        27,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Chóe thờ men lam vẽ sen cổ dùng đựng gạo, muối và nước, họa tiết hoa sen thanh khiết.'
                )
            )
        ),
        NULL,
        'choe-tho-dung-nuoc-men-lam-ve-sen-co',
        1,
        1,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    -- Sản phẩm con của combo DT026
    (
        9,
        'Bát hương rồng phượng men lam',
        'assets/images/products/product-image-thumb.png',
        'VG-DT026-01',
        'SINGLE',
        2000000,
        2500000,
        12,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Bát hương rồng phượng men lam, đắp nổi vẽ tay, thuộc bộ đồ thờ DT026.'
                )
            )
        ),
        NULL,
        'bat-huong-rong-phuong-men-lam',
        0,
        1,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        10,
        'Bát hương Phúc lộc liên hoa vẽ vàng (mẫu 1)',
        'assets/images/products/product-new-thumb.png',
        'VG-DT026-02',
        'SINGLE',
        2000000,
        2500000,
        12,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Bát hương Phúc lộc liên hoa vẽ vàng, thuộc bộ đồ thờ DT026.'
                )
            )
        ),
        NULL,
        'bat-huong-phuc-loc-lien-hoa-ve-vang-1',
        0,
        3,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        11,
        'Bát hương Phúc lộc liên hoa vẽ vàng (mẫu 2)',
        'assets/images/products/product-image-thumb.png',
        'VG-DT026-03',
        'SINGLE',
        2000000,
        2500000,
        12,
        FALSE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Bát hương Phúc lộc liên hoa vẽ vàng, thuộc bộ đồ thờ DT026.'
                )
            )
        ),
        NULL,
        'bat-huong-phuc-loc-lien-hoa-ve-vang-2',
        0,
        3,
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    -- COMBO DT026 (DEMO_PRODUCT) — combo_products tham chiếu id 9,10,11
    (
        12,
        'Bộ đồ thờ Phật vẽ hoa sen men rạn cổ đơn giản DT026',
        'assets/images/products/product-image-thumb.png',
        'VG001',
        'COMBO',
        2000000,
        2500000,
        12,
        TRUE,
        'PUBLISHED',
        JSON_OBJECT(
            'blocks',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Bộ đồ thờ Phật vẽ hoa sen men rạn cổ Bát Tràng được chế tác thủ công tinh xảo, chất men rạn cổ kính trang nghiêm, thích hợp cho không gian thờ cúng gia đình.'
                ),
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Đồ thờ cúng Bát Tràng vẽ sen men rạn cổ là sự kết hợp hài hòa giữa nét vẽ tay mộc mạc và sắc men rạn truyền thống độc bản. Họa tiết hoa sen được khắc họa sống động, uyển chuyển, biểu trưng cho sự thanh cao, thuần khiết và lòng tôn kính vô hạn hướng về nguồn cội gia tiên.'
                ),
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Từng vật phẩm từ bát hương, bát thờ, mâm bồng đến chóe thờ đều trải qua quá trình nung luyện nhiệt độ cao nghiêm ngặt trên 1200 độ C. Nhờ đó, chất gốm trở nên đanh chắc, có độ bền cơ học cao và giữ màu sắc vĩnh cửu theo thời gian.'
                )
            ),
            'attributes',
            JSON_OBJECT(
                'classification',
                'Men rạn',
                'size',
                '120x120',
                'material',
                'Gốm sứ'
            ),
            'specifications',
            JSON_ARRAY(
                JSON_OBJECT(
                    'stt',
                    1,
                    'name',
                    'Bát hương',
                    'quantity',
                    '3',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng cắm hương, thờ Thần linh - Gia tiên'
                ),
                JSON_OBJECT(
                    'stt',
                    2,
                    'name',
                    'Bát thờ',
                    'quantity',
                    '10',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng dâng cơm trắng và lễ vật'
                ),
                JSON_OBJECT(
                    'stt',
                    3,
                    'name',
                    'Chóe thờ',
                    'quantity',
                    '3',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng đựng gạo, muối và nước'
                ),
                JSON_OBJECT(
                    'stt',
                    4,
                    'name',
                    'Bát sâm',
                    'quantity',
                    '1 - 2',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng dâng nước, trà hoặc sâm'
                ),
                JSON_OBJECT(
                    'stt',
                    5,
                    'name',
                    'Bộ kỷ chén',
                    'quantity',
                    '3 hoặc 5',
                    'unit',
                    'Chén',
                    'usage',
                    'Dùng đựng nước sạch hoặc rượu'
                ),
                JSON_OBJECT(
                    'stt',
                    6,
                    'name',
                    'Nậm rượu',
                    'quantity',
                    '1 - 2',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng đựng và dâng rượu cúng'
                ),
                JSON_OBJECT(
                    'stt',
                    7,
                    'name',
                    'Bộ ấm chén thờ (1 ấm - 5 chén)',
                    'quantity',
                    '1 - 2',
                    'unit',
                    'Bộ',
                    'usage',
                    'Dùng pha và dâng trà lên bàn thờ'
                ),
                JSON_OBJECT(
                    'stt',
                    8,
                    'name',
                    'Ống cắm hương',
                    'quantity',
                    '1',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng cắm nhang chưa sử dụng'
                ),
                JSON_OBJECT(
                    'stt',
                    9,
                    'name',
                    'Mâm bồng',
                    'quantity',
                    '3',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng bày hoa quả và lễ vật'
                ),
                JSON_OBJECT(
                    'stt',
                    10,
                    'name',
                    'Lọ hoa',
                    'quantity',
                    '2',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng cắm hoa trang trí bàn thờ'
                ),
                JSON_OBJECT(
                    'stt',
                    11,
                    'name',
                    'Đèn thờ',
                    'quantity',
                    '2',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng thắp sáng và tạo sự trang nghiêm'
                ),
                JSON_OBJECT(
                    'stt',
                    12,
                    'name',
                    'Chân nến',
                    'quantity',
                    '2',
                    'unit',
                    'Chiếc',
                    'usage',
                    'Dùng thắp sáng và tạo sự trang nghiêm'
                )
            )
        ),
        JSON_ARRAY(
            JSON_OBJECT('productId', 9, 'sortOrder', 1),
            JSON_OBJECT('productId', 10, 'sortOrder', 2),
            JSON_OBJECT('productId', 11, 'sortOrder', 3)
        ),
        'bo-do-tho-phat-ve-hoa-sen-men-ran-co-dt026',
        10,
        2,
        'Bộ đồ thờ Phật vẽ hoa sen men rạn cổ DT026',
        'Bộ đồ thờ Phật men rạn cổ vẽ hoa sen Bát Tràng, chế tác thủ công, bảo hành men trọn đời.',
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- PRODUCT IMAGES  (gallery ảnh — lưu tên file)
-- ---------------------------------------------------------------------
INSERT INTO
    `product_images` (`url`, `priority`, `product_id`)
VALUES
    -- Sản phẩm đơn: mỗi sản phẩm 1 ảnh chính (= thumb)
    ('assets/images/product-detail/product-card-image-1.png', 0, 1),
    ('assets/images/product-detail/product-card-image-2.png', 0, 2),
    ('assets/images/product-detail/product-card-image-1.png', 0, 3),
    ('assets/images/product-detail/product-card-image-1.png', 0, 4),
    ('assets/images/product-detail/product-card-image-2.png', 0, 5),
    ('assets/images/product-detail/product-card-image-1.png', 0, 6),
    ('assets/images/product-detail/product-card-image-1.png', 0, 7),
    ('assets/images/product-detail/product-card-image-1.png', 0, 8),
    -- Sản phẩm con combo
    ('assets/images/product-detail/product-card-image-1.png', 0, 9),
    ('assets/images/product-detail/product-card-image-2.png', 0, 10),
    ('assets/images/product-detail/product-card-image-1.png', 0, 11),
    -- COMBO DT026: 4 ảnh gallery (ProductInfo.jsx)
    ('assets/images/product-detail/product-card-image-1.png', 0, 12),
    ('assets/images/product-detail/product-card-image-1.png', 1, 12),
    ('assets/images/product-detail/product-card-image-2.png', 2, 12),
    ('assets/images/product-detail/product-card-image-1.png', 3, 12);

-- ---------------------------------------------------------------------
-- NEWS CATEGORIES  (TABS trong NewsView.jsx)
-- ---------------------------------------------------------------------
INSERT INTO
    `news_categories` (
        `id`,
        `name`,
        `slug`,
        `priority`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        1,
        'Kiến thức phong thuỷ',
        'phong-thuy',
        1,
        NOW(),
        NOW()
    ),
    (
        2,
        'Kiến thức sản phẩm',
        'san-pham',
        2,
        NOW(),
        NOW()
    ),
    (
        3,
        'Cẩm nang làng nghề',
        'lang-nghe',
        3,
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- NEWS  (MOCK_ALL_NEWS trong NewsView.jsx — 21 bài + 1 bài chi tiết demo)
-- des lưu dạng JSON các khối nội dung. priority cao = bài nổi bật.
-- ---------------------------------------------------------------------
INSERT INTO
    `news` (
        `id`,
        `title`,
        `thumb`,
        `short_content`,
        `des`,
        `slug`,
        `priority`,
        `status`,
        `published_at`,
        `news_category_id`,
        `created_at`,
        `updated_at`
    )
VALUES
    -- Kiến thức phong thuỷ (cat 1)
    (
        1,
        'Ý nghĩa chữ Thọ tròn trong không gian tâm linh Việt',
        'assets/images/home/home-new-1.png',
        'Khám phá chiều sâu văn hóa truyền thống và ý nghĩa biểu tượng của họa tiết chữ Thọ tròn tinh xảo trên các tác phẩm thờ cúng.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Khám phá chiều sâu văn hóa truyền thống và ý nghĩa biểu tượng của họa tiết chữ Thọ tròn tinh xảo trên các tác phẩm thờ cúng.'
            )
        ),
        'y-nghia-chu-tho-tron-trong-tam-linh-viet',
        0,
        'PUBLISHED',
        NOW(),
        1,
        NOW(),
        NOW()
    ),
    (
        2,
        'Cách bài trí bàn thờ gia tiên chuẩn phong thủy rước tài lộc',
        'assets/images/home/home-new-2.png',
        'Hướng dẫn chi tiết từ nghệ nhân làng nghề cách sắp xếp vị trí bát hương, kỷ nước và mâm bồng mang lại bình an.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Hướng dẫn chi tiết từ nghệ nhân làng nghề cách sắp xếp vị trí bát hương, kỷ nước và mâm bồng mang lại bình an.'
            )
        ),
        'cach-bai-tri-ban-tho-gia-tien-chuan-phong-thuy',
        10,
        'PUBLISHED',
        NOW(),
        1,
        NOW(),
        NOW()
    ),
    (
        3,
        'Chọn bình hút tài lộc theo bản mệnh gia chủ cát tường',
        'assets/images/home/home-new-1.png',
        'Bí quyết chọn lựa màu men, kiểu dáng bình hút lộc Bát Tràng tương sinh theo Ngũ hành của gia chủ.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Bí quyết chọn lựa màu men, kiểu dáng bình hút lộc Bát Tràng tương sinh theo Ngũ hành của gia chủ.'
            )
        ),
        'chon-binh-hut-tai-loc-theo-ban-menh',
        0,
        'PUBLISHED',
        NOW(),
        1,
        NOW(),
        NOW()
    ),
    (
        10,
        'Vị trí đặt lộc bình phòng khách mang lại tài lộc dồi dào',
        'assets/images/home/home-new-1.png',
        'Cách bố trí đôi lộc bình trong phòng khách vừa tăng tính thẩm mỹ sang trọng vừa hợp phong thủy.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Cách bố trí đôi lộc bình trong phòng khách vừa tăng tính thẩm mỹ sang trọng vừa hợp phong thủy.'
            )
        ),
        'vi-tri-dat-loc-binh-phong-khach',
        10,
        'PUBLISHED',
        NOW(),
        1,
        NOW(),
        NOW()
    ),
    (
        11,
        'Nguyên tắc tam hợp trong lựa chọn họa tiết gốm tâm linh',
        'assets/images/home/home-new-2.png',
        'Tìm hiểu về nguyên tắc kết hợp họa tiết phong thủy giúp gia đạo êm ấm, công danh thăng tiến.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Tìm hiểu về nguyên tắc kết hợp họa tiết phong thủy giúp gia đạo êm ấm, công danh thăng tiến.'
            )
        ),
        'nguyen-tac-tam-hop-hoa-tiet-gom',
        0,
        'PUBLISHED',
        NOW(),
        1,
        NOW(),
        NOW()
    ),
    (
        12,
        'Ý nghĩa của đôi hạc chầu trên bàn thờ gia tiên Việt',
        'assets/images/home/home-new-1.png',
        'Hạc thờ bằng gốm sứ Bát Tràng biểu tượng cho sự trường thọ, thanh cao và kết nối tâm linh bền chặt.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Hạc thờ bằng gốm sứ Bát Tràng biểu tượng cho sự trường thọ, thanh cao và kết nối tâm linh bền chặt.'
            )
        ),
        'y-nghia-doi-hac-chau-tren-ban-tho',
        0,
        'PUBLISHED',
        NOW(),
        1,
        NOW(),
        NOW()
    ),
    (
        13,
        'Cách bao sái bàn thờ cuối năm không lo động bát hương',
        'assets/images/home/home-new-1.png',
        'Quy trình lau dọn bàn thờ thành kính, đúng cách để giữ gìn phước đức và may mắn cho năm mới.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Quy trình lau dọn bàn thờ thành kính, đúng cách để giữ gìn phước đức và may mắn cho năm mới.'
            )
        ),
        'cach-bao-sai-ban-tho-cuoi-nam',
        0,
        'PUBLISHED',
        NOW(),
        1,
        NOW(),
        NOW()
    ),
    -- Kiến thức sản phẩm (cat 2)
    (
        4,
        'Ý nghĩa lịch sử hào hùng ngày giải phóng miền Nam 30/4/1975',
        'assets/images/home/home-new-2.png',
        'Nhìn lại dòng chảy lịch sử vẻ vang và những cảm hứng nghệ thuật gốm sứ truyền tải thông điệp yêu nước.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Nhìn lại dòng chảy lịch sử vẻ vang và những cảm hứng nghệ thuật gốm sứ truyền tải thông điệp yêu nước.'
            )
        ),
        'giai-phong-mien-nam-30-4-1975-lich-su',
        0,
        'PUBLISHED',
        NOW(),
        2,
        NOW(),
        NOW()
    ),
    (
        5,
        'Phân biệt men rạn cổ và men lam truyền thống Bát Tràng',
        'assets/images/home/home-new-1.png',
        'Giúp người sưu tầm gốm nhận diện rõ nét đặc trưng độc bản của hai dòng men trứ danh ngàn năm tuổi.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Giúp người sưu tầm gốm nhận diện rõ nét đặc trưng độc bản của hai dòng men trứ danh ngàn năm tuổi.'
            )
        ),
        'phan-biet-men-ran-co-va-men-lam',
        10,
        'PUBLISHED',
        NOW(),
        2,
        NOW(),
        NOW()
    ),
    (
        6,
        'Quy trình chế tác đôi lục bình đắp nổi thủ công cầu kỳ',
        'assets/images/home/home-new-1.png',
        'Hành trình từ đất sét thô sơ qua đôi tay nghệ nhân tạo tác hoa văn rồng chầu, hoa sen tinh xảo.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Hành trình từ đất sét thô sơ qua đôi tay nghệ nhân tạo tác hoa văn rồng chầu, hoa sen tinh xảo.'
            )
        ),
        'quy-trinh-che-tac-luc-binh-dap-noi',
        0,
        'PUBLISHED',
        NOW(),
        2,
        NOW(),
        NOW()
    ),
    (
        14,
        'Đặc điểm nhận biết gốm vuốt tay thủ công cao cấp',
        'assets/images/home/home-new-2.png',
        'Cách cảm nhận xương đất, độ dày và đường nét độc bản của các tác phẩm được chế tác hoàn toàn thủ công.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Cách cảm nhận xương đất, độ dày và đường nét độc bản của các tác phẩm được chế tác hoàn toàn thủ công.'
            )
        ),
        'dac-diem-nhan-biet-gom-vuot-tay',
        0,
        'PUBLISHED',
        NOW(),
        2,
        NOW(),
        NOW()
    ),
    (
        15,
        'Cách bảo quan và làm sạch bộ đồ thờ men rạn đắp nổi',
        'assets/images/home/home-new-1.png',
        'Mẹo nhỏ giúp giữ gìn độ sáng bóng, tránh bụi bẩn mà không ảnh hưởng đến lớp men rạn quý hiếm.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Mẹo nhỏ giúp giữ gìn độ sáng bóng, tránh bụi bẩn mà không ảnh hưởng đến lớp men rạn quý hiếm.'
            )
        ),
        'cach-bao-quan-do-tho-men-ran',
        0,
        'PUBLISHED',
        NOW(),
        2,
        NOW(),
        NOW()
    ),
    (
        16,
        'Nghệ thuật vẽ vàng 24k trên nền gốm sứ tâm linh Vũ Gia',
        'assets/images/home/home-new-1.png',
        'Tìm hiểu quy trình vẽ nhũ vàng và nung hấp nhiệt độ cao giúp vàng bám chặt bền bỉ cùng thời gian.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Tìm hiểu quy trình vẽ nhũ vàng và nung hấp nhiệt độ cao giúp vàng bám chặt bền bỉ cùng thời gian.'
            )
        ),
        'nghe-thuat-ve-vang-24k-tren-gom',
        0,
        'PUBLISHED',
        NOW(),
        2,
        NOW(),
        NOW()
    ),
    (
        17,
        'Tại sao ấm trà tử sa Bát Tràng lại được người trà hữu tin dùng?',
        'assets/images/home/home-new-2.png',
        'Sự kết hợp hoàn hảo giữa chất đất khoáng tự nhiên và kỹ thuật nung chuẩn xác giữ trọn hương vị trà.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Sự kết hợp hoàn hảo giữa chất đất khoáng tự nhiên và kỹ thuật nung chuẩn xác giữ trọn hương vị trà.'
            )
        ),
        'tai-sao-am-tra-tu-sa-duoc-tin-dung',
        0,
        'PUBLISHED',
        NOW(),
        2,
        NOW(),
        NOW()
    ),
    -- Cẩm nang làng nghề (cat 3)
    (
        7,
        'Lịch sử ngàn năm gìn giữ và thổi bùng ngọn lửa Bát Tràng',
        'assets/images/home/home-new-1.png',
        'Làng cổ Bát Tràng vượt qua thăng trầm thời gian để giữ vững thương hiệu gốm sứ tinh hoa số một Việt Nam.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Làng cổ Bát Tràng vượt qua thăng trầm thời gian để giữ vững thương hiệu gốm sứ tinh hoa số một Việt Nam.'
            )
        ),
        'lich-su-ngan-nam-ngon-lua-bat-trang',
        10,
        'PUBLISHED',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    (
        8,
        'Nghệ nhân ưu tú Giang Cao và khát vọng đánh thức đất sét',
        'assets/images/home/home-new-1.png',
        'Lắng nghe những chia sẻ tâm huyết từ thế hệ giữ lửa Bát Tràng truyền thụ tình yêu nghề gốm cho người trẻ.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Lắng nghe những chia sẻ tâm huyết từ thế hệ giữ lửa Bát Tràng truyền thụ tình yêu nghề gốm cho người trẻ.'
            )
        ),
        'nghe-nhan-uu-tu-giang-cao-va-khay-vong-dat',
        0,
        'PUBLISHED',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    (
        9,
        'Bí quyết dưỡng ấm trà tử sa chuẩn nghệ thuật thưởng trà',
        'assets/images/home/home-new-2.png',
        'Làm thế nào để chiếc ấm đất của bạn có độ bóng đẹp tự nhiên và giữ trọn hương vị trà thượng hạng.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Làm thế nào để chiếc ấm đất của bạn có độ bóng đẹp tự nhiên và giữ trọn hương vị trà thượng hạng.'
            )
        ),
        'bi-quyet-duong-am-tra-tu-sa-chuan',
        0,
        'PUBLISHED',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    (
        18,
        'Kỹ thuật chế tác men ngọc độc bản truyền thừa dòng họ Vũ',
        'assets/images/home/home-new-1.png',
        'Học hỏi phương pháp phối trộn tro trấu và đất sét trắng tạo nên sắc men trong vắt như ngọc bích.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Học hỏi phương pháp phối trộn tro trấu và đất sét trắng tạo nên sắc men trong vắt như ngọc bích.'
            )
        ),
        'ky-thuat-che-tac-men-ngoc-truyen-thua',
        0,
        'PUBLISHED',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    (
        19,
        'Hành trình đưa gốm sứ Việt vươn tầm thế giới',
        'assets/images/home/home-new-1.png',
        'Những bước chuyển mình xuất khẩu gốm mỹ nghệ sang thị trường Nhật Bản và châu Âu của nghệ nhân Bát Tràng.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Những bước chuyển mình xuất khẩu gốm mỹ nghệ sang thị trường Nhật Bản và châu Âu của nghệ nhân Bát Tràng.'
            )
        ),
        'hanh-trinh-dua-gom-viet-vuon-tam-the-gioi',
        0,
        'PUBLISHED',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    (
        20,
        'Lễ hội đình làng Bát Tràng - nét đẹp văn hóa tâm linh',
        'assets/images/home/home-new-2.png',
        'Tìm hiểu các hoạt động rước nước, dâng hương tôn vinh các vị tổ nghề gốm truyền thống hàng năm.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Tìm hiểu các hoạt động rước nước, dâng hương tôn vinh các vị tổ nghề gốm truyền thống hàng năm.'
            )
        ),
        'le-hoi-dinh-lang-bat-trang',
        0,
        'PUBLISHED',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    (
        21,
        'Đất sét trắng - bầu sữa mẹ nuôi dưỡng làng nghề gốm sứ',
        'assets/images/home/home-new-1.png',
        'Cách tuyển chọn và xử lý nguồn đất sét dẻo mịn tạo nên cốt gốm đanh chắc đặc trưng Bát Tràng.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Cách tuyển chọn và xử lý nguồn đất sét dẻo mịn tạo nên cốt gốm đanh chắc đặc trưng Bát Tràng.'
            )
        ),
        'dat-set-trang-bau-sua-me-nuoi-duong',
        0,
        'PUBLISHED',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    -- Bài chi tiết demo (NewsDetailView.jsx) — có nội dung đầy đủ
    (
        22,
        'Giải đáp ý nghĩa bát hương rồng 4 móng và rồng 5 móng?',
        'assets/images/home/home-new-2.png',
        'Phân biệt ý nghĩa hình tượng rồng 4 móng và rồng 5 móng trên bát hương thờ cúng theo văn hóa tâm linh Việt.',
        JSON_ARRAY(
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Nhắc đến gốm sứ người ta sẽ nghĩ ngay đến làng gốm hàng nghìn năm Bát Tràng. Làng nghề truyền thống Bát Tràng chính là cái nôi sinh ra các nghệ nhân làm gốm cũng như các sản phẩm gốm đều được thổi hồn mang một nét đẹp riêng biệt của truyền thống.'
            ),
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Qua bàn tay tài hoa và cái tâm của nghệ nhân làm gốm mà trở nên có hồn. Gốm Bát Tràng có rất nhiều các mẫu gốm phục vụ mọi nhu cầu của người tiêu dùng, từ gốm sứ gia dụng, đồ thờ cho đến vật phẩm phong thủy.'
            ),
            JSON_OBJECT(
                'type',
                'image',
                'src',
                'assets/images/home/home-new-2.png',
                'caption',
                'Hình 1: Nghệ nhân tại xưởng sản xuất gốm tâm linh Vũ Gia đang thảo luận thiết kế độc quyền.'
            ),
            JSON_OBJECT(
                'type',
                'heading',
                'text',
                'Phân biệt Rồng 4 móng và Rồng 5 móng trên Bát Hương thờ cúng'
            ),
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Trong văn hóa tâm linh Việt Nam, hình tượng Rồng chầu mặt nguyệt luôn mang ý nghĩa tối thượng về quyền lực, tài lộc và sự bình an.'
            ),
            JSON_OBJECT(
                'type',
                'list',
                'items',
                JSON_ARRAY(
                    JSON_OBJECT(
                        'title',
                        '1. Rồng 5 móng (Ngũ Trảo Kim Long):',
                        'text',
                        'Rồng 5 móng xưa nay vốn là biểu tượng của Hoàng đế, đại diện cho thiên tử và quyền lực hoàng gia tuyệt đối. Trên các đồ thờ cúng, họa tiết rồng 5 móng thường được đắp nổi tinh xảo, thể hiện sự trang nghiêm tối cao.'
                    ),
                    JSON_OBJECT(
                        'title',
                        '2. Rồng 4 móng (Mãng Long):',
                        'text',
                        'Rồng 4 móng thường được dùng cho các quan lại, hoàng thân quốc thích hoặc đền chùa dân gian, mang tính chất gần gũi hơn với đời sống tâm linh của nhân dân.'
                    )
                )
            ),
            JSON_OBJECT(
                'type',
                'image',
                'src',
                'assets/images/home/home-new-2.png',
                'caption',
                'Quy trình tạo hình gốm vuốt tay thủ công từ phôi đất sét Bát Tràng.'
            ),
            JSON_OBJECT(
                'type',
                'image',
                'src',
                'assets/images/home/home-new-1.png',
                'caption',
                'Nghệ nhân vẽ nhũ vàng và họa màu rồng đắp nổi trực tiếp trên cốt gốm.'
            ),
            JSON_OBJECT(
                'type',
                'quote',
                'text',
                'Mỗi họa tiết rồng ngậm ngọc trên bát hương thờ cúng Vũ Gia đều được các nghệ nhân dồn toàn bộ tâm huyết, vẽ tay tỉ mỉ và nung ở nhiệt độ chuẩn 1300 độ C để tạo ra nước men bóng bẩy, bền bỉ ngàn năm.'
            ),
            JSON_OBJECT(
                'type',
                'paragraph',
                'text',
                'Để chọn được bộ đồ thờ chuẩn phong thủy, quý khách nên chú ý đến kích thước bát hương hài hòa với không gian ban thờ, cũng như màu men tương sinh với bản mệnh gia chủ.'
            )
        ),
        'giai-dap-y-nghia-bat-huong-rong-4-mong-va-5-mong',
        20,
        'PUBLISHED',
        '2026-05-18 09:00:00',
        2,
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- BANNERS  (HomeHero.jsx + HomeCategoryBanners.jsx)
-- ---------------------------------------------------------------------
INSERT INTO
    `banners` (
        `title`,
        `image_url`,
        `link_url`,
        `position`,
        `sort_order`,
        `is_active`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        'Ưu đãi tháng 6 - 299.000đ - Sản phẩm gốm Bát Tràng',
        'assets/images/home/hero-image-1-top.png',
        '/san-pham',
        'HOME_HERO',
        1,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Các sản phẩm nổi bật',
        'assets/images/home/hero-image-2-left.png',
        '/san-pham',
        'HOME_HERO',
        2,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Phong thuỷ, Trang trí',
        'assets/images/home/hero-image-3-right.png',
        '/san-pham',
        'HOME_HERO',
        3,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Ấm chén Bát Tràng',
        'assets/images/nha-xuong/slider-image-1.png',
        '/san-pham',
        'HOME_CATEGORY',
        1,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Chum sành ngâm rượu',
        'assets/images/nha-xuong/slider-image-2.png',
        '/san-pham',
        'HOME_CATEGORY',
        2,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Quà tặng Bát Tràng',
        'assets/images/nha-xuong/slider-image-3.png',
        '/san-pham',
        'HOME_CATEGORY',
        3,
        TRUE,
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- SHOWROOMS  (ShowroomIntro.jsx / ShowroomMap.jsx)
-- ---------------------------------------------------------------------
INSERT INTO
    `showrooms` (
        `name`,
        `phone`,
        `address`,
        `map_embed_url`,
        `opening_hours`,
        `sort_order`,
        `is_active`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        'Showroom Bát Tràng',
        '0966558808',
        '18 Giang Cao, Bát Tràng, Gia Lâm, Hà Nội',
        'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3725.267812970921!2d105.9238384!3d20.9818818!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3135af9e2a53ff33%3A0xe1adbfab1562e1ad!2zMTggR2lhbmcgQ2FvLCBCw6F0IFRyw6BuZywgR2lhIEzDom0sIEjDoCBO4buZaQ!5e0!3m2!1svi!2s!4v1718360000000!5m2!1svi!2s',
        '08:00 - 18:00 (Thứ 2 - Chủ nhật)',
        1,
        TRUE,
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- GALLERY IMAGES  (GalleryView.jsx — "Hình ảnh của khách hàng")
-- ---------------------------------------------------------------------
INSERT INTO
    `gallery_images` (
        `image_url`,
        `title`,
        `category`,
        `sort_order`,
        `is_active`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        'assets/images/gallery/gallery-1.jpg',
        'Kệ phơi sản phẩm gốm mộc tại xưởng chế tác',
        'Hình ảnh của khách hàng',
        1,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'assets/images/gallery/gallery-2.jpg',
        'Các chi tiết ấm chén trà được nghệ nhân tạo hình hoàn thiện',
        'Hình ảnh của khách hàng',
        2,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'assets/images/gallery/gallery-3.jpg',
        'Nghệ nhân gốm khéo léo tạo dáng bình trên bàn xoay truyền thống',
        'Hình ảnh của khách hàng',
        3,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'assets/images/gallery/gallery-4.jpg',
        'Hàng gốm mộc xếp đều tăm tắp chờ công đoạn tráng men',
        'Hình ảnh của khách hàng',
        4,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'assets/images/gallery/gallery-5.jpg',
        'Các tác phẩm bình phong thủy men rạn độc bản hoàn thiện',
        'Hình ảnh của khách hàng',
        5,
        TRUE,
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- FAQS  (FaqView.jsx — 5 nhóm)
-- ---------------------------------------------------------------------
INSERT INTO
    `faqs` (
        `question`,
        `answer`,
        `category`,
        `sort_order`,
        `is_active`,
        `created_at`,
        `updated_at`
    )
VALUES
    -- Sản phẩm
    (
        'Gốm sứ xây dựng Vũ Gia có phải là hàng thủ công không?',
        'Đúng vậy. Chúng tôi tự hào duy trì quy trình sản xuất thủ công truyền thống. Từ khâu chọn đất, tạo hình, đến tráng men và nung lò. Mỗi sản phẩm đều mang dấu ấn bàn tay khéo léo của các nghệ nhân. Điều này tạo nên vẻ đẹp độc bản mà các loại gạch ngói công nghiệp sản xuất hàng loạt không thể có được.',
        'Sản phẩm',
        1,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Tôi có thể mua hàng như thế nào?',
        'Bạn có thể mua hàng trực tiếp tại showroom, qua Hotline hoặc các kênh mạng xã hội của chúng tôi.',
        'Sản phẩm',
        2,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Tôi có thể lấy mẫu thử không?',
        'Chúng tôi sẵn sàng gửi mẫu thử cho khách hàng ở xa. Vui lòng liên hệ để được hỗ trợ.',
        'Sản phẩm',
        3,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Các sản phẩm của gốm sứ Vũ Gia có bền khi sử dụng ngoài trời hay không?',
        'Tất cả sản phẩm của chúng tôi đều được nung ở nhiệt độ cao (1200°C), đảm bảo độ bền tuyệt đối khi sử dụng ngoài trời. Chất liệu đất sét Bát Tràng kết hợp men hỏa biến giúp sản phẩm chống thấm nước, chống rêu mốc vĩnh viễn.',
        'Sản phẩm',
        4,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Màu men có bị phai dưới ánh nắng mặt trời không?',
        'Lớp men gốm được nung hỏa biến ở nhiệt độ 1200°C, cam kết không bao giờ phai màu dưới tác động của thời tiết. Màu men hòa quyện vào xương gốm trong quá trình nung, tạo nên độ bền màu vĩnh cửu.',
        'Sản phẩm',
        5,
        TRUE,
        NOW(),
        NOW()
    ),
    -- Báo giá
    (
        'Giá sản phẩm được tính như thế nào?',
        'Giá gốm sứ xây dựng thường được tính theo mét vuông (m²), mét dài (md) hoặc theo viên/cặp đối với các dòng gạch, ngói và tính theo đơn vị đôi/chiếc đối với các sản phẩm đơn lẻ. Giá phụ thuộc vào kích thước, loại men, và độ phức tạp của hình dáng sản phẩm.',
        'Báo giá',
        6,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Đặt hàng số lượng lớn có được chiết khấu không?',
        'Chúng tôi luôn có chính sách chiết khấu linh hoạt và cạnh tranh cho các đơn hàng số lượng lớn, đặc biệt là các dự án công trình trọng điểm. Vui lòng liên hệ trực tiếp để nhận báo giá ưu đãi nhất.',
        'Báo giá',
        7,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Có yêu cầu số lượng đặt hàng tối thiểu không?',
        'Chúng tôi tiếp nhận mọi đơn hàng, từ một sản phẩm đơn lẻ đến các đơn hàng lớn cho công trình quy mô hàng nghìn mét vuông.',
        'Báo giá',
        8,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Màu sắc có ảnh hưởng đến giá sản phẩm không?',
        'Một số màu men hỏa biến đặc biệt hoặc yêu cầu pha chế màu riêng theo thiết kế có thể có sự chênh lệch nhẹ về giá so với các màu men tiêu chuẩn.',
        'Báo giá',
        9,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Tại sao các kích thước nhỏ lại đắt hơn nhiều so với các kích thước lớn?',
        'Kích thước nhỏ đòi hỏi sự tỉ mỉ cao hơn trong khâu tạo hình và hoàn thiện thủ công. Công sức cho mỗi cm² sản phẩm nhỏ lớn hơn đáng kể, đồng thời tỷ lệ hao hụt trong quá trình nung cũng cao hơn.',
        'Báo giá',
        10,
        TRUE,
        NOW(),
        NOW()
    ),
    -- Vận chuyển & thời gian giao hàng
    (
        'Thời gian sản xuất và giao hàng là bao lâu?',
        'Đối với hàng có sẵn: Chúng tôi có thể giao hàng trong vòng 2-5 ngày làm việc.<br/>Đối với hàng đặt sản xuất: Thường mất từ 3-6 tuần tùy vào quy mô đơn hàng và điều kiện thời tiết (ảnh hưởng đến quá trình phơi gốm mộc).',
        'Vận chuyển & thời gian giao hàng',
        11,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Các bạn có giao hàng toàn quốc không?',
        'Chúng tôi vận chuyển toàn quốc bằng xe tải chuyên dụng hoặc đối tác logistic uy tín. Hàng hóa được đóng gói cẩn thận, đảm bảo an toàn trong suốt quá trình vận chuyển.',
        'Vận chuyển & thời gian giao hàng',
        12,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Tôi nên lưu ý gì khi lắp đặt gốm thủ công?',
        'Nên sử dụng thợ có tay nghề và am hiểu đặc tính gốm nung thủ công. Chúng tôi luôn cung cấp tài liệu hướng dẫn lắp đặt chi tiết kèm theo mỗi đơn hàng.',
        'Vận chuyển & thời gian giao hàng',
        13,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Các bạn có vận chuyển quốc tế không?',
        'Có. Chúng tôi hỗ trợ đóng gói kiện gỗ xuất khẩu đạt chuẩn và làm thủ tục hải quan cần thiết cho các đơn hàng quốc tế.',
        'Vận chuyển & thời gian giao hàng',
        14,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Tôi có thể tự đến lấy hàng trực tiếp không?',
        'Quý khách có thể nhận hàng trực tiếp tại xưởng sản xuất hoặc showroom của chúng tôi. Vui lòng liên hệ trước để chúng tôi chuẩn bị hàng sẵn sàng.',
        'Vận chuyển & thời gian giao hàng',
        15,
        TRUE,
        NOW(),
        NOW()
    ),
    -- Chính sách bảo hành
    (
        'Gốm sứ xây dựng Vũ Gia có chính sách bảo hành như thế nào?',
        'Chúng tôi bảo hành độ bền màu men trọn đời đối với tất cả các dòng sản phẩm gốm sứ xây dựng và trang trí. Đối với độ bền xương gốm, cam kết bảo hành 10 năm trong điều kiện thời tiết tự nhiên thông thường.',
        'Chính sách bảo hành',
        16,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Làm thế nào để yêu cầu xử lý bảo hành?',
        'Quý khách chỉ cần liên hệ Hotline chăm sóc khách hàng, cung cấp số điện thoại đặt hàng hoặc mã hóa đơn. Đội ngũ kỹ thuật của Vũ Gia sẽ phản hồi và tiến hành xác minh thực tế trong vòng 48h.',
        'Chính sách bảo hành',
        17,
        TRUE,
        NOW(),
        NOW()
    ),
    -- Đổi trả
    (
        'Chính sách đổi trả sản phẩm như thế nào?',
        'Khách hàng được quyền đổi trả sản phẩm trong vòng 7 ngày kể từ khi nhận hàng đối với các trường hợp: sản phẩm bị nứt vỡ do lỗi vận chuyển, lỗi tráng men nghiêm trọng hoặc giao sai mẫu mã so với hợp đồng đã ký kết.',
        'Đổi trả',
        18,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'Đơn hàng đặt riêng (sản xuất theo yêu cầu) có được đổi trả không?',
        'Đối với các đơn hàng đặt riêng theo yêu cầu thiết kế đặc biệt của khách hàng, chúng tôi chỉ áp dụng chính sách đổi trả/thay thế đối với sản phẩm bị lỗi kỹ thuật trong khâu sản xuất hoặc nứt vỡ do vận chuyển.',
        'Đổi trả',
        19,
        TRUE,
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- PAGES  (nội dung trang tĩnh + SEO)
-- ---------------------------------------------------------------------
INSERT INTO
    `pages` (
        `key`,
        `title`,
        `content`,
        `hero_title`,
        `hero_subtitle`,
        `hero_des`,
        `hero_image`,
        `status`,
        `seo_title`,
        `seo_description`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        'home',
        'Trang chủ',
        JSON_OBJECT(
            'sections',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'productList',
                    'title',
                    'BỘ ĐỒ THỜ TRUYỀN THỐNG',
                    'tabs',
                    JSON_ARRAY(
                        'BỘ ĐỒ THỜ MEN LAM',
                        'BỘ ĐỒ THỜ LAM VẼ VÀNG 24K',
                        'BỘ ĐỒ THỜ MEN RẠN'
                    )
                ),
                JSON_OBJECT(
                    'type',
                    'productList',
                    'title',
                    'BÌNH PHONG THỦY',
                    'tabs',
                    JSON_ARRAY('BÌNH MEN MÀU', 'BÌNH MEN LAM', 'BÌNH ĐẮP NỔI')
                ),
                JSON_OBJECT(
                    'type',
                    'productList',
                    'title',
                    'LỤC BÌNH GỐM SỨ',
                    'tabs',
                    JSON_ARRAY(
                        'LỤC BÌNH MEN MÀU',
                        'LỤC BÌNH MEN LAM',
                        'LỤC BÌNH ĐẮP NỔI'
                    )
                )
            )
        ),
        'Ưu đãi tháng 6 - 299.000đ - Sản phẩm gốm Bát Tràng',
        NULL,
        NULL,
        'assets/images/home/hero-image-1-top.png',
        'PUBLISHED',
        'Trang chủ',
        'Gốm Sứ Vũ Gia - gốm sứ Bát Tràng chính hãng.',
        NOW(),
        NOW()
    ),
    (
        'about',
        'Về chúng tôi',
        JSON_OBJECT(
            'sections',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'heading',
                    'text',
                    'HỘI TỤ TINH HOA LÀNG NGHỀ'
                ),
                JSON_OBJECT(
                    'type',
                    'block',
                    'image',
                    'assets/images/about/about-image-1.jpg',
                    'text',
                    'Gốm Sứ Vũ Gia kết tinh di sản nghìn năm lửa đỏ Bát Tràng, mỗi tác phẩm là dấu ấn bàn tay nghệ nhân.'
                ),
                JSON_OBJECT(
                    'type',
                    'block',
                    'image',
                    'assets/images/about/about-image-2.jpg',
                    'text',
                    'Chúng tôi kiên trì gìn giữ phương thức thủ công truyền thống trong từng công đoạn chế tác.'
                ),
                JSON_OBJECT(
                    'type',
                    'image',
                    'image',
                    'assets/images/about/about-image-3.jpg',
                    'text',
                    'Quy trình sản xuất Gốm Vũ Gia'
                )
            )
        ),
        NULL,
        NULL,
        'Kết tinh di sản rực rỡ nghìn năm lửa đỏ Bát Tràng.',
        'assets/images/about/hero-bg.jpg',
        'PUBLISHED',
        'Về chúng tôi',
        'Gốm Sứ Vũ Gia - kết tinh di sản rực rỡ nghìn năm lửa đỏ Bát Tràng.',
        NOW(),
        NOW()
    ),
    (
        'factory',
        'Nhà xưởng',
        JSON_OBJECT(
            'sections',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'paragraph',
                    'text',
                    'Để đáp ứng những đơn hàng lớn cho các công trình trọng điểm như đình chùa, biệt thự hay khu nghỉ dưỡng, Vũ Gia đã đầu tư hệ thống nhà xưởng với tổng diện tích lên đến 5.000m², được thiết kế tối ưu với 3 tầng sản xuất. Việc mở rộng không gian không chỉ khẳng định năng lực cung ứng mạnh mẽ mà còn giúp chúng tôi kiểm soát chất lượng sản phẩm một cách khắt khe nhất.'
                ),
                JSON_OBJECT(
                    'type',
                    'quote',
                    'text',
                    'Lựa chọn Gốm Vũ Gia, quý khách không chỉ mua một loại vật liệu xây dựng, mà đang đặt niềm tin vào một quy trình vận hành tận tâm, chuyên nghiệp và những giá trị văn hóa bền vững theo thời gian.'
                ),
                JSON_OBJECT(
                    'type',
                    'block',
                    'heading',
                    'Sức mạnh của sự kết hợp: Máy móc hiện đại & Bàn tay nghệ nhân',
                    'text',
                    'Dù sở hữu hệ thống máy móc hỗ trợ hiện đại để đảm bảo độ chuẩn xác về thông số kỹ thuật, tại Vũ Gia giá trị cốt lõi vẫn nằm ở đôi bàn tay con người. Chúng tôi kiên trì giữ vững phương thức thủ công truyền thống trong các khâu quan trọng.'
                ),
                JSON_OBJECT(
                    'type',
                    'gallery',
                    'images',
                    JSON_ARRAY(
                        'assets/images/nha-xuong/slider-image-1.png',
                        'assets/images/nha-xuong/slider-image-2.png',
                        'assets/images/nha-xuong/slider-image-3.png',
                        'assets/images/nha-xuong/slider-image-4.png',
                        'assets/images/product-detail/product-detail-thumbnail.png'
                    )
                )
            )
        ),
        'Nghệ thuật chế tác',
        'Quy mô ấn tượng: 5000m² - 3 tầng vận hành chuyên biệt',
        NULL,
        'assets/images/about/hero-bg.jpg',
        'PUBLISHED',
        'Nhà xưởng',
        'Khám phá nhà xưởng và năng lực sản xuất của Gốm Sứ Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'contact',
        'Liên hệ',
        JSON_OBJECT(
            'company',
            'Gốm Vũ Gia - Thanh Hai Co., LTD',
            'label',
            'TRỤ SỞ CHÍNH & SHOWROOM 1',
            'address',
            'Số 18 Giang Cao, Bát Tràng, Gia Lâm, Hà Nội',
            'hotline',
            '091 7777 247',
            'email',
            'gomvugia@gmail.com',
            'image',
            'assets/images/about/hero-bg.jpg'
        ),
        'Liên hệ',
        NULL,
        'Liên hệ Gốm Sứ Vũ Gia để được tư vấn sản phẩm và dịch vụ.',
        'assets/images/about/hero-bg.jpg',
        'PUBLISHED',
        'Liên hệ',
        'Liên hệ Gốm Sứ Vũ Gia để được tư vấn sản phẩm và dịch vụ.',
        NOW(),
        NOW()
    ),
    (
        'privacy-policy',
        'Chính sách bảo mật thông tin',
        JSON_OBJECT(
            'sections',
            JSON_ARRAY(
                JSON_OBJECT(
                    'heading',
                    'Giới thiệu',
                    'text',
                    'Gốm Vũ Gia cam kết tôn trọng và bảo vệ quyền riêng tư của khách hàng. Chính sách này mô tả cách chúng tôi thu thập, sử dụng và bảo vệ thông tin cá nhân mà bạn cung cấp khi truy cập website hoặc sử dụng dịch vụ của chúng tôi.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Mục đích thu thập thông tin cá nhân',
                    'text',
                    'Xử lý đơn hàng, tư vấn chuyên sâu, cải thiện dịch vụ, tiếp thị và truyền thông (chỉ khi có sự đồng ý của bạn).'
                ),
                JSON_OBJECT(
                    'heading',
                    'Phạm vi sử dụng thông tin',
                    'text',
                    'Họ và tên, số điện thoại, email; địa chỉ giao hàng; nội dung tư vấn/yêu cầu thiết kế riêng; thông tin thanh toán (không bao gồm thông tin thẻ tín dụng trực tiếp trên hệ thống).'
                ),
                JSON_OBJECT(
                    'heading',
                    'Thời gian lưu trữ thông tin',
                    'text',
                    'Thông tin cá nhân được lưu trữ cho đến khi có yêu cầu hủy bỏ từ khách hàng hoặc khi không còn cần thiết. Thông tin luôn được bảo mật trên máy chủ của Gốm Vũ Gia.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Chia sẻ thông tin với bên thứ ba',
                    'text',
                    'Cam kết không bán, cho thuê hoặc chia sẻ thông tin cá nhân, ngoại trừ đơn vị vận chuyển và theo yêu cầu của cơ quan chức năng có thẩm quyền.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Cam kết bảo mật thông tin',
                    'text',
                    'Sử dụng giao thức mã hóa SSL, hệ thống tường lửa và kiểm soát truy cập nghiêm ngặt để bảo vệ thông tin cá nhân của bạn.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Quyền của khách hàng',
                    'text',
                    'Khách hàng có quyền kiểm tra, cập nhật, điều chỉnh, dừng sử dụng cho mục đích quảng cáo hoặc yêu cầu xóa bỏ hoàn toàn dữ liệu cá nhân.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Thông tin liên hệ',
                    'text',
                    'Thanh Hai Co.,Ltd - Số 18 Phố Gốm, Giang Cao, Bát Tràng, Hà Nội. Hotline: 091 7777 247. Email: gomvugia@gmail.com. Website: https://gomvugia.vn'
                )
            )
        ),
        NULL,
        NULL,
        NULL,
        NULL,
        'PUBLISHED',
        'Bảo mật thông tin',
        'Chính sách bảo mật thông tin khách hàng của Gốm Sứ Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'shipping-policy',
        'Chính sách vận chuyển - đóng gói - kiểm hàng',
        JSON_OBJECT(
            'sections',
            JSON_ARRAY(
                JSON_OBJECT(
                    'heading',
                    'Giới thiệu',
                    'text',
                    'Tại Gốm Vũ Gia, chúng tôi hiểu rằng hàng gốm sứ rất dễ vỡ. Vì vậy, quy trình vận chuyển được thiết lập chuyên nghiệp để đảm bảo sản phẩm đến tay khách hàng an toàn với chi phí tối ưu nhất.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Phí vận chuyển & thời gian giao hàng',
                    'text',
                    'Nội thành Hà Nội (ship ghép từ 70.000đ, trong/sau 1 ngày; ship riêng theo km, giao ngay). Tỉnh miền Bắc (xe tải chuyên dụng hoặc xe khách, 1-2 ngày). Miền Trung & Nam (xe tải ghép Bắc-Nam 3-7 ngày, xe khách 2-4 ngày).'
                ),
                JSON_OBJECT(
                    'heading',
                    'Quy định đóng gói & bảo hiểm nứt vỡ',
                    'text',
                    'Đóng thùng carton cứng hoặc đóng đai chắc chắn; tùy chọn đóng kiện gỗ 50.000đ-300.000đ. Vũ Gia cam kết đền bù 100% giá trị sản phẩm nếu nứt vỡ do lỗi vận chuyển. Vui lòng quay video quá trình mở hộp để làm căn cứ.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Chính sách kiểm hàng',
                    'text',
                    'Khách hàng có quyền mở thùng kiểm tra, đối chiếu mẫu mã, xác nhận tình trạng trước khi thanh toán. Nếu có vấn đề, liên hệ ngay hotline để được xử lý.'
                ),
                JSON_OBJECT(
                    'heading',
                    'Xử lý khi hàng có sự cố (nứt/vỡ)',
                    'text',
                    'Bước 1: Quay phim/chụp ảnh hiện trạng. Bước 2: Liên hệ Hotline/Zalo trong vòng 24h. Bước 3: Xác nhận mã và số lượng bị vỡ. Vũ Gia gửi bù sản phẩm mới 100% miễn phí hoặc hoàn tiền.'
                )
            )
        ),
        NULL,
        NULL,
        NULL,
        NULL,
        'PUBLISHED',
        'Chính sách vận chuyển',
        'Thông tin vận chuyển và giao nhận đơn hàng tại Gốm Sứ Vũ Gia.',
        NOW(),
        NOW()
    ),
    (
        'return-policy',
        'Chính sách đổi trả',
        JSON_OBJECT(
            'sections',
            JSON_ARRAY(
                JSON_OBJECT(
                    'type',
                    'image',
                    'image',
                    'assets/images/customer-services/chinh-sach-doi-tra.png',
                    'device',
                    'desktop'
                ),
                JSON_OBJECT(
                    'type',
                    'image',
                    'image',
                    'assets/images/customer-services/chinh-sach-doi-tra-mobile.png',
                    'device',
                    'mobile'
                ),
                JSON_OBJECT(
                    'type',
                    'image',
                    'image',
                    'assets/images/customer-services/chinh-sach-doi-tra-last.png',
                    'device',
                    'mobile'
                )
            )
        ),
        NULL,
        NULL,
        NULL,
        NULL,
        'PUBLISHED',
        'Chính sách đổi trả',
        'Chính sách đổi trả sản phẩm của Gốm Sứ Vũ Gia.',
        NOW(),
        NOW()
    );

-- ---------------------------------------------------------------------
-- COUPONS  (không có trong FE — mẫu để test tính năng mã giảm giá)
-- ---------------------------------------------------------------------
INSERT INTO
    `coupons` (
        `code`,
        `description`,
        `discount_type`,
        `discount_value`,
        `min_order_amount`,
        `max_discount_amount`,
        `usage_limit`,
        `usage_limit_per_user`,
        `used_count`,
        `starts_at`,
        `ends_at`,
        `is_active`,
        `created_at`,
        `updated_at`
    )
VALUES
    (
        'WELCOME10',
        'Giảm 10% cho đơn hàng đầu tiên',
        'PERCENT',
        10,
        500000,
        200000,
        1000,
        1,
        0,
        NOW(),
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'GIAM50K',
        'Giảm 50.000đ cho đơn từ 1.000.000đ',
        'FIXED',
        50000,
        1000000,
        NULL,
        NULL,
        NULL,
        0,
        NOW(),
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'FREESHIP',
        'Miễn phí vận chuyển nội thành Hà Nội',
        'FREE_SHIP',
        0,
        300000,
        NULL,
        NULL,
        NULL,
        0,
        NOW(),
        NULL,
        TRUE,
        NOW(),
        NOW()
    );

-- Đồng bộ AUTO_INCREMENT sau khi chèn id thủ công
ALTER TABLE
    `users` AUTO_INCREMENT = 3;

ALTER TABLE
    `product_categories` AUTO_INCREMENT = 6;

ALTER TABLE
    `products` AUTO_INCREMENT = 13;

ALTER TABLE
    `news_categories` AUTO_INCREMENT = 4;

ALTER TABLE
    `news` AUTO_INCREMENT = 23;