# FILE STORAGE API — Upload / phục vụ ảnh

Tài liệu bàn giao FE cho cơ chế lưu file (thay MinIO bằng local filesystem).

## 1. Tổng quan

- File upload (thumbnail, gallery, SEO image, avatar, banner…) được lưu **local filesystem**
  trên server tại `app.storage.root` (mặc định `./data`, Docker `/app/data`), phục vụ qua
  `GET /files/**` (public, không cần JWT).
- **DB lưu relative path** (`/files/products/ab12.jpg`), **KHÔNG** lưu domain/host — đổi domain
  server không làm hỏng ảnh cũ.
- **FE không cần đổi gì**: field JSON của các endpoint dưới đây vẫn nhận/gửi **absolute URL** như
  trước (`http://host/files/...`). Việc quy đổi absolute ↔ relative xảy ra tự động ở tầng JSON
  (annotation `@StorageUrl` trên field DTO) — đọc thì ghép base URL vào, ghi thì cắt base URL ra.
- Field không phải ảnh do storage quản lý (URL ngoài như avatar Google, seed `assets/images/...`)
  đi qua nguyên vẹn, không bị đụng.

## 2. Cấu hình (`app.storage.*`)

| Property | Env var | Default | Ý nghĩa |
|---|---|---|---|
| `app.storage.root` | `APP_STORAGE_ROOT` | `./data` | Thư mục gốc lưu file trên đĩa |
| `app.storage.public-url` | `APP_STORAGE_PUBLIC_URL` | `http://localhost:8080` | Base URL công khai để ghép link đọc |
| `app.storage.url-prefix` | (cố định) | `/files` | Path phục vụ file |

`APP_STORAGE_PUBLIC_URL` **bắt buộc set đúng domain thật ở production** (không để `localhost`) —
nếu sai, chỉ ảnh hưởng **response hiện tại** (link ảnh trả cho FE), không hỏng dữ liệu, vì DB luôn
lưu relative path.

## 3. Upload ảnh

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/media/upload` | `ADMIN`/`SUPERADMIN` | Upload 1 ảnh |
| POST | `/api/media/upload-multiple` | `ADMIN`/`SUPERADMIN` | Upload nhiều ảnh (multi-select) |
| POST | `/api/products/{productId}/images` | `ADMIN`/`SUPERADMIN` | Upload ảnh gallery gắn thẳng vào sản phẩm |

Request: `multipart/form-data`, field `file` (single) hoặc nhiều field `files` (multiple), tham số
`folder` optional (mặc định `misc`).

**Loại ảnh cho phép** (whitelist theo content-type, không theo đuôi file gốc):
`image/jpeg`, `image/png`, `image/webp`, `image/gif`. Loại khác (kể cả `image/svg+xml`) → lỗi
`4002 INVALID_FILE`.

Response `200` (`/api/media/upload`):

```json
{
  "code": 1000,
  "message": "Uploaded successfully",
  "data": { "url": "http://localhost:8080/files/misc/3f9a2b.jpg" }
}
```

Response `200` (`/api/media/upload-multiple`): `data` là mảng cùng shape, đúng thứ tự file đã gửi.

## 4. Phục vụ file

`GET /files/**` — public, không cần JWT, cache 7 ngày (`Cache-Control: public, max-age=604800`).
Response luôn kèm header `X-Content-Type-Options: nosniff`.

## 5. Các field JSON dùng URL ảnh (annotate `@StorageUrl`)

Các field dưới đây nhận/trả **absolute URL** ở tầng API như bình thường (backend tự resolve):

| Domain | Response field | Request field |
|---|---|---|
| Media | `UploadResponse.url` | — |
| Product | `ProductResponse.thumb`, `.seoImage` | `ProductCreateRequest`/`UpdateRequest.thumb`, `.seoImage` |
| Product image (gallery) | `ProductImageResponse.url` | `ProductImageRequest.url` |
| Product category | `ProductCategoryResponse.thumb`, `.seoImage` | `ProductCategoryCreateRequest`/`UpdateRequest.thumb`, `.seoImage` |
| News | `NewsResponse.thumb`, `.seoImage` | `NewsCreateRequest`/`UpdateRequest.thumb`, `.seoImage` |
| Page (CMS) | `PageDetailResponse.heroImage`, `.seoImage` | `PageCreateRequest`/`UpdateRequest.heroImage`, `.seoImage` |
| Banner | `BannerResponse.imageUrl` | `BannerCreateRequest`/`UpdateRequest.imageUrl` |
| Gallery image | `GalleryImageResponse.imageUrl` | `GalleryImageCreateRequest`/`UpdateRequest.imageUrl` |
| User | `UserResponse.avatar` | — (avatar chỉ set qua OAuth, không có request field) |
| Cart | `CartItemResponse.productThumb` | — (đọc lại từ Product, không phải input) |

Field không nằm trong bảng trên (vd `PaymentInfoResponse.qrImageUrl` — ảnh VietQR bên ngoài,
`ShowroomResponse.mapEmbedUrl` — Google Maps embed) **không** thuộc storage này, giữ nguyên như cũ.

## 6. Docker / biến môi trường

`docker-compose.yml` chạy chung MySQL + app. Ảnh upload nằm trên named volume `upload-data` (mount
vào `/app/data`) — sống qua `docker compose restart`/`down` (không dùng `down -v`). Xem
`.env.example` để biết danh sách biến cần set, đặc biệt `APP_STORAGE_PUBLIC_URL`.
