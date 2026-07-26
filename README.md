# 🏺 Gốm Sứ Vũ Gia — Backend REST API

> **Hệ thống Backend REST API cho Nền tảng Thương mại Điện tử Gốm Sứ Vũ Gia**  
> Xây dựng trên nền **Spring Boot 3.5.10 (Java 21)**, **MySQL 8.0**, **Flyway Migration** và **Spring Security (JWT + OAuth2)**.

---

## 📋 1. Tổng quan Dự án

`vu-gia-backend-api` là dịch vụ backend trung tâm cung cấp toàn bộ REST API cho hai ứng dụng frontend:
- **Storefront (Khách hàng)**: Duyệt sản phẩm, tìm kiếm, giỏ hàng, đặt hàng trực tuyến, áp dụng coupon, nhận email thông báo và theo dõi đơn hàng.
- **Admin Dashboard (Quản trị)**: Quản lý sản phẩm, danh mục, đơn hàng, coupon, bài viết tin tức, trang CMS tĩnh, banner, showroom và báo cáo doanh thu.

---

## 🛠 2. Công nghệ Sử dụng (Tech Stack)

| Thành phần | Công nghệ / Thư viện | M Describes / Ghi chú |
|---|---|---|
| **Core Framework** | Spring Boot 3.5.10, Java 21 | Kiến trúc REST API hiệu năng cao |
| **Database & Migration** | MySQL 8.0, Flyway 10 | Quản lý phiên bản schema (V1–V2) & seed data hợp nhất |
| **Data Access** | Spring Data JPA, Hibernate | JPA Specification tìm kiếm động & phân trang an toàn |
| **Security & Auth** | Spring Security 6, JJWT 0.12.6 | JWT Access Token, Refresh Token Rotation & Google OAuth2 |
| **Mapping & Utils** | MapStruct 1.6.3, Lombok | Ánh xạ DTO ↔ Entity compile-time |
| **API Docs & Monitoring**| Springdoc OpenAPI 2.8.6, Actuator | Swagger UI tại `/swagger-ui.html`, Health check tại `/actuator/health` |
| **Email & Storage** | Spring Mail, Thymeleaf, Local Storage| Gửi email HTML bất đồng bộ (`@Async`) & phục vụ file qua `/files/**` |

---

## 🏗 3. Kiến trúc Phân tầng (Layered Architecture)

Dự án tuân thủ nghiêm ngặt mô hình phân tầng đơn hướng (Clean Layered Architecture):

```text
HTTP Request ──▶ REST Controller ──▶ Service Interface ──▶ ServiceImpl ──▶ Repository / Spec ──▶ MySQL DB
                      │                                        │
                ApiResponse<T>                           MapStruct Mapper
                      ▲                                        │
           GlobalExceptionHandler ◀── AppException         Entity ↔ DTO
```

### Cấu trúc Thư mục Nguồn:

```text
vu-gia-backend-api/
├── src/main/java/vn/springboot/
│   ├── common/              # ApiResponse, PageResponse, AppException, ErrorCode, BaseEntity
│   ├── config/              # SecurityConfig, AsyncConfig, LocalStorageConfig, OpenApiConfig
│   ├── controller/          # 22 REST Controllers
│   ├── dto/                 # Request & Response DTOs
│   ├── entity/              # 19 JPA Entities (extending BaseEntity)
│   ├── event/               # OrderPlacedEvent & OrderEmailListener
│   ├── mapper/              # MapStruct mappers
│   ├── repository/          # Spring Data JPA Repositories & Specifications
│   ├── security/            # JWT Service, JwtFilter, GoogleTokenVerifier, SepaySignatureVerifier
│   └── service/             # Business Logic Interfaces & Implementations
├── src/main/resources/
│   ├── db/migration/        # Flyway schema migrations (V1, V3..V9)
│   ├── db/seed/             # Flyway seed data (V2)
│   ├── templates/email/     # Thymeleaf HTML Email Templates
│   └── application.yaml     # Application configuration
├── docs/                    # Tài liệu kiến trúc & hệ thống
├── Dockerfile               # Multi-stage Dockerfile
├── docker-compose.yml       # Production Compose (MySQL + Backend)
└── docker-compose.local.yml # Development Compose (MySQL standalone)
```

---

## ⚡ 4. Tính năng Cốt lõi (Key Features)

1. **Xác thực & Phân quyền (Auth & RBAC)**:
   - Đăng nhập Username/Email + Mật khẩu mã hóa BCrypt.
   - Đăng nhập nhanh bằng Google OAuth2 ID Token.
   - Cơ chế JWT Access Token (1 giờ) & Refresh Token Rotation lưu DB (7 ngày) chống hijacking.
   - Phân quyền theo vai trò (`SUPERADMIN`, `ADMIN`, `CUSTOMER`).

2. **Quản lý Sản phẩm (Product Catalog)**:
   - Cấu trúc danh mục phân cấp tự động tạo Slug SEO (`V7`, `V9`).
   - Quản lý thông tin chi tiết sản phẩm, SKU, tồn kho, giá bán, giá khuyến mãi, trạng thái nổi bật (`is_featured`), lượt bán (`sold_count`).
   - Lọc động đa tiêu chí với JPA Specification, chống SQL Injection và clamp kích thước trang (`<= 100`).

3. **Nghiệp vụ Đơn hàng & Thanh toán**:
   - Giỏ hàng lưu trữ theo tài khoản người dùng.
   - Đặt hàng an toàn chống trùng (Idempotent Key) & snapshot giá nguyên tử tại thời điểm đặt.
   - Áp dụng mã giảm giá nguyên tử (Atomic Coupon Redemption) tránh race condition.
   - Sinh ảnh chuyển khoản VietQR tự động (`VietQrPaymentService`).
   - Tiếp nhận và xác thực chữ ký HMAC-SHA256 Webhook từ SePay (`PaymentWebhookController`) tự động duyệt đơn sang `PAID`.

4. **Quản trị CMS & Marketing**:
   - Quản lý bài viết Tin tức (`News`), Danh mục tin (`NewsCategory`) theo Slug.
   - Nội dung Trang động CMS (`Page`), Slide/Banner (`Banner`), Showroom cửa hàng (`Showroom`), Bộ sưu tập (`GalleryImage`), FAQ & Redirect.
   - Tiếp nhận Form liên hệ (`ContactRequest`) và Đăng ký bản tin (`NewsletterSubscriber`).

5. **Báo cáo & Thống kê (Dashboard)**:
   - Tổng quan các chỉ số KPI: Doanh thu, số lượng đơn hàng, số khách hàng, tổng sản phẩm.
   - Biểu đồ thống kê doanh thu theo khoảng thời gian và danh sách top sản phẩm bán chạy.

6. **Lưu trữ Ảnh & Email**:
   - Upload & phục vụ ảnh local tại thư mục `./data` qua đường dẫn `/files/**`.
   - Ghép URL tuyệt đối tự động qua Annotation `@StorageUrl`.
   - Gửi email HTML xác nhận đơn hàng bất đồng bộ bằng Thymeleaf & Spring Mail.

---

## 🚀 5. Hướng dẫn Khởi chạy Dự án (Getting Started)

### Yêu cầu Tiền đề (Prerequisites):
- **Java OpenJDK 21** trở lên.
- **Maven 3.9+** (hoặc sử dụng `./mvnw` đính kèm).
- **MySQL 8.0** (chạy local trên port 3306/3307 hoặc qua Docker).

---

### Cách 1: Chạy Môi trường Dev (Local MySQL + Maven)

1. **Khởi chạy container MySQL (Docker)**:
   ```bash
   docker-compose -f docker-compose.local.yml up -d
   ```
   *Lưu ý: MySQL sẽ chạy trên port `3307`, database `db_vu_gia_fullstack`, username `root`, password `admin`.*

2. **Cấu hình môi trường (`.env` hoặc Biến môi trường)**:
   Mặc định `src/main/resources/application.yaml` đã thiết lập sẵn tham số cho môi trường Dev. Bạn có thể tạo file `.env` nếu muốn ghi đè:
   ```env
   DB_URL=jdbc:mysql://localhost:3307/db_vu_gia_fullstack?allowPublicKeyRetrieval=true&useSSL=false&createDatabaseIfNotExist=true
   DB_USERNAME=root
   DB_PASSWORD=admin
   APP_JWT_SECRET=fJV2+5QqrlNHmoqnaO4FliZ6rdwxbyi4ohpjMeKCKI+OXbe4y6eup8Vj0TaJem/aj8reEfbkK9aWenBdS7xlIg==
   APP_STORAGE_ROOT=./data
   APP_STORAGE_PUBLIC_URL=http://localhost:8080
   SEPAY_WEBHOOK_SECRET=your_sepay_secret
   ```

3. **Chạy ứng dụng Backend**:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Flyway sẽ tự động chạy script DDL hợp nhất (`V1`) và khởi tạo dữ liệu mẫu chuẩn (`V2`).*

---

### Cách 2: Chạy Toàn bộ Hệ thống bằng Docker Compose

```bash
docker-compose up -d --build
```
Hệ thống sẽ dựng 2 dịch vụ:
- `mysql`: Database MySQL 8.0 trên port `3306`.
- `backend`: Spring Boot App trên port `8080` (tự động chờ MySQL sẵn sàng qua healthcheck).

---

## 🔑 6. Tài khoản Mặc định & Phân quyền Initial

Khi khởi tạo thành công, hệ thống tự động tạo tài khoản SuperAdmin ban đầu (`DataInitializer`):
- **Username**: `admin`
- **Email**: `admin@gmail.com`
- **Mật khẩu**: `admin123`
- **Vai trò**: `SUPERADMIN`, `ADMIN`, `CUSTOMER`

---

## 📖 7. Danh sách REST API & Documentation

### OpenAPI / Swagger UI:
Sau khi ứng dụng khởi động thành công, truy cập Swagger UI tương tác tại:
🔗 **`http://localhost:8080/swagger-ui.html`**  
🔗 **OpenAPI Spec JSON**: `http://localhost:8080/v3/api-docs`

### Tóm tắt Nhóm REST API:

| Nhóm API | Base Path | Mô tả / Chức năng |
|---|---|---|
| **Xác thực** | `/api/auth` | Login, Register, Google OAuth, Refresh Token, Logout |
| **Người dùng** | `/api/users` | Profile cá nhân, đổi mật khẩu, Quản lý tài khoản (Admin) |
| **Danh mục SP** | `/api/product-categories` | Cây danh mục, slug SEO, CRUD danh mục |
| **Sản phẩm** | `/api/products` | Danh sách sản phẩm, lọc, chi tiết, CRUD sản phẩm |
| **Ảnh Sản phẩm** | `/api/products/{id}/images` | Upload & Xóa bộ sưu tập ảnh sản phẩm |
| **Giỏ hàng** | `/api/cart` | Xem giỏ hàng, thêm/sửa/xóa item, làm sạch giỏ hàng |
| **Đơn hàng** | `/api/orders` | Đặt hàng, chi tiết đơn, hủy đơn, cập nhật trạng thái (Admin) |
| **Mã giảm giá** | `/api/coupons` | Kiểm tra coupon, áp dụng, CRUD coupon (Admin) |
| **Tin tức** | `/api/news`, `/api/news-categories` | Bài viết tin tức & danh mục bài viết |
| **Trang CMS** | `/api/pages` | Trang nội dung động (Giới thiệu, Chính sách, Điều khoản) |
| **Banner / Slider**| `/api/banners` | Quản lý banner hiển thị trang chủ |
| **Showroom** | `/api/showrooms` | Hệ thống cửa hàng & đại lý |
| **Gallery** | `/api/gallery-images` | Bộ sưu tập hình ảnh gốm sứ thực tế |
| **FAQ / Redirect** | `/api/faqs`, `/api/redirects` | Câu hỏi thường gặp & chuyển hướng URL (301/302) |
| **Liên hệ & Mail** | `/api/contact-requests`, `/api/newsletter` | Form liên hệ & Đăng ký bản tin |
| **Vận chuyển** | `/api/shipping-methods` | Phương thức giao hàng & tính phí |
| **Dashboard** | `/api/dashboard` | Thống kê KPI, doanh thu, top sản phẩm (Admin) |
| **Webhook Payment**| `/api/webhooks/sepay` | Tiếp nhận webhook xác thực chuyển khoản SePay |
| **Media Storage** | `/api/media` | Upload & Quản lý file ảnh local |

---

## 🧪 8. Kiểm thử & Kiểm tra Sức khỏe (Testing & Observability)

### 1. Chạy Unit Tests & Controller Integration Tests:
```bash
./mvnw test
```

### 2. Kiểm tra Sức khỏe Hệ thống (Actuator Health Check):
- **Health Check**: `GET http://localhost:8080/actuator/health`
- **Application Info**: `GET http://localhost:8080/actuator/info`

---

## 📚 9. Danh mục Tài liệu Kỹ thuật Chi tiết

Để tìm hiểu chi tiết về thiết kế kiến trúc, quy chuẩn mã nguồn và lộ trình phát triển, vui lòng tham khảo các tài liệu trong thư mục `docs/`:

- [📜 `project-overview-pdr.md`](docs/project-overview-pdr.md) — Tổng quan dự án, ma trận yêu cầu chức năng & phi chức năng (PDR).
- [📁 `codebase-summary.md`](docs/codebase-summary.md) — Bản đồ chi tiết package Java, bảng CSDL & các Flyway migration.
- [📏 `code-standards.md`](docs/code-standards.md) — Quy chuẩn kiến trúc phân tầng, envelope response, mã lỗi `ErrorCode` & testing.
- [🏛 `system-architecture.md`](docs/system-architecture.md) — Sơ đồ kiến trúc Mermaid (Auth JWT/OAuth2, Order Flow, SePay Webhook, Storage).
- [🗺 `project-roadmap.md`](docs/project-roadmap.md) — Trạng thái tính năng v1.0.0 & Kế hoạch phát triển các phiên bản tương lai.

---

© 2026 Gốm Sứ Vũ Gia. All rights reserved.
