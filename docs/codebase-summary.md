# 📁 Gốm Sứ Vũ Gia — Codebase Summary & Directory Structure

> **Mô tả**: Tài liệu tổng quan về cấu trúc mã nguồn, bản đồ package, các thành phần cốt lõi và lược đồ dữ liệu dự án backend `vu-gia-backend-api`.

---

## 1. Cấu trúc thư mục tổng quan

```text
vu-gia-backend-api/
├── .claude/                             # Cấu hình workspace Claude Kit
├── assets/                              # Tài nguyên hình ảnh minh họa / sơ đồ
├── data/                                # Thư mục đĩa lưu ảnh upload local (ignored in git)
├── docs/                                # Thư mục chứa toàn bộ tài liệu dự án
│   ├── AUTH_USER_API.md
│   ├── PRODUCT_API.md
│   ├── ORDER_API.md
│   ├── CART_API.md
│   ├── COUPON_API.md
│   ├── NEWS_API.md
│   ├── PAGE_API.md
│   ├── DASHBOARD_API.md
│   ├── BASIC_MODULES_API.md
│   ├── CONTACT_API.md
│   ├── NEWSLETTER_API.md
│   ├── SHIPPING_API.md
│   ├── FILE_STORAGE_API.md
│   ├── RUN_AND_SEED.md
│   ├── MINIO_DOCKER_PRODUCTION_SETUP.md
│   ├── MYSQL_DOCKER_PRODUCTION_SETUP.md
│   ├── project-overview-pdr.md          # Project Overview & PDR
│   ├── codebase-summary.md              # Tài liệu này
│   ├── code-standards.md                # Quy chuẩn lập trình & kiến trúc
│   ├── system-architecture.md           # Sơ đồ & Kiến trúc hệ thống
│   ├── project-roadmap.md               # Lộ trình phát triển
│   ├── deployment-guide.md              # Hướng dẫn triển khai
│   └── design-guidelines.md             # Quy chuẩn API & Response
├── plans/                               # Các kế hoạch phát triển / refactor
├── src/
│   ├── main/
│   │   ├── java/vn/springboot/          # Mã nguồn Java chính
│   │   └── resources/                   # Application config, Flyway migrations & templates
│   └── test/java/vn/springboot/         # Unit test & Controller test
├── docker-compose.yml                   # Docker Compose môi trường production
├── docker-compose.local.yml             # Docker Compose môi trường dev
├── Dockerfile                           # Docker build multi-stage
├── mvnw / mvnw.cmd                      # Maven Wrapper
├── pom.xml                              # Khai báo phụ thuộc Maven
├── CLAUDE.md                            # Quy trình hướng dẫn AI & team
├── README.md                            # Document chính dự án (Tiếng Việt)
└── README.en.md                         # Document chính dự án (English)
```

---

## 2. Chi tiết Package Mã Nguồn Java (`src/main/java/vn/springboot`)

| Package | Vai trò & Thành phần chính |
|---|---|
| `vn.springboot` | Entry point chính: `Application.java` (`@SpringBootApplication`, `@EnableAsync`, `@EnableScheduling`). |
| `vn.springboot.common` | Hạ tầng dùng chung toàn ứng dụng: <br/>• `entity/BaseEntity.java`: Lớp cơ sở chứa `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`. <br/>• `response/ApiResponse.java` & `PageResponse.java`: Envelope chuẩn hóa response API. <br/>• `exception/`: `AppException`, `ErrorCode` (enum dải mã lỗi), `GlobalExceptionHandler`. |
| `vn.springboot.config` | Cấu hình Spring Boot Core: <br/>• `SecurityConfig.java`: Cấu hình Spring Security 6, JWT Filter, Stateless Session, CORS, Web Security. <br/>• `JpaAuditingConfig.java`: Bật tự động điền ngày tạo/người tạo. <br/>• `AsyncConfig.java`: Bật xử lý bất đồng bộ cho gửi Email. <br/>• `LocalStorageConfig.java`: Cấu hình phục vụ file tĩnh `/files/**` từ đĩa. <br/>• `DataInitializer.java`: Tự động seed tài khoản SuperAdmin ban đầu. <br/>• `OpenApiConfig.java`: Cấu hình Swagger UI OpenAPI 3.0. |
| `vn.springboot.controller` | Chứa 22 REST Controllers nhận HTTP Request và trả `ApiResponse<T>`: <br/>`AuthController`, `UserController`, `ProductController`, `ProductCategoryController`, `ProductImageController`, `CartController`, `OrderController`, `CouponController`, `NewsController`, `NewsCategoryController`, `PageController`, `BannerController`, `ShowroomController`, `GalleryImageController`, `FaqController`, `RedirectController`, `ContactRequestController`, `NewsletterSubscriberController`, `ShippingMethodController`, `DashboardController`, `PaymentWebhookController`, `MediaController`. |
| `vn.springboot.service` & `service.impl` | Chứa Interfaces và Implementations xử lý logic nghiệp vụ. Tầng Service được phân tách độc lập với Controller và Repository. |
| `vn.springboot.repository` & `specification` | Tầng truy vấn dữ liệu Spring Data JPA: <br/>• Thừa kế `JpaRepository` & `JpaSpecificationExecutor`. <br/>• Package `specification/`: Chứa các lớp JPA Specification xây dựng truy vấn động, lọc theo từ khóa, danh mục, giá, ngày tháng. |
| `vn.springboot.entity` | Chứa 19 Entity JPA tương ứng với các bảng trong CSDL MySQL. Tất cả Entity đều mở rộng từ `BaseEntity`. |
| `vn.springboot.dto` | Chứa Data Transfer Objects phân chia theo gói `request` và `response` cho từng miền nghiệp vụ. |
| `vn.springboot.mapper` | Khai báo các giao diện ánh xạ MapStruct (`@Mapper(componentModel = "spring")`) để chuyển đổi giữa Entity và DTO. |
| `vn.springboot.security` | Các lớp xử lý xác thực & bảo mật: <br/>• `jwt/JwtService.java` & `JwtAuthenticationFilter.java`: Sinh, kiểm tra và trích xuất JWT. <br/>• `oauth2/GoogleTokenVerifier.java`: Xác thực Google ID Token. <br/>• `CustomUserDetailsService.java`: Load thông tin user từ CSDL cho Spring Security. <br/>• `SepaySignatureVerifier.java`: Kiểm tra chữ ký bảo mật Webhook SePay. |
| `vn.springboot.event` | Cơ chế sự kiện bất đồng bộ: <br/>• `OrderPlacedEvent.java`: Sự kiện phát ra khi đơn hàng được tạo thành công. <br/>• `OrderEmailListener.java`: Lắng nghe sự kiện để gửi email HTML xác nhận đơn hàng qua Thymeleaf. |

---

## 3. Quản lý Cơ sở dữ liệu & Migrations (`src/main/resources`)

Dự án áp dụng **Flyway Database Migration** để quản lý phiên bản CSDL và khởi tạo dữ liệu tự động:

- `src/main/resources/db/migration/V1__init_db.sql`: DDL định nghĩa toàn bộ 19 bảng trong CSDL MySQL, bao gồm khóa chính, khóa ngoại, chỉ mục (Index) và ràng buộc duy nhất (Unique Constraints).
- `src/main/resources/db/seed/V2__seed_db.sql`: Dữ liệu khởi tạo mẫu (Seed data) về danh mục sản phẩm, sản phẩm, mã giảm giá, bài viết tin tức, thông tin cửa hàng và cấu hình hệ thống.
- `src/main/resources/templates/email/order-confirmation.html`: Template Email HTML giao diện đẹp render bằng Thymeleaf để gửi thông báo đơn hàng.
- `src/main/resources/application.yaml`: Tập tin cấu hình môi trường chuẩn của Spring Boot.

---

## 4. Tóm tắt các Thực thể CSDL chính (Core Entities)

1. **User & Auth**: `UserEntity`, `RoleEntity`, `RefreshTokenEntity`.
2. **Product Catalog**: `ProductEntity`, `ProductCategoryEntity`, `ProductImageEntity`.
3. **Cart & Order**: `CartItemEntity`, `OrderEntity`, `OrderItemEntity`, `CouponEntity`, `ShippingMethodEntity`, `PaymentTransactionEntity`.
4. **CMS & Content**: `NewsEntity`, `NewsCategoryEntity`, `PageEntity`, `BannerEntity`, `ShowroomEntity`, `GalleryImageEntity`, `FaqEntity`, `RedirectEntity`.
5. **Marketing**: `ContactRequestEntity`, `NewsletterSubscriberEntity`.
