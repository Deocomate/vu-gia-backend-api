-- ----------------------------
-- Phương thức thanh toán: COD (thu tiền khi nhận) hoặc ONL (chuyển khoản VietQR).
-- Mặc định COD để các đơn hiện có không bị ảnh hưởng.
-- ----------------------------
ALTER TABLE `orders`
    ADD COLUMN `payment_method` ENUM('COD', 'ONL') NOT NULL DEFAULT 'COD'
        COMMENT 'COD = thu tiền khi nhận; ONL = chuyển khoản VietQR' AFTER `payment_status`;
