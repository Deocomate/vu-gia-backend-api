# 📁 Gốm Sứ Vũ Gia — Codebase Summary & Directory Structure

> **Mô tả**: Tài liệu tổng quan về cấu trúc mã nguồn, bản đồ package, các thành phần cốt lõi và lược đồ dữ liệu dự án backend `vu-gia-backend-api`.

---

## 1. Cấu trúc thư mục tổng quan

```text
vu-gia-backend-api/
├── .claude/                             # Cấu hình workspace Claude Kit
├── assets/                              # Tài nguyên hình ảnh minh họa / sơ đồ
├── data/                                # Thư mục đĩa lưu ảnh upload local (ignored in git)
├── docs/                                # Thư mục chứa toàn bộ tài liệu hệ thống
│   ├── project-overview-pdr.md          # Project Overview & PDR
│   ├── codebase-summary.md              # Tài liệu này
│   ├── code-standards.md                # Quy chuẩn lập trình & kiến trúc
│   ├── system-architecture.md           # Sơ đồ & Kiến trúc hệ thống
│   └── project-roadmap.md               # Lộ trình phát triển & tính năng
├── plans/                               # Các kế hoạch phát triển / refactor
├── src/
│   ├── main/
│   │   ├── java/vn/springboot/          # Mã nguồn Java chính
│   │   └── resources/                   # Application config & templates
│   │       ├── templates/email/         # Thymeleaf HTML Email Templates
│   │       └── application.yaml         # Spring Boot Config
│   └── test/java/vn/springboot/         # Unit test & Controller integration test
├── docker-compose.yml                   # Docker Compose môi trường production (MySQL + App)
├── docker-compose.local.yml             # Docker Compose môi trường dev (MySQL standalone)
├── Dockerfile                           # Docker build multi-stage
├── mvnw / mvnw.cmd                      # Maven Wrapper
├── pom.xml                              # Khai báo phụ thuộc Maven
└── README.md                            # Document chính dự án (Tiếng Việt)
```

---

## 2. Chi tiết Package Mã Nguồn Java (`src/main/java/vn/springboot`)

| Package | Vai trò & Thành phần chính |
|---|---|
| `vn.springboot` | Entry point chính: `Application.java` (`@SpringBootApplication`, `@EnableAsync`, `@EnableScheduling`). |
| `vn.springboot.common` | Hạ tầng dùng chung toàn ứng dụng: <br/>• `entity/BaseEntity.java`: Lớp cơ sở chứa `id`, `createdAt`, `updatedAt`. <br/>• `response/ApiResponse.java` & `PageResponse.java`: Envelope chuẩn hóa response API. <br/>• `exception/`: `AppException`, `ErrorCode` (enum dải mã lỗi), `GlobalExceptionHandler`. |
| `vn.springboot.config` | Cấu hình Spring Boot Core: <br/>• `SecurityConfig.java`: Cấu hình Spring Security 6, JWT Filter, Stateless Session, CORS, Web Security. <br/>• `JpaAuditingConfig.java`: Bật tự động điền ngày tạo/người tạo. <br/>• `AsyncConfig.java`: Bật xử lý bất đồng bộ cho gửi Email. <br/>• `LocalStorageConfig.java`: Cấu hình phục vụ file tĩnh `/files/**` từ đĩa. <br/>• `OpenApiConfig.java`: Cấu hình Swagger UI OpenAPI 3.0. |
| `vn.springboot.controller` | Chứa 22 REST Controllers nhận HTTP Request và trả `ApiResponse<T>`: <br/>`AuthController`, `UserController`, `ProductController`, `ProductCategoryController`, `ProductImageController`, `CartController`, `OrderController`, `CouponController`, `NewsController`, `NewsCategoryController`, `PageController`, `BannerController`, `ShowroomController`, `GalleryImageController`, `FaqController`, `RedirectController`, `ContactRequestController`, `NewsletterSubscriberController`, `ShippingMethodController`, `DashboardController`, `PaymentWebhookController`, `MediaController`. |
| `vn.springboot.service` & `service.impl` | Chứa Interfaces và Implementations xử lý logic nghiệp vụ. Tầng Service được phân tách độc lập với Controller và Repository. |
| `vn.springboot.repository` & `specification` | Tầng truy vấn dữ liệu Spring Data JPA: <br/>• Thừa kế `JpaRepository` & `JpaSpecificationExecutor`. <br/>• Package `specification/`: Chứa các lớp JPA Specification xây dựng truy vấn động, lọc theo từ khóa, danh mục, giá, ngày tháng. |
| `vn.springboot.entity` | Chứa 21 Entity JPA tương ứng với các bảng trong CSDL MySQL. Tất cả Entity đều mở rộng từ `BaseEntity`. |
| `vn.springboot.dto` | Chứa Data Transfer Objects phân chia theo gói `request` và `response` cho từng miền nghiệp vụ. |
| `vn.springboot.mapper` | Khai báo các giao diện ánh xạ MapStruct (`@Mapper(componentModel = "spring")`) để chuyển đổi giữa Entity và DTO. |
| `vn.springboot.security` | Các lớp xử lý xác thực & bảo mật: <br/>• `jwt/JwtService.java` & `JwtAuthenticationFilter.java`: Sinh, kiểm tra và trích xuất JWT. <br/>• `oauth2/GoogleTokenVerifier.java`: Xác thực Google ID Token. <br/>• `CustomUserDetailsService.java`: Load thông tin user từ CSDL cho Spring Security. <br/>• `SepaySignatureVerifier.java`: Kiểm tra chữ ký bảo mật Webhook SePay. |
| `vn.springboot.event` | Cơ chế sự kiện bất đồng bộ: <br/>• `OrderPlacedEvent.java`: Sự kiện phát ra khi đơn hàng được tạo thành công. <br/>• `OrderEmailListener.java`: Lắng nghe sự kiện để gửi email HTML xác nhận đơn hàng qua Thymeleaf. |
| `vn.springboot.seed` | Framework seed dữ liệu code-first (thay thế SQL seed cũ): <br/>• `DomainSeeder`: interface chung (`isEmpty()`/`reset()`/`seed()`) cho 12 seeder theo từng miền (shipping method, user, product category/product, news category/news, banner, showroom, gallery image, faq, page, coupon). <br/>• `SeedRunner`: `CommandLineRunner` điều phối theo thứ tự FK-safe cố định; `APP_ENV=development` → xoá sạch + seed lại toàn bộ (kể cả bảng giao dịch qua `TransactionalDataCleaner`); ngược lại chỉ seed bảng còn trống. <br/>• `OrphanReferenceChecker`: kiểm tra dangling reference sau mỗi lần seed (không có FK thật ở tầng CSDL). |

---

## 3. Quản lý Cơ sở dữ liệu (Code-First)

Dự án dùng **JPA Entity làm nguồn chân lý duy nhất của schema** (`spring.jpa.hibernate.ddl-auto=update`, mọi profile kể cả production — đánh đổi có chủ đích, xem `system-architecture.md`). Không còn migration SQL nào; dữ liệu mẫu do `vn.springboot.seed.SeedRunner` và 12 `DomainSeeder` (Java, dùng JPA repository) đảm nhiệm:

- `vn.springboot.entity.*`: 21 Entity JPA định nghĩa toàn bộ schema (bảng, khóa chính, chỉ mục, ràng buộc duy nhất) qua annotation `@Table`/`@Column`.
- `vn.springboot.seed.*`: 12 `DomainSeeder` (shipping method, user, product category/product, news category/news, banner, showroom, gallery image, faq, page, coupon) port 1:1 dữ liệu mẫu cũ từ SQL seed đã xoá; `SeedRunner` điều phối, `OrphanReferenceChecker` kiểm tra tính toàn vẹn tham chiếu sau mỗi lần seed.
- `src/main/resources/templates/email/order-confirmation.html`: Template Email HTML giao diện chuẩn render bằng Thymeleaf để gửi thông báo đơn hàng.
- `src/main/resources/application.yaml`: Tập tin cấu hình môi trường chính của Spring Boot.

---

## 4. Tóm tắt các Thực thể CSDL chính (Core Entities)

1. **User & Auth**: `UserEntity`, `RoleEntity`, `RefreshTokenEntity`.
2. **Product Catalog**: `ProductEntity`, `ProductCategoryEntity`, `ProductImageEntity`.
3. **Cart & Order**: `CartItemEntity`, `OrderEntity`, `OrderItemEntity`, `CouponEntity`, `ShippingMethodEntity`, `PaymentTransactionEntity`.
4. **CMS & Content**: `NewsEntity`, `NewsCategoryEntity`, `PageEntity`, `BannerEntity`, `ShowroomEntity`, `GalleryImageEntity`, `FaqEntity`, `RedirectEntity`.
5. **Marketing**: `ContactRequestEntity`, `NewsletterSubscriberEntity`.
