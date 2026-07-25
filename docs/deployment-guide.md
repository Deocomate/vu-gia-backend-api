# 🚀 Gốm Sứ Vũ Gia — Deployment & Operations Guide

> **Tài liệu Hướng dẫn Triển khai**: Chi tiết các bước cài đặt, cấu hình môi trường, chạy Docker Compose, quản lý CSDL MySQL, khởi tạo Flyway migration và giám sát vận hành dự án `vu-gia-backend-api`.

---

## 1. Yêu cầu Hệ thống (System Requirements)

- **Môi trường cục bộ (Local Dev)**:
  - Java JDK 21 trở lên
  - Maven 3.8+ (hoặc sử dụng wrapper `./mvnw`)
  - CSDL MySQL 8.0+
- **Môi trường Container (Production / Staging)**:
  - Docker 24.0+
  - Docker Compose 2.20+
  - Dung lượng đĩa tối thiểu: 10 GB (cho MySQL data và đĩa đệm lưu ảnh `./data`)
  - RAM tối thiểu: 2 GB

---

## 2. Triển khai bằng Docker Compose (Khuyến nghị)

Dự án đã tích hợp sẵn tệp `docker-compose.yml` định nghĩa môi trường tự chứa (Self-contained) gồm **MySQL 8.0** và **Spring Boot App**.

### Các bước triển khai:

1. **Tạo tệp cấu hình biến môi trường `.env`**:
   ```bash
   cp .env.example .env
   ```

2. **Chỉnh sửa cấu hình `.env` cho môi trường Production**:
   ```env
   # Database Configuration
   DB_URL=jdbc:mysql://mysql:3306/db_vu_gia_fullstack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   DB_USERNAME=root
   DB_PASSWORD=YourStrongProductionPassword123!

   # Security JWT Secret (Tạo chuỗi ngẫu nhiên 512-bit Base64)
   APP_JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

   # Public Domain config cho lưu trữ tệp tin
   APP_STORAGE_ROOT=/app/data
   APP_STORAGE_PUBLIC_URL=https://api.gomsuvugia.vn

   # Mail Server (SMTP)
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   APP_MAIL_FROM=your-email@gmail.com

   # Webhook SePay Secret
   SEPAY_WEBHOOK_SECRET=your_sepay_webhook_secret_here
   ```

3. **Khởi chạy hệ thống**:
   ```bash
   docker compose up -d --build
   ```

4. **Kiểm tra trạng thái container và logs**:
   ```bash
   docker compose ps
   docker compose logs -f app
   ```
   *Khi log hiển thị `Successfully applied ... migrations to schema db_vu_gia_fullstack` và `Started Application in ... seconds`, ứng dụng đã sẵn sàng.*

---

## 3. Chạy trực tiếp trên máy cục bộ (Local Development)

Nếu bạn muốn chạy ứng dụng trực tiếp bằng Maven Wrapper trên máy dev:

1. **Khởi tạo cơ sở dữ liệu MySQL**:
   Tạo CSDL tên `db_vu_gia_fullstack` trên MySQL local (Port 3306).

2. **Chạy ứng dụng bằng lệnh Maven**:
   ```bash
   # Linux / macOS
   DB_URL="jdbc:mysql://localhost:3306/db_vu_gia_fullstack?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false" \
   DB_USERNAME="root" \
   DB_PASSWORD="your_password" \
   ./mvnw spring-boot:run

   # Windows (PowerShell)
   $env:DB_URL="jdbc:mysql://localhost:3306/db_vu_gia_fullstack?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_password"
   .\mvnw.cmd spring-boot:run
   ```

3. **Đóng gói tập tin JAR chạy sản xuất**:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/spring-boot-0.0.1-SNAPSHOT.jar
   ```

---

## 4. Quản lý Cơ sở dữ liệu & Flyway Migration

- Schema được định nghĩa tự động tại `src/main/resources/db/migration/V1__init_db.sql`.
- Dữ liệu mẫu được tự động nạp tại `src/main/resources/db/seed/V2__seed_db.sql`.
- Tài khoản SuperAdmin mặc định sau khi seed:
  - **Username**: `admin`
  - **Password**: `admin123`
  - **Email**: `admin@gmail.com`
  *(⚠️ Lưu ý: Bắt buộc đổi mật khẩu tài khoản admin ngay sau khi triển khai sản xuất).*

---

## 5. Giám sát & Kiểm tra Sức khỏe Ứng dụng (Health Checks & Monitoring)

- **Kiểm tra sức khỏe ứng dụng (Health Check)**:
  `GET http://localhost:8080/actuator/health`
  ```json
  { "status": "UP" }
  ```
- **Tài liệu Swagger UI Tương tác**:
  `http://localhost:8080/swagger-ui.html`
- **OpenAPI Schema JSON**:
  `http://localhost:8080/v3/api-docs`

---

## 6. Tài liệu Hướng dẫn Triển khai Nâng cao

- [`MYSQL_DOCKER_PRODUCTION_SETUP.md`](MYSQL_DOCKER_PRODUCTION_SETUP.md) — Triển khai cụm CSDL MySQL nâng cao cho Production
- [`MINIO_DOCKER_PRODUCTION_SETUP.md`](MINIO_DOCKER_PRODUCTION_SETUP.md) — Triển khai MinIO Object Storage (nếu chuyển từ local storage sang MinIO S3)
- [`RUN_AND_SEED.md`](RUN_AND_SEED.md) — Hướng dẫn khởi chạy & seed dữ liệu chi tiết
