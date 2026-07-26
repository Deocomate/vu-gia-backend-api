# 🗺 Gốm Sứ Vũ Gia — Project Roadmap & Feature Status

> **Lộ trình phát triển dự án**: Theo dõi tiến độ hoàn thành các tính năng hiện tại và kế hoạch nâng cấp cho các phiên bản tiếp theo của dự án `vu-gia-backend-api`.

---

## 1. Trạng thái Hiện tại (Current Version: v1.0.0)

Phiên bản **v1.0.0** đã hoàn thành 100% các tính năng thương mại cốt lõi, sẵn sàng đưa vào vận hành thực tế (Production-Ready).

### Danh sách các Module đã hoàn thiện:

- [x] **Hạ tầng Nền tảng & Bảo mật**:
  - [x] Tích hợp Spring Security 6 với JWT Access Token + Refresh Token Rotation.
  - [x] Đăng nhập Google OAuth2 (xác thực Google ID Token).
  - [x] Hệ thống Phân quyền Role-based (`SUPERADMIN`, `ADMIN`, `CUSTOMER`).
  - [x] Envelope Response thống nhất (`ApiResponse<T>`) và Xử lý lỗi tập trung (`GlobalExceptionHandler`).
  - [x] Schema code-first qua JPA entity (`ddl-auto=update`) + seed dữ liệu qua `SeedRunner`/`DomainSeeder` (Java).
  - [x] Quản lý lưu trữ tệp tin local đĩa cứng với Annotation `@StorageUrl`.

- [x] **Quản lý Sản phẩm (Product Catalog)**:
  - [x] Quản lý Danh mục sản phẩm (hỗ trợ SEO Slug).
  - [x] Quản lý Sản phẩm (SKU, giá, giá khuyến mãi, tồn kho, trạng thái nổi bật, lượt bán `sold_count`).
  - [x] Bộ sưu tập ảnh sản phẩm.
  - [x] Tra cứu & Tìm kiếm sản phẩm phân trang an toàn (Sort Whitelist + Dynamic JPA Specification).

- [x] **Nghiệp vụ Mua hàng & Đơn hàng**:
  - [x] Giỏ hàng theo người dùng.
  - [x] Đặt hàng an toàn Idempotent, trừ kho và tính toán tổng tiền snapshot.
  - [x] Quản lý Mã giảm giá (Coupon) nguyên tử race-free (`PERCENT`, `FIXED`, `FREE_SHIP`).
  - [x] Tích hợp Phương thức vận chuyển.
  - [x] Thanh toán VietQR sinh mã QR tự động.
  - [x] Xử lý Webhook SePay xác thực chữ ký tự động duyệt đơn.
  - [x] Gửi Email HTML bất đồng bộ thông báo đơn hàng thành công qua Thymeleaf.

- [x] **CMS Nội dung & Quản trị**:
  - [x] Module Tin tức & Danh mục tin tức theo Slug.
  - [x] Module Trang tĩnh CMS (Chính sách, Giới thiệu).
  - [x] Module Banner, Showroom cửa hàng, Bộ sưu tập Gallery, FAQ & Redirect URL.
  - [x] Module Form liên hệ & Đăng ký Newsletter.
  - [x] Dashboard Báo cáo thống kê Admin (KPI Doanh thu, số lượng đơn hàng, top sản phẩm bán chạy).

---

## 2. Kế hoạch Phát triển Tiếp theo (Future Roadmap)

### Phiên bản v1.1.0 — Tối ưu Hiệu năng & Mở rộng Kênh Thanh toán
- [ ] **Bộ nhớ đệm (Caching)**: Tích hợp Redis Caching cho danh mục sản phẩm, cấu hình trang và danh sách banner giúp tăng tốc độ phản hồi API lên dưới 20ms.
- [ ] **Tích hợp Cổng thanh toán**: Bổ sung tích hợp trực tiếp với MoMo, VNPAY, ZaloPay.
- [ ] **Tối ưu hóa Tìm kiếm**: Tích hợp Spring Data Elasticsearch cho phép tìm kiếm mờ (Fuzzy Search), gợi ý từ khóa thông minh (Auto-complete) cho sản phẩm và bài viết.

### Phiên bản v1.2.0 — Đa kho hàng & Chăm sóc Khách hàng
- [ ] **Quản lý Đa kho hàng**: Hỗ trợ nhiều chi nhánh/showroom với số lượng tồn kho tách biệt cho từng vị trí.
- [ ] **Đánh giá & Bình luận (Product Reviews)**: Cho phép khách hàng đã mua sản phẩm gửi đánh giá 5 sao, kèm hình ảnh thực tế và nhận xét.
- [ ] **Hệ thống Tích điểm & Hạng thành viên**: Tính điểm thưởng cho mỗi đơn hàng thành công, tự động nâng hạng thành viên (Vàng, Kim Cương) với các ưu đãi riêng.

### Phiên bản v2.0.0 — Kiến trúc Microservices & AI Integration
- [ ] **Chuyển đổi Kiến trúc (Microservices Modularization)**: Tách các module Nguồn hàng, Đơn hàng, CMS thành các dịch vụ độc lập nếu quy mô hệ thống tăng trưởng mạnh.
- [ ] **Gợi ý Sản phẩm bằng AI**: Sử dụng mô hình AI gợi ý sản phẩm phù hợp dựa trên lịch sử xem và hành vi mua sắm của người dùng.
