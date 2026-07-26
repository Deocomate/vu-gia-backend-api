# 🏺 Gốm Sứ Vũ Gia — Project Overview & PDR (Product Development Requirement)

> **Dự án**: Backend REST API Thương mại điện tử Gốm Sứ Vũ Gia  
> **Phiên bản**: v1.0.0  
> **Tech Stack**: Java 21, Spring Boot 3.5.10, MySQL 8, Flyway, Spring Security (JWT + OAuth2), MapStruct, Lombok

---

## 1. Tổng quan dự án (Project Overview)

**Gốm Sứ Vũ Gia** là hệ thống thương mại điện tử chuyên cung cấp các sản phẩm gốm sứ thủ công truyền thống cao cấp. Dự án Backend REST API đóng vai trò là nền tảng xử lý nghiệp vụ trung tâm cho cả hệ thống **Storefront (Khách hàng)** và **Admin Dashboard (Quản trị viên)**.

Hệ thống được thiết kế theo kiến trúc phân tầng hiện đại (Clean Layered Architecture), đảm bảo khả năng mở rộng, bảo mật cao, tuân thủ nguyên tắc SOLID và cung cấp hiệu năng tối ưu cho công tác quản lý catalogue, giỏ hàng, đơn hàng, mã giảm giá, bài viết CMS và báo cáo thống kê doanh thu.

---

## 2. Đối tượng người dùng (User Personas)

1. **Khách truy cập & Mua hàng (Customer)**:
   - Duyệt sản phẩm theo danh mục, từ khóa, mức giá, trạng thái nổi bật.
   - Quản lý giỏ hàng cá nhân.
   - Đặt hàng công khai (Guest) hoặc người dùng đã đăng nhập.
   - Áp dụng mã giảm giá (Coupon), chọn phương thức vận chuyển và thanh toán (COD / Chuyển khoản VietQR).
   - Đăng ký nhận tin tin tức (Newsletter) và gửi yêu cầu tư vấn (Contact Request).
   - Theo dõi thông tin cá nhân và lịch sử đơn hàng.

2. **Quản trị viên (Admin / Staff)**:
   - Quản lý danh mục sản phẩm, thông tin sản phẩm, cập nhật số lượng tồn kho và hình ảnh.
   - Xử lý đơn hàng (xác nhận, cập nhật trạng thái đơn hàng, theo dõi giao hàng).
   - Quản lý các chương trình khuyến mãi (Mã giảm giá/Coupon).
   - Quản trị nội dung CMS: Tin tức, Trang nội dung động, Banner, Showroom, Gallery, FAQ.
   - Theo dõi báo cáo thống kê Dashboard (Doanh thu, số lượng đơn hàng, sản phẩm bán chạy).

3. **Quản trị hệ thống cấp cao (Super Admin)**:
   - Tất cả quyền của Admin.
   - Quản lý tài khoản quản trị và phân quyền (RBAC).
   - Reset mật khẩu và thay đổi vai trò người dùng trong hệ thống.

---

## 3. Ma trận yêu cầu chức năng (Functional Requirements)

### 3.1. Xác thực & Phân quyền (Auth & User Management)
- **Đăng ký / Đăng nhập**: Hỗ trợ đăng nhập qua `username` hoặc `email` với mật khẩu mã hóa BCrypt.
- **Google OAuth2 Login**: Đăng nhập nhanh qua tài khoản Google bằng xác thực Google ID Token.
- **JWT & Refresh Token Rotation**: Sử dụng Access Token ngắn hạn (HS512) và Refresh Token lưu DB có cơ chế xoay vòng (Rotation) chống chiếm đoạt phiên.
- **Phân quyền dựa trên Vai trò (RBAC)**: Các vai trò `CUSTOMER`, `ADMIN`, `SUPERADMIN` kiểm soát bằng `@PreAuthorize`.

### 3.2. Quản lý Sản phẩm (Product Catalog)
- **Danh mục sản phẩm**: Cấu trúc phân cấp danh mục, tự động tạo slug chuẩn SEO.
- **Sản phẩm & Biến thể**: Lưu trữ thông tin chi tiết, mã sản phẩm (SKU), giá bán, giá khuyến mãi, tồn kho, lượt bán (`sold_count`), trạng thái ẩn/hiện, nổi bật (`is_featured`).
- **Hình ảnh sản phẩm**: Quản lý bộ sưu tập ảnh sản phẩm lưu trữ trên Local Filesystem (`/files/**`).
- **Tra cứu & Tìm kiếm**: Tìm kiếm theo từ khóa, lọc theo danh mục, khoảng giá, trạng thái, hỗ trợ sort an toàn và phân trang.

### 3.3. Giỏ hàng & Đặt hàng (Cart & Order Processing)
- **Giỏ hàng**: Lưu trữ item giỏ hàng theo tài khoản người dùng, hỗ trợ thêm/sửa/xóa và tính toán tổng tiền.
- **Đặt hàng Idempotent**: Đặt hàng an toàn chống trùng lặp request, snapshot giá sản phẩm tại thời điểm đặt.
- **Áp dụng Mã giảm giá**: Áp dụng mã giảm giá dạng `PERCENT`, `FIXED`, hoặc `FREE_SHIP` với các điều kiện giá trị đơn tối thiểu, số lần sử dụng.
- **Khấu trừ Tồn kho & Coupon Nguyên tử (Atomic Operations)**: Khấu trừ số lượt sử dụng coupon và cập nhật kho chống race-condition.
- **Thanh toán VietQR & SePay Webhook**: Tự động sinh mã QR thanh toán ngân hàng chuyển khoản; tiếp nhận webhook từ SePay để tự động xác nhận đơn hàng khi chuyển khoản thành công.
- **Thông báo Email HTML**: Tự động gửi email xác nhận đơn hàng bằng HTML template (Thymeleaf) bất đồng bộ qua `@Async` & `@TransactionalEventListener`.

### 3.4. Quản trị Nội dung (CMS Modules)
- **Tin tức (News)**: Bài viết tin tức theo danh mục, hỗ trợ đăng bài, tìm kiếm, lưu trữ slug SEO.
- **Trang động (Page)**: Quản lý nội dung các trang chính sách, giới thiệu, điều khoản theo `key`.
- **Banner & Slider**: Quản lý hình ảnh banner hiển thị trên trang chủ/khuyến mãi.
- **Showroom & Gallery**: Quản lý danh sách hệ thống cửa hàng/showroom và bộ sưu tập hình ảnh thực tế.
- **FAQ & Redirect**: Quản lý câu hỏi thường gặp và cấu hình chuyển hướng URL (301/302).

### 3.5. Báo cáo & Thống kê (Dashboard & Analytics)
- **Chỉ số KPI tổng quan**: Tổng doanh thu, tổng số đơn hàng, tổng số khách hàng, tổng số sản phẩm.
- **Thống kê doanh thu**: Biểu đồ doanh thu theo khoảng thời gian (ngày/tháng).
- **Sản phẩm bán chạy**: Thống kê danh sách top sản phẩm có số lượng bán ra cao nhất.

---

## 4. Yêu cầu phi chức năng (Non-Functional Requirements)

| Tiêu chí | Trạng thái / Giải pháp |
|---|---|
| **Hiệu năng (Performance)** | MapStruct ánh xạ DTO compile-time; JPA Specification tối ưu query DB; phân trang bắt buộc clamp `size <= 100`. |
| **Bảo mật (Security)** | Mật khẩu mã hóa BCrypt; JWT token HMAC-SHA512; kiểm soát quyền `@PreAuthorize`; xác thực webhook SePay qua signature HMAC; vô hiệu hóa CSRF cho Stateless API. |
| **Tính sẵn sàng & Khởi tạo** | Tự động nâng cấp Schema và Seed Data bằng Flyway (`V1__init_db.sql`, `V2__seed_db.sql`) giúp khởi chạy 1-click qua Docker Compose. |
| **Tính toàn vẹn dữ liệu** | Sử dụng `@Transactional` đúng phạm vi; xử lý race condition khi dùng mã giảm giá và kho hàng bằng truy vấn nguyên tử. |
| **Khả năng quan sát (Observability)** | Tích hợp Spring Boot Actuator cung cấp `/actuator/health` và `/actuator/info`. |

---

## 5. Danh mục Tài liệu Kỹ thuật Liên quan

- [`README.md`](../README.md) — Hướng dẫn cài đặt, cấu hình môi trường, chạy Docker & Tóm tắt nhóm REST API
- [`codebase-summary.md`](codebase-summary.md) — Bản đồ chi tiết package Java, bảng CSDL & các Flyway migration (V1–V9)
- [`code-standards.md`](code-standards.md) — Quy chuẩn lập trình, envelope response, mã lỗi `ErrorCode` & testing
- [`system-architecture.md`](system-architecture.md) — Sơ đồ kiến trúc Mermaid (Auth JWT/OAuth2, Order Flow, SePay Webhook, Storage)
- [`project-roadmap.md`](project-roadmap.md) — Lộ trình phát triển & trạng thái hoàn thiện các tính năng v1.0.0
- **Tài liệu REST API Tương tác**: Swagger UI tại URL `http://localhost:8080/swagger-ui.html` (OpenAPI 3.0 spec tại `/v3/api-docs`).

