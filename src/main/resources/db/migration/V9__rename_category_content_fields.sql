-- ----------------------------
-- Đổi tên 2 cột nội dung của product_categories cho rõ nghĩa:
-- - long_content -> short_description (mô tả ngắn, hiển thị dưới listing)
-- - des -> detail_content (nội dung chi tiết dạng block JSON)
-- Giữ nguyên type/data, chỉ đổi tên cột.
-- ----------------------------

ALTER TABLE `product_categories` RENAME COLUMN `long_content` TO `short_description`;
ALTER TABLE `product_categories` RENAME COLUMN `des` TO `detail_content`;
