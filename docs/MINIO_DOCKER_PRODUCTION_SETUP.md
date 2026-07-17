# MinIO Production Setup trên Coolify

Hướng dẫn tạo MinIO làm **managed resource riêng** trên Coolify (tách khỏi `docker-compose.yml`
của app — xem [`../docker-compose.yml`](../docker-compose.yml)). Theo quyết định đã chốt:
Console (port 9001) **có** public domain riêng để thao tác upload/quản lý ảnh thủ công khi cần.

## 1. Tạo resource MinIO trên Coolify

1. Coolify Dashboard → project chứa app `vu-gia` → **+ New Resource** → **Services** (one-click) →
   tìm **MinIO**. Nếu bản Coolify đang dùng không có sẵn template, tạo bằng **Docker Compose**
   custom resource với image `minio/minio:latest`, tương tự service `minio` cũ trong compose dev
   (`command: server /data --console-address ":9001"`).
2. Set:
   - `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD`: sinh mới, **không** tái dùng `minioadmin`/`minioadmin123`
     của dev.
   - Volume persistent cho `/data` (Coolify tự tạo nếu dùng one-click template; tự khai `volumes:`
     nếu dùng Compose custom).
3. Deploy resource.

## 2. Domain cho API (port 9000) và Console (port 9001)

MinIO expose 2 cổng khác nhau — cần **2 domain riêng**:

| Cổng | Dùng cho | Domain |
|---|---|---|
| `9000` | S3 API — app upload/đọc ảnh, browser tải ảnh trực tiếp | vd `cdn.vugia.com` |
| `9001` | Console UI — đăng nhập quản lý bucket/object thủ công | vd `minio-console.vugia.com` |

Trong Coolify: resource MinIO → **Domains**, gán FQDN riêng cho từng port, bật **HTTPS** (Let's
Encrypt tự động qua Traefik) cho cả 2. **Bắt buộc HTTPS** cho domain API vì URL ảnh sẽ nhúng trực
tiếp vào response JSON của app và hiển thị trên web công khai.

> Console có domain public theo quyết định đã chọn — **bảo vệ bằng password mạnh** (bước 1) vì đây
> là UI quản trị có quyền xoá/ghi đè toàn bộ bucket. Cân nhắc thêm IP allowlist ở Coolify/Traefik
> nếu chỉ admin nội bộ cần truy cập.

## 3. Set biến môi trường cho app

Trong app resource → **Environment Variables**:

```bash
# Server-to-server (app gọi MinIO): dùng internal Coolify host nếu app + MinIO cùng server/network
# (nhanh hơn, không qua internet); dùng domain HTTPS public nếu khác server.
MINIO_URL=http://<internal-host>:9000

# Trình duyệt tải ảnh trực tiếp — LUÔN LÀ domain public HTTPS đã gán ở mục 2.
MINIO_PUBLIC_URL=https://cdn.vugia.com

MINIO_ACCESS_KEY=<MINIO_ROOT_USER hoặc access key riêng — xem mục 5>
MINIO_SECRET_KEY=<MINIO_ROOT_PASSWORD hoặc secret key riêng>

MINIO_BUCKET_PRODUCT=products
MINIO_BUCKET_ASSET=assets
```

`MINIO_URL` và `MINIO_PUBLIC_URL` **khác nhau có chủ đích**: `MinioBucketInitializer` và service upload
dùng `MINIO_URL` để gọi API (nội bộ), còn `MINIO_PUBLIC_URL` chỉ dùng để build link ảnh trả về client —
xem `application.yaml:52-57`.

## 4. Bucket & public-read policy — app tự lo, không cần thao tác tay

- Khi app khởi động, `MinioBucketInitializer` tự tạo bucket `products`/`assets` (nếu chưa có) và
  tự set **public-read policy** — kể cả bucket đã tồn tại sẵn (tự vá lại policy). Không cần vào
  Console chỉnh Access Policy thủ công (chi tiết: [`RUN_AND_SEED.md`](./RUN_AND_SEED.md#bucket-tự-động-public)).
- Nếu MinIO chưa sẵn sàng lúc app boot, app chỉ log warning và vẫn chạy bình thường (không block startup).

## 5. Migrate ảnh seed từ local lên MinIO production

Seed DB (`V2__seed_db.sql`) chỉ lưu **hậu tố đường dẫn** (vd `assets/images/gallery/gallery-1.jpg`),
không lưu host — nên phải tự mirror file ảnh thật lên bucket `assets` của MinIO production:

```bash
# Cài mc (MinIO Client) trên máy có sẵn thư mục ./assets/images của repo
mc alias set vugia-prod https://cdn.vugia.com <ACCESS_KEY> <SECRET_KEY>
mc mirror --overwrite ./assets/images vugia-prod/assets/images
mc anonymous set download vugia-prod/assets   # phòng khi app chưa kịp tự set (mục 4)

# Kiểm tra
curl -I https://cdn.vugia.com/assets/images/home/hero-image-1-top.png   # phải 200
```

> Khuyến nghị: tạo **access key riêng** cho tác vụ mirror này (Console → Access Keys → Create) thay vì
> dùng thẳng root user, giới hạn quyền chỉ ghi vào bucket `assets`.

## 6. Backup

- MinIO resource → nếu Coolify hỗ trợ scheduled backup cho service này, bật backup `/data` volume.
- Với dữ liệu quan trọng (ảnh sản phẩm khách upload qua app, bucket `products`), cân nhắc thêm
  **bucket versioning** hoặc **replication** sang MinIO/S3 thứ 2 nếu yêu cầu độ tin cậy cao — vượt
  ngoài phạm vi one-click default, cấu hình qua `mc admin`/`mc replicate` nếu cần.

## 7. Checklist go-live

- [ ] MinIO resource deploy, root user/password mới (không dùng giá trị dev).
- [ ] 2 domain HTTPS riêng cho API (9000) và Console (9001).
- [ ] Console có password mạnh (public domain — rủi ro cao nếu yếu).
- [ ] `MINIO_URL` (internal) và `MINIO_PUBLIC_URL` (public HTTPS) set đúng, **không** giống nhau
  trừ khi app và MinIO khác server.
- [ ] Sau lần deploy app đầu tiên: check bucket `products`/`assets` đã tồn tại + public-read (app tự làm).
- [ ] Đã `mc mirror` ảnh seed từ `./assets/images` lên bucket `assets` prod, `curl -I` trả 200.
- [ ] Backup/volume persistence đã xác nhận.

## Câu hỏi chưa xử lý

- Coolify bản đang dùng có one-click template MinIO sẵn không, hay phải tự viết Docker Compose
  custom resource? Cần kiểm tra trực tiếp trên dashboard vì phụ thuộc version Coolify.
