-- ----------------------------
-- Table structure for shipping_methods
-- Configurable shipping methods (name + fee) offered at checkout.
-- ----------------------------
CREATE TABLE `shipping_methods` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `fee` BIGINT NOT NULL DEFAULT 0 COMMENT 'Phí vận chuyển (VND)',
    `sort_order` INT NOT NULL DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_shipping_methods_is_active` (`is_active`),
    INDEX `idx_shipping_methods_sort_order` (`sort_order`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Fold shipping fee into order placement. Existing orders keep shipping_fee=0
-- (no backfill needed — shipping is a new/optional checkout concept).
-- ----------------------------
ALTER TABLE `orders`
    ADD COLUMN `shipping_method_id` BIGINT NULL AFTER `discount_amount`,
    ADD COLUMN `shipping_fee` BIGINT NOT NULL DEFAULT 0 COMMENT 'Phí vận chuyển đã áp dụng (VND)' AFTER `shipping_method_id`;

ALTER TABLE `orders`
    ADD CONSTRAINT `fk_orders_shipping_method` FOREIGN KEY (`shipping_method_id`)
        REFERENCES `shipping_methods` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

CREATE INDEX `idx_orders_shipping_method_id` ON `orders` (`shipping_method_id`);
