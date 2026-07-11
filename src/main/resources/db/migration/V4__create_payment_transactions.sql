-- ----------------------------
-- Log giao dịch webhook (SePay). `sepay_id` UNIQUE để chống trùng (retry/replay).
-- ----------------------------
CREATE TABLE `payment_transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `sepay_id` BIGINT NOT NULL COMMENT 'ID giao dịch trên SePay (khóa chống trùng)',
    `order_code` VARCHAR(50) NULL COMMENT 'Mã đơn khớp được (từ trường code / nội dung CK)',
    `gateway` VARCHAR(100) NULL,
    `amount` BIGINT NOT NULL,
    `transfer_type` VARCHAR(10) NULL,
    `reference_code` VARCHAR(100) NULL,
    `transaction_date` VARCHAR(30) NULL,
    `content` TEXT NULL,
    `matched` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Đã khớp & cập nhật đơn hay chưa',
    `created_at` DATETIME NOT NULL,
    UNIQUE INDEX `uidx_payment_transactions_sepay_id` (`sepay_id`),
    INDEX `idx_payment_transactions_order_code` (`order_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
