<div align="center">

# 🏺 Gốm Sứ Vũ Gia — Backend Thương mại điện tử

**REST API Spring Boot cho cửa hàng gốm sứ Vũ Gia — danh mục sản phẩm, giỏ hàng, đặt hàng, mã giảm giá, nội dung CMS, dashboard quản trị, JWT + RBAC, lưu ảnh MinIO và tự động migrate/seed bằng Flyway.**

<br/>

<!-- 🌐 Nút chuyển ngôn ngữ -->
<a href="README.md"><img src="https://img.shields.io/badge/🇻🇳_Tiếng_Việt-2C5BFF?style=for-the-badge" alt="Tiếng Việt"/></a>
<a href="README.en.md"><img src="https://img.shields.io/badge/🇬🇧_English-555?style=for-the-badge" alt="English"/></a>

<br/><br/>

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Security-JWT%20%2B%20RBAC-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrate%20%2B%20Seed-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-C72E49?style=for-the-badge&logo=minio&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

</div>

---

## 📑 Mục lục

- [Tổng quan](#-tổng-quan)
- [Công nghệ](#-công-nghệ)
- [Chức năng & Module](#-chức-năng--module)
- [Kiến trúc](#-kiến-trúc)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Bắt đầu](#-bắt-đầu)
- [Cấu hình](#-cấu-hình)
- [Tài liệu API](#-tài-liệu-api)
- [Mô hình bảo mật](#-mô-hình-bảo-mật)
- [Định dạng response](#-định-dạng-response)
- [License](#-license)

---

## 🧭 Tổng quan

Backend REST API cho storefront **Gốm Sứ Vũ Gia**. Phủ toàn bộ luồng thương mại — danh mục sản phẩm, giỏ hàng, đặt hàng kèm mã giảm giá — cùng nội dung dạng CMS (tin tức, trang, banner, gallery, showroom, FAQ) và dashboard thống kê cho admin.

Mã nguồn bám **kiến trúc phân tầng** chặt chẽ và nguyên tắc **SOLID**: mỗi service viết theo interface, lọc động bằng JPA Specification, map DTO ↔ Entity ở compile-time (MapStruct), các mối quan tâm chéo (bảo mật, lỗi, auditing) nằm ở package riêng. Schema và dữ liệu mẫu được áp dụng **tự động khi khởi động** bằng **Flyway**.

---

## 🛠 Công nghệ

| Tầng | Công nghệ | Version | Vai trò |
|---|---|---|---|
| **Ngôn ngữ** | Java | 21 (LTS) | record, pattern matching, text block |
| **Framework** | Spring Boot | 3.5.10 | Auto-config & DI |
| **Web** | Spring Web (MVC) | — | REST controller, JSON |
| **Persistence** | Spring Data JPA + Hibernate | — | ORM, repository, Specification API |
| **CSDL** | MySQL | 8.x | DB quan hệ (`mysql-connector-j`) |
| **Migration** | Flyway (+ `flyway-mysql`) | — | Tự migrate schema + seed data lúc khởi động |
| **Bảo mật** | Spring Security | — | Xác thực, method security (`@PreAuthorize`) |
| **Token** | JJWT | 0.12.6 | JWT access token (HS512) + refresh rotation |
| **OAuth** | Google Identity | — | Đăng nhập Google (xác thực ID-token) |
| **Lưu trữ ảnh** | MinIO | 8.5.x | Ảnh sản phẩm/asset (bucket public) |
| **Email** | Spring Mail + Thymeleaf | — | Email **HTML** bất đồng bộ |
| **Mapping** | MapStruct | 1.6.3 | Map DTO ↔ Entity compile-time |
| **Validation** | Jakarta Bean Validation | — | Kiểm tra payload `@Valid` |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) | — | Tài liệu tương tác `/swagger-ui.html` |
| **Monitoring** | Spring Boot Actuator | — | `/actuator/health`, `/actuator/info` |
| **Boilerplate** | Lombok | — | getter/setter/builder |
| **Build** | Maven (wrapper `mvnw`) | — | Quản lý phụ thuộc & đóng gói |
| **Test** | JUnit 5 + Mockito + Spring Security Test | — | Test service & controller |

---

## ✨ Chức năng & Module

### Nền tảng lõi
- 🔐 **JWT + refresh rotation** — access token HS512 stateless; refresh token lưu DB (logout/thu hồi thật). Đăng nhập bằng **username hoặc email**, kèm **đăng nhập Google**.
- 👥 **RBAC** — mô hình 1 vai trò (`Role` enum: `SUPERADMIN` / `ADMIN` / `CUSTOMER`); chặn quyền bằng `@PreAuthorize`.
- 🧾 **Envelope thống nhất** — mọi response là `{ code, message, data, timestamp }`; `code=1000` = thành công.
- 🧯 **Xử lý lỗi tập trung** — một `@RestControllerAdvice` map exception → error code ổn định.
- 🕵️ **JPA auditing** — `createdAt / updatedAt / createdBy / updatedBy` tự điền.
- 🔎 **Search & phân trang an toàn** — JPA Specification + whitelist sort (phân trang 1-based).
- 🌱 **Tự migrate + seed** — Flyway chạy schema (`db/migration`) rồi data mẫu (`db/seed`); seed admin idempotent.
- 🖼 **Ảnh MinIO** — upload vào bucket public (`assets`, `products`), bucket tự tạo & set public-read khi khởi động.

### Các module nghiệp vụ
| Miền | Điểm nổi bật |
|---|---|
| **Auth / User** | đăng ký · đăng nhập · refresh · logout · me · đăng nhập Google · đổi mật khẩu · quản trị user (list, tạo, đổi vai trò, reset mật khẩu) |
| **Sản phẩm** | catalog + danh mục + ảnh (MinIO), bật/tắt trạng thái & nổi bật, **tra cứu theo slug (SEO)** |
| **Nội dung / CMS** | Tin tức + danh mục (theo slug), **Trang** (theo key), Banner, Showroom, Gallery, FAQ, Redirect |
| **Marketing** | **Mã giảm giá** (PERCENT / FIXED / FREE_SHIP, validate + điều kiện), đăng ký Newsletter, form Liên hệ |
| **Giỏ hàng** | giỏ theo user, cộng dồn số lượng, tính tổng trực tiếp |
| **Đơn hàng** | **đặt hàng idempotent**, snapshot giá, **trừ lượt coupon nguyên tử** (race-free), trừ giỏ, `sold_count` chỉ tăng khi **COMPLETED**, **email xác nhận HTML bất đồng bộ**, admin search đơn |
| **Dashboard** | KPI admin (doanh thu/đơn/khách), doanh thu theo ngày, top sản phẩm bán chạy |

> Tài liệu endpoint đầy đủ theo từng module nằm trong [`docs/`](docs).

---

## 🏗 Kiến trúc

Luồng phụ thuộc một chiều; tầng web không đụng thẳng persistence, service phụ thuộc **interface**.

```text
HTTP ─▶ Controller ─▶ Service (interface → impl) ─▶ Repository (+ Specification) ─▶ MySQL
          │                     │
          │ ApiResponse<T>      │ MapStruct mapper
          ▼                     ▼
  GlobalExceptionHandler ◀─ AppException(ErrorCode)
          ▲
  Chuỗi filter bảo mật (JWT) ─▶ 401 EntryPoint / 403 AccessDeniedHandler

  Đơn commit ─▶ @TransactionalEventListener(AFTER_COMMIT) ─▶ @Async email HTML (Thymeleaf)
```

---

## 📂 Cấu trúc dự án

```text
src/main/java/vn/springboot
├── Application.java
├── common
│   ├── entity/BaseEntity.java            # id + cột audit
│   ├── exception/                        # ErrorCode, AppException, GlobalExceptionHandler
│   └── response/ApiResponse.java         # envelope thống nhất
├── config                                # Async, JPA auditing, MinIO, khởi tạo bucket, seed admin
├── controller                            # 20 REST controller (theo module)
├── dto/{request,response}                # DTO request/response theo miền
├── entity                                # 19 entity JPA (product, order, cart, news, page, …)
├── event                                 # OrderPlacedEvent + OrderEmailListener (async)
├── mapper                                # MapStruct mapper
├── repository (+ specification)          # Spring Data repo + filter động
├── security                              # SecurityConfig, JWT, CustomUserDetails, handler
└── service (+ impl)                      # nghiệp vụ sau interface

src/main/resources
├── application.yaml
├── db/migration/V1__init_db.sql          # schema (Flyway)
├── db/seed/V2__seed_db.sql               # data mẫu (Flyway)
└── templates/email/order-confirmation.html   # email HTML (Thymeleaf)
```

---

## 🚦 Bắt đầu

### Cách A — Docker (khuyến nghị, 1 lệnh)

Dựng **MySQL + MinIO + app**; app tự migrate schema và seed data khi khởi động.

```bash
docker compose up -d --build
docker compose logs -f app        # xem: "Successfully applied 2 migrations"
```

- API → **http://localhost:8080** (Swagger: `/swagger-ui.html`)
- MinIO Console → **http://localhost:9001** (`minioadmin` / `minioadmin123`)

### Cách B — Chạy local

**Yêu cầu:** JDK 21, MySQL 8 đang chạy (MinIO tuỳ chọn, chỉ cần khi phục vụ ảnh).

```bash
# DB mới: MySQL tự tạo qua flag URL; Flyway dựng schema + seed
DB_URL="jdbc:mysql://localhost:3306/dev_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false" \
  ./mvnw spring-boot:run

# Build jar chạy được
./mvnw clean package && java -jar target/spring-boot-0.0.1-SNAPSHOT.jar
```

### Tài khoản admin mặc định

| Username | Password | Email |
|---|---|---|
| `admin` | `admin123` | `admin@gmail.com` |

> ⚠️ Đổi mật khẩu trước khi deploy.

### Ảnh (MinIO)

App tự tạo bucket `assets` và `products` dạng public-read. Upload thư mục `assets/images/` vào bucket **`assets`** qua MinIO console — xem chi tiết ở **[docs/RUN_AND_SEED.md](docs/RUN_AND_SEED.md)**. DB lưu **đường dẫn tương đối** (vd `assets/images/gallery/gallery-1.jpg`); FE tự cộng tiền tố URL MinIO.

---

## ⚙️ Cấu hình

Mọi thứ trong `src/main/resources/application.yaml` đều override được qua biến môi trường.

| Biến môi trường | Mặc định | Mô tả |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/dev_db` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `rootpassword` | Thông tin DB |
| `APP_JWT_SECRET` | *(mặc định dev)* | Khoá HS512 base64 512-bit — **phải override ở prod** |
| `MINIO_URL` | `http://localhost:9000` | Endpoint MinIO (phía server) |
| `MINIO_PUBLIC_URL` | `http://localhost:9000` | Base URL ảnh công khai (trình duyệt / CDN) |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | `minioadmin` / `minioadmin123` | Thông tin MinIO |
| `MINIO_BUCKET_ASSET` / `MINIO_BUCKET_PRODUCT` | `assets` / `products` | Tên bucket |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | *(rỗng)* | Tài khoản SMTP (cần để gửi email thật) |
| `APP_MAIL_FROM` | *(rỗng)* | Địa chỉ "From" cho email |
| `GOOGLE_CLIENT_ID` | *(rỗng)* | Google OAuth Web client ID (rỗng = tắt kiểm tra — chỉ dev) |
| `app.init.enabled` | `true` | Bật/tắt seed admin khi khởi động |

> 🔒 **Prod:** sinh `APP_JWT_SECRET` mới, đặt SMTP + MinIO thật, trỏ `MINIO_PUBLIC_URL` về CDN/domain.

---

## 🔌 Tài liệu API

Base path **`/api`** · Swagger UI **`/swagger-ui.html`** · Health **`/actuator/health`**.

Tài liệu theo từng module (request/response, error code, curl dán Postman):

| Module | Tài liệu |
|---|---|
| Auth & User (RBAC) | [docs/AUTH_USER_API.md](docs/AUTH_USER_API.md) |
| Sản phẩm & Danh mục | [docs/PRODUCT_API.md](docs/PRODUCT_API.md) |
| Tin tức & Danh mục | [docs/NEWS_API.md](docs/NEWS_API.md) |
| Mã giảm giá | [docs/COUPON_API.md](docs/COUPON_API.md) |
| Giỏ hàng | [docs/CART_API.md](docs/CART_API.md) |
| Đơn hàng | [docs/ORDER_API.md](docs/ORDER_API.md) |
| Dashboard admin | [docs/DASHBOARD_API.md](docs/DASHBOARD_API.md) |
| Trang (CMS) | [docs/PAGE_API.md](docs/PAGE_API.md) |
| Liên hệ | [docs/CONTACT_API.md](docs/CONTACT_API.md) |
| Newsletter | [docs/NEWSLETTER_API.md](docs/NEWSLETTER_API.md) |
| Banner / Showroom / Gallery / FAQ / Redirect | [docs/BASIC_MODULES_API.md](docs/BASIC_MODULES_API.md) |
| Chạy & seed & MinIO | [docs/RUN_AND_SEED.md](docs/RUN_AND_SEED.md) |

**Ví dụ đăng nhập**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 🛡 Mô hình bảo mật

1. **Login** kiểm tra thông tin qua `AuthenticationManager` + `BCryptPasswordEncoder` (hoặc Google ID token).
2. Server phát **JWT access token** ngắn hạn (HS512) + **refresh token** dài hạn lưu DB (xoay vòng mỗi lần refresh).
3. `JwtAuthenticationFilter` kiểm token mỗi request và nạp `SecurityContext`.
4. **Phân quyền**: **đọc storefront công khai**; **ghi cần staff** (`@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")`); quản trị user chỉ `SUPERADMIN`; giỏ hàng/đặt hàng cần user đăng nhập.
5. Lỗi auth trả JSON nhất quán: **401** (`JwtAuthenticationEntryPoint`) hoặc **403** (`CustomAccessDeniedHandler`).

**Vai trò:** `SUPERADMIN` › `ADMIN` › `CUSTOMER` (lưu ở `users.role`).

---

## 📦 Định dạng response

**Thành công**
```json
{ "code": 1000, "message": "Order placed", "data": { "...": "..." }, "timestamp": "2026-07-11T02:00:00Z" }
```
**Lỗi**
```json
{ "code": 4105, "message": "Mã giảm giá đã hết hạn", "data": null, "timestamp": "2026-07-11T02:00:00Z" }
```

**Dải error code**

| Dải | Ý nghĩa |
|---|---|
| `1000` | Thành công |
| `4000–4005` | Bad request / validation |
| `401x` | Xác thực |
| `4030` | Phân quyền (403) |
| `404x` | Không tìm thấy |
| `409x` / `41xx` | Conflict (trùng, coupon không đủ điều kiện…) |
| `9000–9999` | Lỗi server / nội bộ |

---

## 📄 License

Phát hành theo **MIT License**.

---

<div align="center">

**Gốm Sứ Vũ Gia** — Xây bằng ❤️ với Spring Boot

</div>
