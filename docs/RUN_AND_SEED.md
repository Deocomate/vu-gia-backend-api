# CHẠY DỰ ÁN & AUTO-SEED (Flyway + Local File Storage)

Hướng dẫn chạy `vu-gia` để **tự động migrate schema + seed dữ liệu**, và cách ảnh hoạt động sau khi
bỏ MinIO (lưu local filesystem).

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

## 2. Ảnh (KHÔNG còn MinIO — 2 nguồn tách biệt)

Kể từ khi gỡ MinIO, ảnh trong hệ thống đến từ **2 nguồn hoàn toàn khác nhau**, đừng nhầm lẫn:

| Nguồn | Ví dụ giá trị trong DB | Ai phục vụ | Cần setup gì? |
|---|---|---|---|
| **Ảnh seed** (demo: catalog/gallery/news/home…) | `assets/images/products/product-image-thumb.png` (đường dẫn tương đối, KHÔNG có `/` đầu) | **Frontend** — file có thật trong `vu-gia-client/public/assets/images/**`, Next.js serve thẳng tại `/assets/...` | **Không cần gì** — không cần backend chạy, không cần upload, chỉ cần frontend có sẵn thư mục `public/assets` (đã có trong repo) |
| **Ảnh upload qua admin** (`POST /api/media/upload`, gallery sản phẩm…) | `/files/products/ab12cd34.jpg` (bắt đầu bằng `/files`) | **Backend** — ghi vào `app.storage.root` (mặc định `./data`, Docker `/app/data`), serve qua `GET /files/**` | Backend chạy + `app.storage.*` cấu hình đúng (mặc định đã đúng cho dev) |

Chi tiết cơ chế 2 nguồn này (kể cả cách JSON API tự ghép/cắt domain — annotation `@StorageUrl`)
xem **[FILE_STORAGE_API.md](./FILE_STORAGE_API.md)**.

**Vì sao seed KHÔNG cần backend/bucket nào cả**: `formatImageUrl()` phía FE (`src/lib/media.js`)
nhận diện đường dẫn seed (không bắt đầu bằng `http`/`/`) và tự resolve **same-origin** (`/${url}`) —
tức là Next.js tự phục vụ file tĩnh trong `public/assets/`, không hề gọi ra backend. Ảnh backend-upload
thì ngược lại: JSON trả về **luôn là absolute URL sẵn** (`http://<host>/files/...`), FE chỉ cần render thẳng.

## 3. Chạy bằng Spring (máy local)

Yêu cầu: MySQL đang chạy (ảnh không cần setup gì thêm — xem mục 2).

```bash
# 1) Chạy app — Flyway tự migrate + seed khi khởi động
./mvnw spring-boot:run
```

- DB mặc định: `jdbc:mysql://localhost:3307/db_vu_gia_fullstack` (đổi qua biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — xem `application.yaml`).
- **DB rỗng/mới**: URL mặc định đã có `createDatabaseIfNotExist=true` nên MySQL tự tạo database.
- Muốn seed lại từ đầu hoàn toàn: xoá database rồi chạy lại (`DROP DATABASE db_vu_gia_fullstack;`).
- Ảnh upload thủ công lúc dev sẽ nằm ở `vu-gia-backend-api/data/` (thư mục này gitignore, tự tạo khi cần).

## 4. Chạy bằng Docker (1 lệnh, MySQL + app)

`docker-compose.yml` chạy chung MySQL (`mysql:8.4`) + app trong 1 stack tự chứa — xem
**[deployment-guide.md](../../docs/deployment-guide.md)** ở repo root cho hướng dẫn đầy đủ (kèm cả
frontend, biến môi trường, test full-stack qua Docker Desktop). Tóm tắt nhanh:

```bash
cp .env.example .env   # điền secret thật, xem .env.example

# Build image app + dựng MySQL + app; app tự migrate + seed
docker compose up -d --build

# Xem log app (thấy Flyway "Successfully applied ... migrations")
docker compose logs -f app
```

Sau khi lên:
- API: **http://localhost:8080** (Swagger: `/swagger-ui.html`)
- MySQL: **localhost:3306** (expose ra host cho DBeaver/dev, xem `docker-compose.yml`)
- Ảnh upload nằm trên named volume `upload-data` (mount vào `/app/data`) — sống qua `docker compose restart`.

Dừng / xoá:
```bash
docker compose down          # dừng, giữ dữ liệu (volume)
docker compose down -v       # dừng + xoá sạch dữ liệu MySQL + ảnh upload (seed lại từ đầu lần sau)
```

## 5. Lưu ý quan trọng

- **Sửa seed sau khi đã chạy**: Flyway lưu checksum của migration đã áp dụng. Nếu sửa `V2__seed_db.sql`
  sau khi nó đã chạy trên 1 DB → lần sau sẽ báo *checksum mismatch*. Khi đó: tạo migration mới `V3__...sql`,
  hoặc chạy `flyway repair`, hoặc (dev) `docker compose down -v` / drop DB để seed lại.
- **Prod**: URL ảnh backend-upload được ghép động lúc trả JSON (không "bake" vào DB) — chỉ cần set đúng
  `APP_STORAGE_PUBLIC_URL` ở env là đủ, **không** cần sửa/seed lại DB khi đổi domain. Ảnh seed
  (`assets/images/...`) hoàn toàn không phụ thuộc domain backend.
- **Test**: `./mvnw test` chạy `ApplicationTests.contextLoads` (dùng `@SpringBootTest`) sẽ kích hoạt Flyway
  trên DB cấu hình. Chạy test unit riêng bằng `-Dtest=Xxx` để không đụng DB thật.
