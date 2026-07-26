-- =====================================================================
-- V1__init_db.sql
-- Khai báo toàn bộ cấu trúc CSDL Gốm Sứ Vũ Gia (19 bảng cốt lõi)
-- =====================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. users
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

-- 2. pages
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
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    `seo_title` VARCHAR(255) NULL,
    `seo_description` VARCHAR(500) NULL,
    `seo_image` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_pages_key` (`key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 3. contact_requests
DROP TABLE IF EXISTS `contact_requests`;
CREATE TABLE `contact_requests` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NULL,
    `email` VARCHAR(255) NULL,
    `phone` VARCHAR(20) NULL,
    `content` LONGTEXT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'NEW',
    `handled_by_id` BIGINT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_contact_requests_status` (`status`),
    INDEX `idx_contact_requests_handled_by_id` (`handled_by_id`),
    INDEX `idx_contact_requests_created_at` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 4. news_categories
DROP TABLE IF EXISTS `news_categories`;
CREATE TABLE `news_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `priority` INT DEFAULT 0,
    `seo_title` VARCHAR(255) NULL,
    `seo_description` VARCHAR(500) NULL,
    `seo_image` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_news_categories_priority` (`priority`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 5. news
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
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
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

-- 6. product_categories
DROP TABLE IF EXISTS `product_categories`;
CREATE TABLE `product_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `category_type` VARCHAR(30) NOT NULL UNIQUE,
    `name` VARCHAR(50) NOT NULL,
    `thumb` VARCHAR(255) NOT NULL,
    `priority` INT DEFAULT 0,
    `short_description` TEXT NULL,
    `detail_content` JSON NULL,
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `seo_title` VARCHAR(255) NULL,
    `seo_description` VARCHAR(500) NULL,
    `seo_image` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_product_categories_priority` (`priority`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 7. products
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
    `detail_sections` JSON NULL COMMENT 'Chỉ dùng khi type=SINGLE: [{title, description, image}]',
    `combo_products` JSON NULL COMMENT 'Chỉ dùng khi type=COMBO: [{productId, quantity, sortOrder}]',
    `functions` JSON NULL COMMENT 'Chỉ dùng khi type=COMBO: [{name, quantity, unit, usage}]',
    `combo_gallery` JSON NULL COMMENT 'Chỉ dùng khi type=COMBO: [{url}] - slider ảnh full-width',
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

-- 8. product_images
DROP TABLE IF EXISTS `product_images`;
CREATE TABLE `product_images` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `url` VARCHAR(255) NOT NULL,
    `priority` INT DEFAULT 0,
    `product_id` BIGINT NOT NULL,
    INDEX `idx_product_images_product_id` (`product_id`),
    INDEX `idx_product_images_priority` (`priority`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 9. cart_items
DROP TABLE IF EXISTS `cart_items`;
CREATE TABLE `cart_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `combo_items` JSON NULL COMMENT 'Chỉ dùng khi product là COMBO: [{productId, quantity}]',
    INDEX `idx_cart_items_user_id` (`user_id`),
    INDEX `idx_cart_items_product_id` (`product_id`),
    UNIQUE INDEX `uidx_cart_items_user_product` (`user_id`, `product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 10. coupons
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(500) NULL,
    `discount_type` ENUM('PERCENT', 'FIXED', 'FREE_SHIP') NOT NULL,
    `discount_value` BIGINT NOT NULL,
    `min_order_amount` BIGINT NULL,
    `max_discount_amount` BIGINT NULL,
    `usage_limit` INT NULL,
    `usage_limit_per_user` INT NULL,
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

-- 11. orders
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
    `payment_method` ENUM('COD', 'BANK_TRANSFER') NOT NULL DEFAULT 'COD',
    `total_amount` BIGINT NOT NULL,
    `coupon_id` BIGINT NULL,
    `coupon_code` VARCHAR(50) NULL,
    `discount_amount` BIGINT NOT NULL DEFAULT 0,
    `shipping_method_id` BIGINT NULL,
    `shipping_fee` BIGINT NOT NULL DEFAULT 0,
    `receiver_name` VARCHAR(100) NULL,
    `receiver_phone` VARCHAR(20) NULL,
    `receiver_address` TEXT NULL,
    `note` TEXT NULL,
    `idempotency_key` VARCHAR(100) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_orders_user_id` (`user_id`),
    INDEX `idx_orders_status` (`status`),
    INDEX `idx_orders_payment_status` (`payment_status`),
    INDEX `idx_orders_order_code` (`order_code`),
    INDEX `idx_orders_coupon_id` (`coupon_id`),
    INDEX `idx_orders_shipping_method_id` (`shipping_method_id`),
    INDEX `idx_orders_user_status` (`user_id`, `status`),
    INDEX `idx_orders_user_id_id` (`user_id`, `id`),
    INDEX `idx_orders_coupon_user` (`coupon_id`, `user_id`),
    UNIQUE INDEX `uidx_orders_user_idempotency` (`user_id`, `idempotency_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 12. order_items
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
    `combo_items` JSON NULL,
    INDEX `idx_order_items_order_id` (`order_id`),
    INDEX `idx_order_items_product_id` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 13. payment_transactions
DROP TABLE IF EXISTS `payment_transactions`;
CREATE TABLE `payment_transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `sepay_id` BIGINT NOT NULL UNIQUE,
    `order_code` VARCHAR(50) NULL,
    `gateway` VARCHAR(100) NULL,
    `amount` BIGINT NOT NULL,
    `transfer_type` VARCHAR(10) NULL,
    `reference_code` VARCHAR(100) NULL,
    `transaction_date` VARCHAR(30) NULL,
    `content` TEXT NULL,
    `matched` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL,
    INDEX `idx_payment_transactions_sepay_id` (`sepay_id`),
    INDEX `idx_payment_transactions_order_code` (`order_code`),
    INDEX `idx_payment_transactions_matched` (`matched`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 14. refresh_tokens
DROP TABLE IF EXISTS `refresh_tokens`;
CREATE TABLE `refresh_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `expires_at` DATETIME NOT NULL,
    `revoked` BOOLEAN NOT NULL,
    `token` VARCHAR(100) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    INDEX `idx_refresh_tokens_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 15. banners
DROP TABLE IF EXISTS `banners`;
CREATE TABLE `banners` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NULL,
    `image_url` VARCHAR(255) NOT NULL,
    `link_url` VARCHAR(255) NULL,
    `position` VARCHAR(30) NOT NULL,
    `sort_order` INT DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `starts_at` DATETIME NULL,
    `ends_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_banners_position` (`position`),
    INDEX `idx_banners_is_active` (`is_active`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 16. showrooms
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
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_showrooms_is_active` (`is_active`),
    INDEX `idx_showrooms_sort_order` (`sort_order`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 17. shipping_methods
DROP TABLE IF EXISTS `shipping_methods`;
CREATE TABLE `shipping_methods` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(500) NULL,
    `fee` BIGINT NOT NULL DEFAULT 0,
    `estimated_delivery` VARCHAR(100) NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_shipping_methods_code` (`code`),
    INDEX `idx_shipping_methods_is_active` (`is_active`),
    INDEX `idx_shipping_methods_sort_order` (`sort_order`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 18. gallery_images
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
    INDEX `idx_gallery_images_is_active` (`is_active`),
    INDEX `idx_gallery_images_sort_order` (`sort_order`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 19. faqs
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
    INDEX `idx_faqs_is_active` (`is_active`),
    INDEX `idx_faqs_sort_order` (`sort_order`),
    INDEX `idx_faqs_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 20. redirects
DROP TABLE IF EXISTS `redirects`;
CREATE TABLE `redirects` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `from_path` VARCHAR(500) NOT NULL UNIQUE,
    `to_path` VARCHAR(500) NOT NULL,
    `status_code` INT NOT NULL DEFAULT 301,
    `hit_count` INT NOT NULL DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_redirects_from_path` (`from_path`),
    INDEX `idx_redirects_is_active` (`is_active`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 21. newsletter_subscribers
DROP TABLE IF EXISTS `newsletter_subscribers`;
CREATE TABLE `newsletter_subscribers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    INDEX `idx_newsletter_subscribers_email` (`email`),
    INDEX `idx_newsletter_subscribers_is_active` (`is_active`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;