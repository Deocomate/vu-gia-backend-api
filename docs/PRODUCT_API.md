# PRODUCT API

Tài liệu bàn giao FE cho domain **Sản phẩm**: Danh mục sản phẩm (`product-categories`), Sản phẩm
(`products`), Ảnh gallery sản phẩm, và Upload media (MinIO).

## 1. Tổng quan & phân quyền

- **Đọc (GET) công khai** — phục vụ storefront.
- **Ghi (POST/PUT/PATCH/DELETE) yêu cầu `ADMIN` hoặc `SUPERADMIN`** (`@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")`), gửi kèm `Authorization: Bearer <accessToken>`.
- Ảnh (thumb, seoImage, ảnh gallery) đều **lưu trên MinIO**, bucket `products`; API trả về **URL public** để lưu/hiển thị.
- Slug **tự sinh từ `name`** (bỏ dấu tiếng Việt) và đảm bảo duy nhất (thêm hậu tố `-2, -3…`). Client có thể tự gửi `slug`; nếu gửi mà trùng → lỗi 409.
- **PUT là partial update**: chỉ gửi field muốn đổi; field **không gửi (null) → giữ nguyên**, không bị ghi đè. Các ràng buộc "bắt buộc" (name/thumb/price…) chỉ áp dụng khi **tạo mới (POST)**.

## 2. Envelope & mã lỗi

Envelope chung `{code, message, data, timestamp}`, `code=1000` là thành công. Mã lỗi liên quan:

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation (kèm `data` = map `field → message`) |
| 4002 | 400 | File không hợp lệ (rỗng hoặc không phải ảnh) |
| 4003 | 413 | File upload vượt quá dung lượng cho phép (10MB) |
| 4010 | 401 | Chưa xác thực |
| 4030 | 403 | Không đủ quyền |
| 4044 | 404 | Không tìm thấy sản phẩm |
| 4045 | 404 | Không tìm thấy danh mục sản phẩm |
| 4046 | 404 | Không tìm thấy ảnh sản phẩm |
| 4094 | 409 | Slug sản phẩm đã tồn tại |
| 4095 | 409 | SKU sản phẩm đã tồn tại |
| 4096 | 409 | Slug danh mục đã tồn tại |
| 4097 | 409 | Danh mục còn sản phẩm (không xoá được) |
| 9001 | 500 | Upload file thất bại |

## 3. Quy ước phân trang / sort (mọi endpoint list)

| Param | Mặc định | Ghi chú |
|---|---|---|
| `page` | `1` | **1-based** (trang đầu = 1) |
| `size` | `10` | Tự kẹp trong `[1,100]` |
| `sortBy` | `id` | Whitelist theo từng resource (xem dưới); giá trị lạ → `id` |
| `sortDirection` | `ASC` | `ASC` / `DESC` |

`data` trả `PageResponse`: `{content, pageNumber(1-based), pageSize, totalElements, totalPages, first, last}`.

---

## 4. Danh mục sản phẩm — `/api/product-categories`

`sortBy` cho phép: `id, name, priority, createdAt`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/product-categories` | Công khai | Danh sách (filter: `name`, `slug`, `isActive`) |
| GET | `/api/product-categories/{id}` | Công khai | Chi tiết |
| GET | `/api/product-categories/slug/{slug}` | Công khai | Chi tiết theo slug (SEO) |
| POST | `/api/product-categories` | ADMIN/SUPERADMIN | Tạo mới |
| PUT | `/api/product-categories/{id}` | ADMIN/SUPERADMIN | Cập nhật |
| DELETE | `/api/product-categories/{id}` | ADMIN/SUPERADMIN | Xoá (409 nếu còn sản phẩm) |

Body tạo/cập nhật:

```json
{
  "name": "Điện thoại",       // bắt buộc, ≤ 50 ký tự
  "thumb": "https://.../a.jpg", // bắt buộc (URL ảnh, upload qua /api/media trước), ≤ 255
  "slug": "dien-thoai",        // tuỳ chọn; bỏ trống sẽ tự sinh từ name
  "priority": 10,               // tuỳ chọn (mặc định 0)
  "longContent": "…",          // tuỳ chọn (text dài)
  "des": "{\"blocks\":[]}",    // tuỳ chọn (chuỗi JSON)
  "isActive": true,             // tuỳ chọn (mặc định true)
  "seoTitle": "…", "seoDescription": "…", "seoImage": "https://…"
}
```

`ProductCategoryResponse`: `{id, name, thumb, priority, longContent, des, slug, isActive, seoTitle, seoDescription, seoImage, createdAt, updatedAt}`.

---

## 5. Sản phẩm — `/api/products`

`sortBy` cho phép: `id, name, price, priority, soldCount, createdAt`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/products` | Công khai | Danh sách + filter (xem dưới) |
| GET | `/api/products/{id}` | Công khai | Chi tiết (kèm ảnh gallery) |
| GET | `/api/products/slug/{slug}` | Công khai | Chi tiết theo slug (storefront) |
| POST | `/api/products` | ADMIN/SUPERADMIN | Tạo mới |
| PUT | `/api/products/{id}` | ADMIN/SUPERADMIN | Cập nhật |
| PATCH | `/api/products/{id}/status` | ADMIN/SUPERADMIN | Đổi trạng thái |
| PATCH | `/api/products/{id}/featured?featured=true` | ADMIN/SUPERADMIN | Bật/tắt nổi bật |
| DELETE | `/api/products/{id}` | ADMIN/SUPERADMIN | Xoá (kèm xoá ảnh trên MinIO) |

**Filter danh sách** (query params): `name` (like), `sku` (like), `type` (`SINGLE`/`COMBO`), `status`
(`DRAFT`/`PUBLISHED`/`ARCHIVED`), `productCategoryId`, `isFeatured` (bool), `minPrice`, `maxPrice`.

Ví dụ: `GET /api/products?status=PUBLISHED&productCategoryId=3&isFeatured=true&minPrice=100000&sortBy=price&sortDirection=DESC&page=1&size=20`

Body tạo:

```json
{
  "name": "iPhone 15",              // bắt buộc, ≤ 255
  "thumb": "https://.../thumb.jpg", // bắt buộc (URL, upload qua /api/media), ≤ 255
  "sku": "IP15-128",                // tuỳ chọn, ≤ 100, duy nhất
  "type": "SINGLE",                 // bắt buộc: SINGLE | COMBO
  "price": 19990000,                // bắt buộc, > 0 (VND, kiểu long)
  "compareAtPrice": 22990000,       // tuỳ chọn (giá gạch ngang)
  "isFeatured": false,              // tuỳ chọn (mặc định false)
  "status": "DRAFT",                // tuỳ chọn (mặc định DRAFT)
  "description": "{\"blocks\":[]}", // tuỳ chọn (chuỗi JSON)
  "comboProducts": "[{\"productId\":2,\"sortOrder\":1}]", // chỉ dùng khi type=COMBO (chuỗi JSON)
  "slug": "iphone-15",              // tuỳ chọn; bỏ trống tự sinh
  "priority": 0,                    // tuỳ chọn
  "productCategoryId": 3,           // bắt buộc
  "seoTitle": "…", "seoDescription": "…", "seoImage": "https://…",
  "images": [                        // tuỳ chọn — tạo luôn ảnh gallery cùng lúc
    { "url": "https://…/products/a.jpg", "priority": 0 },
    { "url": "https://…/products/b.jpg" }   // priority bỏ trống → lấy theo thứ tự trong mảng
  ]
}
```

> **Tạo sản phẩm kèm ảnh trong 1 request**: FE upload từng file ảnh qua `POST /api/media/upload`
> (mục 7) để lấy `url`, gom vào form, rồi gửi **một** request tạo sản phẩm kèm mảng `images`. Backend
> tạo sản phẩm + toàn bộ ảnh gallery trong cùng một transaction (không cần gọi API ảnh riêng sau đó).
> `priority` mỗi ảnh: nếu bỏ trống sẽ tự lấy theo vị trí trong mảng. Response tạo trả kèm `images`.
> Việc **thêm/xoá/sắp xếp ảnh sau khi đã tạo** dùng các endpoint ở mục 6.

Body cập nhật (`PUT`) — **partial**: chỉ gửi field muốn đổi; field không gửi giữ nguyên. (PUT
**không** đụng tới gallery ảnh — quản lý ảnh qua các endpoint mục 6.)

Body đổi trạng thái (`PATCH /{id}/status`): `{ "status": "PUBLISHED" }`.

`ProductResponse`:

```json
{
  "id": 12, "name": "iPhone 15", "thumb": "https://…", "sku": "IP15-128",
  "type": "SINGLE", "price": 19990000, "compareAtPrice": 22990000,
  "soldCount": 0, "isFeatured": false, "status": "DRAFT",
  "description": "…", "comboProducts": null, "slug": "iphone-15", "priority": 0,
  "category": { "id": 3, "name": "Điện thoại", "slug": "dien-thoai" },
  "images": [ { "id": 1, "url": "https://…/products/abc.jpg", "priority": 0 } ],
  "seoTitle": "…", "seoDescription": "…", "seoImage": "https://…",
  "createdAt": "2026-07-10T10:00:00Z", "updatedAt": "2026-07-10T10:00:00Z"
}
```

> Lưu ý: endpoint **list** trả `images = null` (chỉ có `thumb`) để tránh N+1; **chi tiết**
> (`/{id}`, `/slug/{slug}`) mới kèm đầy đủ `images` theo thứ tự `priority`.

---

## 6. Ảnh gallery sản phẩm — `/api/products/{productId}/images`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/products/{productId}/images` | Công khai | Danh sách ảnh (sắp theo priority) |
| POST | `/api/products/{productId}/images` | ADMIN/SUPERADMIN | Upload 1 ảnh (multipart) |
| PATCH | `/api/products/{productId}/images/{imageId}?priority=2` | ADMIN/SUPERADMIN | Đổi thứ tự |
| DELETE | `/api/products/{productId}/images/{imageId}` | ADMIN/SUPERADMIN | Xoá ảnh (kèm xoá trên MinIO) |

Upload — `multipart/form-data`:
- `file`: (bắt buộc) file ảnh (`image/*`, ≤ 10MB).
- `priority`: (tuỳ chọn) số thứ tự (mặc định 0).

```
curl -X POST /api/products/12/images \
  -H "Authorization: Bearer <token>" \
  -F "file=@photo.jpg" -F "priority=1"
```

Trả `ProductImageResponse`: `{ "id": 5, "url": "https://…/products/xxx.jpg", "priority": 1 }`.

---

## 7. Upload media dùng chung — `/api/media`

Dùng để upload **thumb / seoImage / ảnh gallery** (lấy URL rồi đưa vào body tạo/sửa).

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/media/upload` | ADMIN/SUPERADMIN | Upload **1 ảnh**, trả 1 URL |
| POST | `/api/media/upload-multiple` | ADMIN/SUPERADMIN | Upload **nhiều ảnh** trong 1 request, trả mảng URL |

- Upload 1 ảnh — `multipart/form-data`: `file` (bắt buộc, `image/*`), `folder` (tuỳ chọn, mặc định
  `misc`). Trả `{ "url": "https://…/products/misc/xxx.jpg" }`.
- Upload nhiều ảnh — `multipart/form-data`: lặp lại part tên **`files`** cho từng ảnh (FE
  multi-select gửi 1 lần), `folder` (tuỳ chọn). Trả **mảng** theo đúng thứ tự:
  `[ { "url": "…/a.jpg" }, { "url": "…/b.jpg" } ]`. Tất cả file được kiểm tra hợp lệ **trước khi**
  ghi bất kỳ file nào — 1 file sai (rỗng/không phải ảnh) → cả lô bị từ chối (`4002`), không upload dở dang.

Giới hạn: mỗi file ≤ **10MB**, tổng 1 request ≤ **100MB** (vượt → `4003`).

> Luồng khuyến nghị cho form tạo/sửa sản phẩm: FE cho chọn nhiều ảnh → gọi
> `POST /api/media/upload-multiple` **một lần** lấy danh sách URL → đưa vào `images` của body tạo
> sản phẩm (mục 5) hoặc dùng cho thumb/seo.

> **Cấu hình MinIO** (biến môi trường): `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`,
> `MINIO_PUBLIC_URL` (URL public để dựng link, mặc định = `MINIO_URL`), `MINIO_BUCKET_PRODUCT`
> (mặc định `products`). Bucket được tạo tự động và set policy public-read khi upload lần đầu.

## 8. DTO cho FE (TypeScript)

```ts
export type ProductType = 'SINGLE' | 'COMBO';
export type ProductStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface ProductCategoryResponse {
  id: number; name: string; thumb: string; priority: number;
  longContent: string | null; des: string | null; slug: string; isActive: boolean;
  seoTitle: string | null; seoDescription: string | null; seoImage: string | null;
  createdAt: string; updatedAt: string;
}

export interface ProductCategoryBrief { id: number; name: string; slug: string; }
export interface ProductImageResponse { id: number; url: string; priority: number; }

export interface ProductResponse {
  id: number; name: string; thumb: string; sku: string | null;
  type: ProductType; price: number; compareAtPrice: number | null;
  soldCount: number; isFeatured: boolean; status: ProductStatus;
  description: string | null; comboProducts: string | null; slug: string; priority: number;
  category: ProductCategoryBrief;
  images: ProductImageResponse[] | null; // null ở list, có ở chi tiết
  seoTitle: string | null; seoDescription: string | null; seoImage: string | null;
  createdAt: string; updatedAt: string;
}

export interface UploadResponse { url: string; }

// Ảnh gallery gửi kèm khi TẠO sản phẩm (url lấy từ /api/media/upload)
export interface ProductImageInput { url: string; priority?: number; }
// ProductCreateRequest có thêm: images?: ProductImageInput[]

export interface PageResponse<T> {
  content: T[]; pageNumber: number; pageSize: number;
  totalElements: number; totalPages: number; first: boolean; last: boolean;
}
```

## 9. Bảng tổng hợp endpoint

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/product-categories` | Công khai | List danh mục |
| GET | `/api/product-categories/{id}` | Công khai | Chi tiết danh mục |
| GET | `/api/product-categories/slug/{slug}` | Công khai | Chi tiết danh mục theo slug (SEO) |
| POST | `/api/product-categories` | ADMIN/SUPERADMIN | Tạo danh mục |
| PUT | `/api/product-categories/{id}` | ADMIN/SUPERADMIN | Sửa danh mục |
| DELETE | `/api/product-categories/{id}` | ADMIN/SUPERADMIN | Xoá danh mục |
| GET | `/api/products` | Công khai | List sản phẩm (filter/sort) |
| GET | `/api/products/{id}` | Công khai | Chi tiết sản phẩm |
| GET | `/api/products/slug/{slug}` | Công khai | Chi tiết theo slug |
| POST | `/api/products` | ADMIN/SUPERADMIN | Tạo sản phẩm |
| PUT | `/api/products/{id}` | ADMIN/SUPERADMIN | Sửa sản phẩm |
| PATCH | `/api/products/{id}/status` | ADMIN/SUPERADMIN | Đổi trạng thái |
| PATCH | `/api/products/{id}/featured` | ADMIN/SUPERADMIN | Bật/tắt nổi bật |
| DELETE | `/api/products/{id}` | ADMIN/SUPERADMIN | Xoá sản phẩm |
| GET | `/api/products/{productId}/images` | Công khai | List ảnh gallery |
| POST | `/api/products/{productId}/images` | ADMIN/SUPERADMIN | Upload ảnh gallery |
| PATCH | `/api/products/{productId}/images/{imageId}` | ADMIN/SUPERADMIN | Đổi thứ tự ảnh |
| DELETE | `/api/products/{productId}/images/{imageId}` | ADMIN/SUPERADMIN | Xoá ảnh |
| POST | `/api/media/upload` | ADMIN/SUPERADMIN | Upload 1 ảnh dùng chung (thumb/seo) |
| POST | `/api/media/upload-multiple` | ADMIN/SUPERADMIN | Upload nhiều ảnh 1 request, trả mảng URL |

---

## 10. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Thay `<ACCESS_TOKEN>` bằng token ADMIN/SUPERADMIN (login ở AUTH_USER_API).
> Postman: **Import → Raw text** rồi dán lệnh `curl`. Đường dẫn file trong `-F` đổi thành file thật trên máy.

```bash
# ============ MEDIA (upload ảnh trước để lấy URL) ============
# Upload 1 ảnh
curl -X POST http://localhost:8080/api/media/upload \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "file=@/path/thumb.jpg" -F "folder=products"

# Upload NHIỀU ảnh 1 lần (lặp part "files")
curl -X POST http://localhost:8080/api/media/upload-multiple \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "files=@/path/a.jpg" -F "files=@/path/b.jpg" -F "folder=products"

# ============ DANH MỤC SẢN PHẨM ============
# List (public)
curl "http://localhost:8080/api/product-categories?name=&slug=&isActive=true&page=1&size=10&sortBy=priority&sortDirection=DESC"
# Chi tiết danh mục theo slug (SEO — CÔNG KHAI)
curl http://localhost:8080/api/product-categories/slug/binh-gom-su
# Chi tiết (public)
curl http://localhost:8080/api/product-categories/1
# Tạo (ADMIN)
curl -X POST http://localhost:8080/api/product-categories \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Điện thoại","thumb":"https://cdn/x.jpg","slug":"","priority":10,"longContent":"...","des":"{\"blocks\":[]}","isActive":true,"seoTitle":"","seoDescription":"","seoImage":""}'
# Cập nhật (ADMIN) — partial, chỉ gửi field muốn đổi
curl -X PUT http://localhost:8080/api/product-categories/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Điện thoại & Phụ kiện","isActive":false}'
# Xoá (ADMIN)
curl -X DELETE http://localhost:8080/api/product-categories/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# ============ SẢN PHẨM ============
# List (public) — đầy đủ filter
curl "http://localhost:8080/api/products?name=&sku=&type=SINGLE&status=PUBLISHED&productCategoryId=1&isFeatured=true&minPrice=100000&maxPrice=50000000&page=1&size=20&sortBy=price&sortDirection=DESC"
# Chi tiết theo id (public, kèm images)
curl http://localhost:8080/api/products/1
# Chi tiết theo slug (public)
curl http://localhost:8080/api/products/slug/iphone-15
# Tạo (ADMIN) — kèm luôn ảnh gallery
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{
    "name":"iPhone 15","thumb":"https://cdn/thumb.jpg","sku":"IP15-128","type":"SINGLE",
    "price":19990000,"compareAtPrice":22990000,"isFeatured":false,"status":"DRAFT",
    "description":"{\"blocks\":[]}","comboProducts":null,"slug":"","priority":0,"productCategoryId":1,
    "seoTitle":"iPhone 15","seoDescription":"...","seoImage":"https://cdn/seo.jpg",
    "images":[{"url":"https://cdn/a.jpg","priority":0},{"url":"https://cdn/b.jpg"}]
  }'
# Cập nhật (ADMIN) — partial
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"price":18990000,"isFeatured":true}'
# Đổi trạng thái (ADMIN)
curl -X PATCH http://localhost:8080/api/products/1/status \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"status":"PUBLISHED"}'
# Bật/tắt nổi bật (ADMIN)
curl -X PATCH "http://localhost:8080/api/products/1/featured?featured=true" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
# Xoá (ADMIN) — xoá kèm ảnh trên MinIO
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# ============ ẢNH GALLERY CỦA SẢN PHẨM ============
# List ảnh (public)
curl http://localhost:8080/api/products/1/images
# Thêm 1 ảnh (ADMIN) — upload file trực tiếp
curl -X POST http://localhost:8080/api/products/1/images \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "file=@/path/photo.jpg" -F "priority=1"
# Đổi thứ tự ảnh (ADMIN)
curl -X PATCH "http://localhost:8080/api/products/1/images/5?priority=2" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
# Xoá ảnh (ADMIN)
curl -X DELETE http://localhost:8080/api/products/1/images/5 \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
