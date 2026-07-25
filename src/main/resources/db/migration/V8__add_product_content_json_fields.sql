-- ----------------------------
-- Thêm 3 field JSON cho nội dung động của trang chi tiết sản phẩm:
-- - detail_sections: chỉ dùng khi type=SINGLE, thay thế 3 section "Chi tiết
--   sản phẩm" đang hardcode ở FE: [{title, description, image}]
-- - functions: chỉ dùng khi type=COMBO, thay bảng "Công năng sản phẩm" đang
--   hardcode ở FE: [{name, quantity, unit, usage}]
-- - combo_gallery: chỉ dùng khi type=COMBO, ảnh slider full-width riêng biệt
--   với gallery chính (product_images): [{url}]
-- Đồng thời combo_products đổi shape (không đổi cột, chỉ đổi payload JSON):
-- cũ [{productId, sortOrder}] -> mới [{productId, quantity, sortOrder}].
-- ----------------------------

ALTER TABLE `products`
    ADD COLUMN `detail_sections` JSON NULL COMMENT 'Chỉ dùng khi type=SINGLE: [{title, description, image}]' AFTER `description`,
    ADD COLUMN `functions` JSON NULL COMMENT 'Chỉ dùng khi type=COMBO: [{name, quantity, unit, usage}]' AFTER `combo_products`,
    ADD COLUMN `combo_gallery` JSON NULL COMMENT 'Chỉ dùng khi type=COMBO: [{url}] - slider ảnh full-width' AFTER `functions`,
    MODIFY COLUMN `combo_products` JSON NULL COMMENT 'Chỉ dùng khi type=COMBO: [{productId, quantity, sortOrder}], thay cho bảng combo_items';
