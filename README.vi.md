<div align="center">

# 🚀 Spring Boot Starter — Xác thực JWT & RBAC

**Bộ khung Spring Boot sẵn sàng cho production với xác thực JWT, xoay vòng refresh-token và phân quyền theo vai trò/quyền hạn.**

<br/>

<!-- 🌐 Nút chuyển ngôn ngữ / Language switcher -->
<a href="README.md"><img src="https://img.shields.io/badge/🇬🇧_English-555?style=for-the-badge" alt="English"/></a>
<a href="README.vi.md"><img src="https://img.shields.io/badge/🇻🇳_Tiếng_Việt-2C5BFF?style=for-the-badge" alt="Tiếng Việt"/></a>

<br/><br/>

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-Database-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

</div>

---

## 📑 Mục lục

- [Tổng quan](#-tổng-quan)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Tính năng](#-tính-năng)
- [Kiến trúc](#-kiến-trúc)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Bắt đầu](#-bắt-đầu)
- [Cấu hình](#-cấu-hình)
- [Tài liệu API](#-tài-liệu-api)
- [Mô hình bảo mật](#-mô-hình-bảo-mật)
- [Định dạng phản hồi](#-định-dạng-phản-hồi)
- [Giấy phép](#-giấy-phép)

---

## 🧭 Tổng quan

Đây là một bộ khung Spring Boot phân tầng, sạch sẽ, bạn có thể fork để khởi tạo nhanh bất kỳ REST API nào. Dự án đi kèm luồng xác thực hoàn chỉnh (đăng ký / đăng nhập / làm mới token / đăng xuất / lấy thông tin người dùng hiện tại), mô hình **phân quyền theo vai trò (RBAC)** với quyền hạn chi tiết, JPA auditing, một lớp vỏ phản hồi JSON thống nhất và xử lý ngoại lệ tập trung.

Mã nguồn tuân thủ nguyên tắc **SOLID**: mọi service đều lập trình theo interface, trách nhiệm được tách bạch rõ ràng, và các mối quan tâm xuyên suốt (bảo mật, lỗi, audit) được đặt trong các package riêng.

---

## 🛠 Công nghệ sử dụng

| Tầng | Công nghệ | Phiên bản | Mục đích |
|---|---|---|---|
| **Ngôn ngữ** | Java | 21 (LTS) | Ngôn ngữ chính, dùng record/pattern-matching hiện đại |
| **Framework** | Spring Boot | 3.5.10 | Nền tảng auto-configuration & dependency injection |
| **Web** | Spring Web (MVC) | — | REST controller, serialize JSON |
| **Lưu trữ** | Spring Data JPA + Hibernate | — | ORM, repository abstraction, Specification API |
| **CSDL** | MariaDB | — | CSDL quan hệ (driver `mariadb-java-client`) |
| **Bảo mật** | Spring Security | — | Xác thực, phân quyền, method-level security |
| **Token** | JJWT (io.jsonwebtoken) | 0.12.6 | Phát hành & xác thực JWT (HS512) |
| **Kiểm tra dữ liệu** | Jakarta Bean Validation | — | Kiểm tra dữ liệu đầu vào (`@Valid`) |
| **Ánh xạ** | MapStruct | 1.6.3 | Ánh xạ DTO ↔ Entity tại compile-time (`UserMapper`) |
| **Tài liệu API** | SpringDoc OpenAPI (Swagger UI) | 2.8.6 | Tài liệu API tương tác tại `/swagger-ui.html` |
| **Giám sát** | Spring Boot Actuator | — | Health/info/metrics tại `/actuator/**` |
| **Email** | Spring Boot Starter Mail | — | Gửi email giao dịch (`EmailService`) |
| **Giảm boilerplate** | Lombok | — | Giảm code lặp (getter/setter/builder) |
| **Trải nghiệm dev** | Spring Boot DevTools | — | Tự reload khi phát triển |
| **Build** | Maven (`mvnw` wrapper) | 3.9.x | Quản lý phụ thuộc & đóng gói |
| **Kiểm thử** | JUnit 5 + Spring Security Test | — | Kiểm thử tích hợp |

---

## ✨ Tính năng

- 🔐 **Xác thực JWT** — access token stateless (HS512), không lưu session phía server.
- 🔄 **Xoay vòng refresh-token** — refresh token dạng opaque lưu trong DB, cho phép đăng xuất & thu hồi thật; mỗi lần làm mới sẽ thay token cũ.
- 👥 **RBAC theo quyền hạn** — người dùng → vai trò → quyền; kiểm tra bằng `hasRole(...)` *và* `hasAuthority(PERMISSION)`.
- 🧾 **Vỏ API thống nhất** — mọi phản hồi (thành công hay lỗi) đều cùng cấu trúc `{ code, message, data, timestamp }`.
- 🧯 **Xử lý ngoại lệ tập trung** — một `@RestControllerAdvice` ánh xạ mọi ngoại lệ về mã lỗi nghiệp vụ ổn định.
- 🕵️ **JPA auditing** — tự động điền `createdAt / updatedAt / createdBy / updatedBy`.
- 🔎 **Tìm kiếm động & phân trang** — JPA Specification kèm sắp xếp an toàn (whitelist).
- 🌱 **Seed dữ liệu idempotent** — tạo sẵn quyền mặc định, vai trò `ADMIN`/`USER` và tài khoản admin khi khởi động.
- ✉️ **Dịch vụ email** — ẩn sau interface để dễ thay thế (DIP).
- 🌍 **Đã cấu hình CORS** & đăng nhập bằng **username hoặc email**.

---

## 🏗 Kiến trúc

Kiến trúc phân tầng cổ điển với luồng phụ thuộc một chiều. Tầng web không bao giờ chạm trực tiếp vào tầng lưu trữ; service phụ thuộc vào **interface**, không phụ thuộc implementation.

```text
HTTP Request
     │
     ▼
┌─────────────┐   @Valid DTO     ┌──────────────┐   Interface     ┌──────────────┐
│ Controller  │ ───────────────▶ │   Service    │ ──────────────▶ │  Repository  │
│  (REST API) │                  │ (impl)       │                 │ (Spring Data)│
└─────────────┘                  └──────────────┘                 └──────────────┘
     ▲                                  │                                 │
     │ ApiResponse<T>                   │ Mapper / Specification          ▼
     │                                  ▼                          ┌──────────────┐
┌──────────────────────┐        ┌──────────────┐                  │   MariaDB    │
│ GlobalExceptionHandler│◀──────│  AppException │                  └──────────────┘
└──────────────────────┘        └──────────────┘
            ▲
            │  Chuỗi filter bảo mật (JWT)  ──▶  EntryPoint / AccessDeniedHandler
```

---

## 📂 Cấu trúc dự án

```text
src/main/java/vn/springboot
├── Application.java                  # Điểm khởi động
├── common
│   ├── entity/BaseEntity.java        # Trường audit (id, created/updated by/at)
│   ├── exception/                    # ErrorCode, AppException, GlobalExceptionHandler
│   └── response/ApiResponse.java     # Lớp vỏ phản hồi chung
├── config
│   ├── DataInitializer.java          # Seed vai trò/quyền/admin
│   └── JpaAuditingConfig.java        # AuditorAware (người dùng hiện tại)
├── controller                        # AuthController, UserController
├── dto
│   ├── request/                      # Login/Register/RefreshToken/UserSearch
│   └── response/                     # Phản hồi Auth/User/Page
├── entity/user                       # User, Role, Permission, RefreshToken
├── mapper/UserMapper.java            # Entity → DTO
├── repository                        # Spring Data repository + Specification
├── security
│   ├── SecurityConfig.java           # Filter chain, CORS, password encoder
│   ├── jwt/                          # JwtService, JwtAuthenticationFilter
│   ├── CustomUserDetails(Service)    # UserEntity → UserDetails của Spring Security
│   └── *Handler / *EntryPoint        # Phản hồi JSON 401/403 thống nhất
└── service                           # AuthService, UserService, EmailService (+ impl)
```

---

## 🚦 Bắt đầu

### 1. Yêu cầu

- **JDK 21+**
- **MariaDB** (hoặc MySQL) đang chạy cục bộ
- Không cần cài Maven — dùng wrapper `./mvnw` có sẵn

### 2. Tạo CSDL

```sql
CREATE DATABASE dev_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> Hibernate `ddl-auto: update` sẽ tự tạo bảng ở lần chạy đầu tiên.

### 3. Chạy ứng dụng

```bash
# Phát triển
./mvnw spring-boot:run

# Đóng gói jar chạy được
./mvnw clean package
java -jar target/spring-boot-0.0.1-SNAPSHOT.jar
```

Ứng dụng khởi động tại **`http://localhost:8080`**.

### 4. Tài khoản admin mặc định

| Trường | Giá trị |
|---|---|
| Username | `admin` |
| Mật khẩu | `admin123` |
| Email | `admin@springboot.vn` |

> ⚠️ **Hãy đổi mật khẩu này trước khi triển khai!**

### 5. Chạy bằng Docker

```bash
docker build -t spring-boot-app .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mariadb://host.docker.internal:3306/dev_db" \
  -e DB_USERNAME=root -e DB_PASSWORD=root \
  -e APP_JWT_SECRET="<secret-base64-512bit>" \
  spring-boot-app
```

---

## ⚙️ Cấu hình

Mọi cấu hình nằm trong `src/main/resources/application.yaml` và có thể ghi đè bằng biến môi trường.

| Biến môi trường | Mặc định | Mô tả |
|---|---|---|
| `DB_URL` | `jdbc:mariadb://localhost:3306/dev_db` | Chuỗi kết nối JDBC |
| `DB_USERNAME` | `root` | User CSDL |
| `DB_PASSWORD` | `root` | Mật khẩu CSDL |
| `APP_JWT_SECRET` | *(mặc định dev)* | Khoá ký 512-bit mã hoá Base64 — **bắt buộc ghi đè ở production** |
| `app.jwt.access-token-expiration` | `3600000` | Thời hạn access token (ms) — 1 giờ |
| `app.jwt.refresh-token-expiration` | `604800000` | Thời hạn refresh token (ms) — 7 ngày |
| `app.mail.from` | `no-reply@springboot.vn` | Địa chỉ "From" cho email gửi đi |
| `app.init.enabled` | `true` | Bật/tắt seed dữ liệu khi khởi động |
| `app.init.admin-username` / `-email` / `-password` | `admin` / … | Thông tin admin được seed |

> 🔒 **Lưu ý production:** hãy sinh JWT secret mới và truyền qua `APP_JWT_SECRET`; đừng commit secret thật.

---

## 🔌 Tài liệu API

Đường dẫn gốc: **`/api`** · Tài liệu tương tác: **`/swagger-ui.html`** · Health: **`/actuator/health`**

### 🔑 Xác thực — `/api/auth`

| Method | Endpoint | Auth | Mô tả |
|---|---|:---:|---|
| `POST` | `/register` | ❌ | Đăng ký người dùng mới |
| `POST` | `/login` | ❌ | Đăng nhập (username **hoặc** email) → access + refresh token |
| `POST` | `/refresh` | ❌ | Làm mới & xoay refresh token → cặp token mới |
| `POST` | `/logout` | ✅ | Thu hồi refresh token |
| `GET` | `/me` | ✅ | Lấy người dùng đang đăng nhập |

### 👤 Người dùng — `/api/users`

| Method | Endpoint | Quyền | Mô tả |
|---|---|:---:|---|
| `GET` | `/` | `USER_READ` | Tìm kiếm & phân trang người dùng |
| `GET` | `/{id}` | `USER_READ` | Lấy người dùng theo id |

### 📥 Ví dụ request

**Đăng ký**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@mail.com","password":"secret123","fullName":"John Doe"}'
```

**Đăng nhập**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Gọi endpoint cần xác thực**
```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

---

## 🛡 Mô hình bảo mật

1. **Đăng nhập** xác thực thông tin qua `AuthenticationManager` + `BCryptPasswordEncoder`.
2. Server phát hành **JWT access token** (HS512) thời gian ngắn và **refresh token** thời gian dài lưu trong DB.
3. Mỗi request đi qua `JwtAuthenticationFilter` để xác thực token và nạp `SecurityContext`.
4. **Phân quyền** thực thi bằng `@PreAuthorize("hasAuthority('USER_READ')")` — quyền được suy ra từ vai trò của người dùng.
5. Khi xác thực thất bại, trả về JSON nhất quán: **401** (`JwtAuthenticationEntryPoint`) hoặc **403** (`CustomAccessDeniedHandler`).

**Vai trò & quyền mặc định**

| Vai trò | Quyền |
|---|---|
| `ADMIN` | `USER_READ`, `USER_WRITE`, `USER_DELETE`, `ROLE_READ`, `ROLE_WRITE` |
| `USER` | `USER_READ` |

---

## 📦 Định dạng phản hồi

Mọi phản hồi dùng chung một lớp vỏ. Mã `1000` nghĩa là thành công.

**Thành công**
```json
{
  "code": 1000,
  "message": "Login successful",
  "data": { "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer", "expiresIn": 3600 },
  "timestamp": "2026-06-05T10:00:00Z"
}
```

**Lỗi**
```json
{
  "code": 4011,
  "message": "Invalid username or password",
  "data": null,
  "timestamp": "2026-06-05T10:00:00Z"
}
```

**Dải mã lỗi**

| Dải | Ý nghĩa |
|---|---|
| `1000` | Thành công |
| `4000–4001` | Bad request / validation |
| `4010–4014` | Xác thực |
| `4030` | Phân quyền (403) |
| `4040–4043` | Không tìm thấy |
| `4090–4093` | Xung đột (đã tồn tại, token bị thu hồi…) |
| `9000–9999` | Lỗi server / nội bộ |

---

## 📄 Giấy phép

Phát hành theo **Giấy phép MIT**. Tự do sử dụng, chỉnh sửa và phân phối.

---

<div align="center">

**Xây dựng bằng ❤️ với Spring Boot**

⭐ Nếu dự án hữu ích, hãy tặng một sao nhé!

</div>
