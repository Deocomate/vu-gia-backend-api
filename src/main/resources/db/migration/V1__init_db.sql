-- Thiết lập mã hóa hỗ trợ tiếng Việt và các ký tự đặc biệt
SET
    NAMES utf8mb4;

SET
    FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NULL COMMENT 'Null nếu user chỉ đăng nhập qua Google OAuth',
    `name` VARCHAR(50) NULL,
    `phone` VARCHAR(10) NULL,
    `email` VARCHAR(50) NOT NULL UNIQUE,
    `gender` VARCHAR(10) NULL,
    `dob` DATE NULL,
    `avatar` VARCHAR(255) NULL,
    `provider` VARCHAR(20) NULL COMMENT 'null = local; vd: GOOGLE',
    `provider_id` VARCHAR(100) NULL COMMENT 'sub/id trả về từ provider OAuth',
    `role` ENUM('SUPERADMIN', 'ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_users_phone` (`phone`),
    INDEX `idx_users_role` (`role`),
    INDEX `idx_users_name` (`name`),
    INDEX `idx_users_created_at` (`created_at`),
    UNIQUE INDEX `uidx_users_provider_provider_id` (`provider`, `provider_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for pages
-- ----------------------------
DROP TABLE IF EXISTS `pages`;

CREATE TABLE `pages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `key` VARCHAR(255) NOT NULL UNIQUE,
    `title` VARCHAR(255) NULL,
    `content` JSON NULL,
    `hero_title` VARCHAR(255) NULL,
    `hero_subtitle` VARCHAR(255) NULL,
    `hero_des` TEXT NULL,
    `hero_image` VARCHAR(255) NULL,
    `status` ENUM('DRAFT', 'PUBLISHED') NOT NULL DEFAULT 'DRAFT',
    `seo_title` VARCHAR(255) NULL,
    `seo_description` VARCHAR(500) NULL,
    `seo_image` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_pages_key` (`key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for contact_requests
-- ----------------------------
DROP TABLE IF EXISTS `contact_requests`;

CREATE TABLE `contact_requests` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NULL,
    `email` VARCHAR(255) NULL,
    `phone` VARCHAR(20) NULL,
    `content` LONGTEXT NULL,
    `status` ENUM('NEW', 'HANDLED', 'CLOSED') NOT NULL DEFAULT 'NEW',
    `handled_by_id` BIGINT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_contact_requests_status` (`status`),
    INDEX `idx_contact_requests_handled_by_id` (`handled_by_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for newsletter_subscribers
-- ----------------------------
DROP TABLE IF EXISTS `newsletter_subscribers`;

CREATE TABLE `newsletter_subscribers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for news_categories
-- ----------------------------
DROP TABLE IF EXISTS `news_categories`;

CREATE TABLE `news_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `priority` INT DEFAULT 0,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_news_categories_priority` (`priority`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for news
-- ----------------------------
DROP TABLE IF EXISTS `news`;

CREATE TABLE `news` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` TEXT NOT NULL,
    `thumb` VARCHAR(255) NOT NULL,
    `short_content` TEXT NOT NULL,
    `des` JSON NOT NULL,
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `priority` INT DEFAULT 0,
    `view_count` INT NOT NULL DEFAULT 0,
    `status` ENUM('DRAFT', 'PUBLISHED') NOT NULL DEFAULT 'DRAFT',
    `published_at` DATETIME NULL,
    `news_category_id` BIGINT NOT NULL,
    `seo_title` VARCHAR(255) NULL,
    `seo_description` VARCHAR(500) NULL,
    `seo_image` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_news_news_category_id` (`news_category_id`),
    INDEX `idx_news_priority` (`priority`),
    INDEX `idx_news_status` (`status`),
    INDEX `idx_news_published_at` (`published_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for product_categories
-- ----------------------------
DROP TABLE IF EXISTS `product_categories`;

CREATE TABLE `product_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `thumb` VARCHAR(255) NOT NULL,
    `priority` INT DEFAULT 0,
    `long_content` TEXT NULL,
    `des` JSON NULL,
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `seo_title` VARCHAR(255) NULL,
    `seo_description` VARCHAR(500) NULL,
    `seo_image` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_product_categories_priority` (`priority`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;

CREATE TABLE `products` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `thumb` VARCHAR(255) NOT NULL,
    `sku` VARCHAR(100) UNIQUE,
    `type` ENUM('SINGLE', 'COMBO') NOT NULL,
    `price` BIGINT NOT NULL COMMENT 'Giá bán hiện tại',
    `compare_at_price` BIGINT NULL COMMENT 'Giá gốc trước giảm, hiển thị gạch ngang; null nếu không giảm giá',
    `sold_count` INT NOT NULL DEFAULT 0,
    `is_featured` BOOLEAN NOT NULL DEFAULT FALSE,
    `status` ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    `description` JSON NULL,
    `combo_products` JSON NULL COMMENT 'Chỉ dùng khi type=COMBO: [{productId, sortOrder}], thay cho bảng combo_items',
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `priority` INT DEFAULT 0,
    `product_category_id` BIGINT NOT NULL,
    `seo_title` VARCHAR(255) NULL,
    `seo_description` VARCHAR(500) NULL,
    `seo_image` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_products_sku` (`sku`),
    INDEX `idx_products_slug` (`slug`),
    INDEX `idx_products_type` (`type`),
    INDEX `idx_products_priority` (`priority`),
    INDEX `idx_products_product_category_id` (`product_category_id`),
    INDEX `idx_products_is_featured` (`is_featured`),
    INDEX `idx_products_type_priority` (`type`, `priority`),
    INDEX `idx_products_status_is_featured` (`status`, `is_featured`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for product_images
-- ----------------------------
DROP TABLE IF EXISTS `product_images`;

CREATE TABLE `product_images` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `url` VARCHAR(255) NOT NULL,
    `priority` INT DEFAULT 0,
    `product_id` BIGINT NOT NULL,
    INDEX `idx_product_images_product_id` (`product_id`),
    INDEX `idx_product_images_priority` (`priority`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for cart_items
-- ----------------------------
DROP TABLE IF EXISTS `cart_items`;

CREATE TABLE `cart_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `combo_items` JSON NULL COMMENT 'Chỉ dùng khi product là COMBO: [{productId, quantity}], thay cho cart_combo_items',
    INDEX `idx_cart_items_user_id` (`user_id`),
    INDEX `idx_cart_items_product_id` (`product_id`),
    UNIQUE INDEX `uidx_cart_items_user_product` (`user_id`, `product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for coupons
-- ----------------------------
DROP TABLE IF EXISTS `coupons`;

CREATE TABLE `coupons` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(500) NULL,
    `discount_type` ENUM('PERCENT', 'FIXED', 'FREE_SHIP') NOT NULL,
    `discount_value` BIGINT NOT NULL,
    `min_order_amount` BIGINT NULL,
    `max_discount_amount` BIGINT NULL,
    `usage_limit` INT NULL COMMENT 'null = không giới hạn tổng lượt dùng',
    `usage_limit_per_user` INT NULL COMMENT 'null = không giới hạn theo user; kiểm tra bằng cách đếm orders.coupon_id',
    `used_count` INT NOT NULL DEFAULT 0,
    `starts_at` DATETIME NULL,
    `ends_at` DATETIME NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_coupons_code` (`code`),
    INDEX `idx_coupons_is_active` (`is_active`),
    INDEX `idx_coupons_created_at` (`created_at`),
    INDEX `idx_coupons_ends_at` (`ends_at`),
    INDEX `idx_coupons_starts_at` (`starts_at`),
    INDEX `idx_coupons_used_count` (`used_count`),
    INDEX `idx_coupons_discount_value` (`discount_value`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;

CREATE TABLE `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `order_code` VARCHAR(50) NOT NULL UNIQUE,
    `status` ENUM(
        'PENDING_PAYMENT',
        'PROCESSING',
        'SHIPPING',
        'COMPLETED',
        'CANCELLED',
        'RETURNED'
    ) NOT NULL,
    `payment_status` ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    `total_amount` BIGINT NOT NULL,
    `coupon_id` BIGINT NULL,
    `coupon_code` VARCHAR(50) NULL COMMENT 'Snapshot mã đã dùng, giữ lại kể cả khi coupon bị xoá/sửa',
    `discount_amount` BIGINT NOT NULL DEFAULT 0,
    `receiver_name` VARCHAR(100) NULL,
    `receiver_phone` VARCHAR(20) NULL,
    `receiver_address` TEXT NULL,
    `note` TEXT NULL,
    `idempotency_key` VARCHAR(100) NULL COMMENT 'Client idempotency token; unique per user to dedupe checkout',
    `created_at` DATETIME NOT NULL COMMENT 'Dùng làm "Ngày đặt hàng" (placedAt)',
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_orders_user_id` (`user_id`),
    INDEX `idx_orders_status` (`status`),
    INDEX `idx_orders_payment_status` (`payment_status`),
    INDEX `idx_orders_order_code` (`order_code`),
    INDEX `idx_orders_coupon_id` (`coupon_id`),
    INDEX `idx_orders_user_status` (`user_id`, `status`),
    INDEX `idx_orders_user_id_id` (`user_id`, `id`),
    INDEX `idx_orders_coupon_user` (`coupon_id`, `user_id`),
    UNIQUE INDEX `uidx_orders_user_idempotency` (`user_id`, `idempotency_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;

CREATE TABLE `order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    `product_type` ENUM('SINGLE', 'COMBO') NOT NULL,
    `unit_price` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `subtotal` BIGINT NOT NULL,
    `combo_items` JSON NULL COMMENT 'Snapshot sub-item nếu là COMBO: [{productId, name, quantity, unitPrice, subtotal}]',
    INDEX `idx_order_items_order_id` (`order_id`),
    INDEX `idx_order_items_product_id` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for refresh_tokens
-- ----------------------------
DROP TABLE IF EXISTS `refresh_tokens`;

CREATE TABLE `refresh_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `expires_at` DATETIME NOT NULL,
    `revoked` BOOLEAN NOT NULL,
    `token` VARCHAR(100) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    INDEX `idx_refresh_tokens_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for banners
-- ----------------------------
DROP TABLE IF EXISTS `banners`;

CREATE TABLE `banners` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NULL,
    `image_url` VARCHAR(255) NOT NULL,
    `link_url` VARCHAR(255) NULL,
    `position` ENUM('HOME_HERO', 'HOME_CATEGORY', 'HOME_PROMO') NOT NULL,
    `sort_order` INT DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `starts_at` DATETIME NULL,
    `ends_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_banners_position` (`position`),
    INDEX `idx_banners_is_active` (`is_active`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for showrooms
-- ----------------------------
DROP TABLE IF EXISTS `showrooms`;

CREATE TABLE `showrooms` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20) NULL,
    `address` TEXT NOT NULL,
    `map_embed_url` VARCHAR(500) NULL,
    `opening_hours` VARCHAR(255) NULL,
    `sort_order` INT DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for gallery_images
-- ----------------------------
DROP TABLE IF EXISTS `gallery_images`;

CREATE TABLE `gallery_images` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `image_url` VARCHAR(255) NOT NULL,
    `title` VARCHAR(255) NULL,
    `category` VARCHAR(100) NULL,
    `sort_order` INT DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_gallery_images_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for faqs
-- ----------------------------
DROP TABLE IF EXISTS `faqs`;

CREATE TABLE `faqs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question` VARCHAR(500) NOT NULL,
    `answer` TEXT NOT NULL,
    `category` VARCHAR(100) NULL,
    `sort_order` INT DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_faqs_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for redirects
-- ----------------------------
DROP TABLE IF EXISTS `redirects`;

CREATE TABLE `redirects` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `from_path` VARCHAR(500) NOT NULL UNIQUE,
    `to_path` VARCHAR(500) NOT NULL,
    `status_code` INT NOT NULL DEFAULT 301,
    `hit_count` INT NOT NULL DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Foreign Keys (Ràng buộc khóa ngoại)
-- ----------------------------
SET
    FOREIGN_KEY_CHECKS = 1;

-- Ref: contact_requests.handled_by_id > users.id
ALTER TABLE
    `contact_requests`
ADD
    CONSTRAINT `fk_contact_requests_handled_by` FOREIGN KEY (`handled_by_id`) REFERENCES `users` (`id`) ON DELETE
SET
    NULL ON UPDATE CASCADE;

-- Ref: news.news_category_id > news_categories.id
ALTER TABLE
    `news`
ADD
    CONSTRAINT `fk_news_news_category` FOREIGN KEY (`news_category_id`) REFERENCES `news_categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- Ref: products.product_category_id > product_categories.id
ALTER TABLE
    `products`
ADD
    CONSTRAINT `fk_products_category` FOREIGN KEY (`product_category_id`) REFERENCES `product_categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- Ref: product_images.product_id > products.id
ALTER TABLE
    `product_images`
ADD
    CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Ref: cart_items.user_id > users.id
-- Ref: cart_items.product_id > products.id
ALTER TABLE
    `cart_items`
ADD
    CONSTRAINT `fk_cart_items_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
ADD
    CONSTRAINT `fk_cart_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Ref: orders.user_id > users.id
-- Ref: orders.coupon_id > coupons.id
ALTER TABLE
    `orders`
ADD
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
ADD
    CONSTRAINT `fk_orders_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`) ON DELETE
SET
    NULL ON UPDATE CASCADE;

-- Ref: order_items.order_id > orders.id
-- Ref: order_items.product_id > products.id
ALTER TABLE
    `order_items`
ADD
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
ADD
    CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- Ref: refresh_tokens.user_id > users.id
ALTER TABLE
    `refresh_tokens`
ADD
    CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;