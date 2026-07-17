# CHẠY DỰ ÁN & AUTO-SEED (Flyway + MinIO)

Hướng dẫn chạy `vu-gia` để **tự động migrate schema + seed dữ liệu**, và cách upload ảnh vào MinIO.

## 1. Cơ chế tự migrate + seed

- Dùng **Flyway** (đã thêm vào `pom.xml`). Khi app khởi động, Flyway chạy theo thứ tự version:
  1. `src/main/resources/db/migration/V1__init_db.sql` — tạo toàn bộ schema.
  2. `src/main/resources/db/seed/V2__seed_db.sql` — seed dữ liệu mẫu (catalog, news, banner, gallery, pages…).
- `spring.jpa.hibernate.ddl-auto=validate` → Hibernate chỉ kiểm tra entity khớp schema, **không tự sửa DB**.
- Mỗi migration chỉ chạy **1 lần**, được ghi vào bảng `flyway_schema_history`. Chạy lại app sẽ không seed lại.
- DB đã có sẵn bảng (chưa có lịch sử Flyway) → được **baseline ở version 1** (bỏ qua V1, không xoá dữ liệu), rồi chạy V2 seed.
- Admin mặc định do `DataInitializer` tạo: **`admin` / `admin123`** (SUPERADMIN) nếu chưa tồn tại.

> ⚠️ Seed V2 dùng `TRUNCATE` các bảng catalog (products, product_categories, news, banners, showrooms,
> gallery_images, product_images) rồi INSERT lại. Bảng `users` / `orders` / `cart_items` **không** bị đụng.

## 2. Ảnh & bucket MinIO

Có **2 bucket**:

| Bucket | Dùng cho | Nội dung |
|---|---|---|
| `assets` | Ảnh nội dung/giao diện của seed | Thư mục `./assets/images/**` (products, gallery, about, news, showroom, nha-xuong…) |
| `products` | Ảnh sản phẩm upload qua app | App tự tạo khi upload lần đầu (MediaController) |

### Quy ước đường dẫn (QUAN TRỌNG)

- **DB chỉ lưu HẬU TỐ (đường dẫn tương đối)**, ví dụ: `assets/images/gallery/gallery-1.jpg`.
  **KHÔNG** lưu host. **FE tự cộng tiền tố** (base URL của MinIO), ví dụ:
  `VITE_IMG_BASE = http://localhost:9000` → ảnh đầy đủ = `http://localhost:9000/` + `assets/images/gallery/gallery-1.jpg`.
- Đường dẫn trong DB **khớp đúng cấu trúc thư mục** trong `./assets/` → upload nguyên thư mục là chạy được.
- Toàn bộ ảnh seed đều là **file có thật** trong `./assets/images/**` (không bịa ảnh mới).

### Bucket tự động public

Khi app khởi động, `MinioBucketInitializer` **tự tạo + set public-read** cho cả 2 bucket `assets` và `products`
(kể cả bucket bạn đã tạo tay trước đó — nó vá lại policy). **Bạn KHÔNG cần chỉnh Access Policy thủ công.**
Nếu MinIO chưa chạy thì app chỉ log warning và vẫn boot bình thường.

### Cách upload (tự động — mặc định)

`docker-compose.yml` có service `minio-init` (image `minio/mc`, one-shot) tự chạy sau khi `minio` healthy:
1. Tạo bucket `assets` + `products` (`mc mb --ignore-existing`).
2. Set cả 2 bucket public-read (`mc anonymous set download`).
3. Mirror `./assets/images` → `myminio/assets/images` (`mc mirror --overwrite`).

```bash
docker compose up -d minio minio-init
docker logs vugia-minio-init   # thấy "minio-init: done" là xong
curl -I http://localhost:9000/assets/images/home/hero-image-1-top.png   # phải 200
```

Idempotent — chạy lại `docker compose up -d minio-init` nhiều lần không hỏng, lần sau gần như no-op
(chỉ mirror phần thay đổi). Không cần thao tác tay qua console cho luồng dev bình thường.

### Cách upload thủ công (dự phòng, MinIO Console)

Chỉ cần khi muốn thêm ảnh ngoài `./assets/images` mà không muốn sửa lại thư mục nguồn:

1. Chạy app 1 lần để bucket `assets` được tạo & set public (hoặc tự tạo bucket `assets` trong console cũng được).
2. Mở console: Docker → **http://localhost:9001** (local → **http://localhost:9000/minio**), login `minioadmin` / `minioadmin123`.
3. Vào bucket `assets` → **Upload → Upload Folder**, chọn thư mục **`images`** (nằm trong `./assets/`).
   → Object key phải thành `images/gallery/gallery-1.jpg`, `images/products/product-image-thumb.png`…
   → URL đầy đủ = `http://localhost:9000/assets/images/gallery/gallery-1.jpg` = tiền tố (`http://localhost:9000/`) + hậu tố trong DB (`assets/images/gallery/gallery-1.jpg`). ✔

> App **không cần** MinIO để boot/seed (seed chỉ lưu chuỗi đường dẫn). Ảnh chỉ hiển thị trên trình duyệt
> sau khi bucket `assets` có ảnh (tự động qua `minio-init`, hoặc thủ công qua console).

Các nhóm ảnh seed dùng (đều có trong `./assets/images/`): `products/`, `product-detail/`, `gallery/`,
`home/` (home-new, hero-image), `about/`, `nha-xuong/` (slider/banner), `customer-services/` (chính sách).

## 3. Chạy bằng Spring (máy local)

Yêu cầu: MySQL + MinIO đang chạy (hoặc chỉ MySQL nếu chưa cần ảnh).

```bash
# 1) Chạy app — Flyway tự migrate + seed khi khởi động
./mvnw spring-boot:run
```

- DB mặc định: `jdbc:mysql://localhost:3306/dev_db` (đổi qua biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- **DB rỗng/mới**: thêm `?createDatabaseIfNotExist=true` vào URL để MySQL tự tạo database:
  ```bash
  DB_URL="jdbc:mysql://localhost:3306/dev_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false" ./mvnw spring-boot:run
  ```
- Muốn seed lại từ đầu hoàn toàn: xoá database rồi chạy lại (`DROP DATABASE dev_db; CREATE DATABASE dev_db;`).

## 4. Chạy bằng Docker (1 lệnh, đủ MySQL + MinIO + app)

Đã có sẵn [`docker-compose.yml`](../docker-compose.yml):

```bash
# Build image app + dựng MySQL + MinIO + app; app tự migrate + seed
docker compose up -d --build

# Xem log app (thấy Flyway "Successfully applied ... migrations")
docker compose logs -f app
```

Sau khi lên:
- API: **http://localhost:8080** (Swagger: `/swagger-ui.html`)
- MinIO Console: **http://localhost:9001** (login `minioadmin` / `minioadmin123`) → tạo bucket `assets` + upload `./images` (mục 2).

Dừng / xoá:
```bash
docker compose down          # dừng, giữ dữ liệu (volume)
docker compose down -v       # dừng + xoá sạch dữ liệu MySQL/MinIO (seed lại từ đầu lần sau)
```

## 5. Lưu ý quan trọng

- **Sửa seed sau khi đã chạy**: Flyway lưu checksum của migration đã áp dụng. Nếu sửa `V2__seed_db.sql`
  sau khi nó đã chạy trên 1 DB → lần sau sẽ báo *checksum mismatch*. Khi đó: tạo migration mới `V3__...sql`,
  hoặc chạy `flyway repair`, hoặc (dev) `docker compose down -v` / drop DB để seed lại.
- **Prod**: URL ảnh trong seed đang là `http://localhost:9000/...`. Khi deploy, đổi host cho khớp
  `MINIO_PUBLIC_URL` thật (CDN/domain). Có thể find-replace trong seed hoặc seed lại với host đúng.
- **Test**: `./mvnw test` chạy `ApplicationTests.contextLoads` (dùng `@SpringBootTest`) sẽ kích hoạt Flyway
  trên DB cấu hình. Chạy test unit riêng bằng `-Dtest=Xxx` để không đụng DB thật.
