# NEWS API — Tin tức

Tài liệu bàn giao FE cho domain **Tin tức**: Danh mục tin (`news-categories`) và Bài viết (`news`).

## 1. Tổng quan & phân quyền

- **Đọc (GET) công khai** (storefront); **ghi (POST/PUT/PATCH/DELETE) yêu cầu `ADMIN`/`SUPERADMIN`** (`Authorization: Bearer <accessToken>`).
- Slug **tự sinh từ `name`/`title`** (bỏ dấu tiếng Việt), đảm bảo duy nhất (hậu tố `-2, -3…`); client có thể tự gửi `slug`, trùng → 409.
- **PUT là partial update**: chỉ gửi field muốn đổi; field không gửi → giữ nguyên. Ràng buộc bắt buộc chỉ khi **tạo (POST)**.
- **`publishedAt` tự động**: set = thời điểm hiện tại **lần đầu** bài viết chuyển sang `PUBLISHED` (qua create/update/đổi trạng thái); sau đó giữ nguyên.
- **`viewCount` tự tăng** mỗi lần gọi `GET /api/news/slug/{slug}` (trang chi tiết công khai). Chỉ đọc, không nhận qua create/update.
- Ảnh (`thumb`, `seoImage`) là **URL** — upload qua `POST /api/media/upload` (xem PRODUCT_API.md) rồi đưa vào body.

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation |
| 4010 | 401 | Chưa xác thực |
| 4030 | 403 | Không đủ quyền |
| 4053 | 404 | Không tìm thấy bài viết |
| 4054 | 404 | Không tìm thấy danh mục tin |
| 4100 | 409 | Slug bài viết đã tồn tại |
| 4101 | 409 | Slug danh mục tin đã tồn tại |
| 4102 | 409 | Danh mục tin còn bài viết (không xoá được) |

## 3. Phân trang / sort

`page` (mặc định 1, 1-based), `size` (10, kẹp 1–100), `sortBy`, `sortDirection` (`ASC`/`DESC`). Trả `PageResponse`.

- news-categories `sortBy`: `id, name, priority, createdAt`.
- news `sortBy`: `id, priority, viewCount, publishedAt, createdAt` (không sort theo `title` vì là TEXT).

---

## 4. Danh mục tin — `/api/news-categories`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/news-categories` | Công khai | List (filter: `name`, `slug`) |
| GET | `/api/news-categories/{id}` | Công khai | Chi tiết |
| GET | `/api/news-categories/slug/{slug}` | Công khai | Chi tiết theo slug (SEO) |
| POST | `/api/news-categories` | ADMIN/SUPERADMIN | Tạo |
| PUT | `/api/news-categories/{id}` | ADMIN/SUPERADMIN | Cập nhật (partial) |
| DELETE | `/api/news-categories/{id}` | ADMIN/SUPERADMIN | Xoá (409 nếu còn bài) |

Body tạo/sửa:

```json
{
  "name": "Thời sự",     // bắt buộc (tạo), ≤ 50
  "slug": "thoi-su",     // tuỳ chọn; bỏ trống tự sinh
  "priority": 10          // tuỳ chọn (mặc định 0)
}
```

`NewsCategoryResponse`: `{id, name, slug, priority, createdAt, updatedAt}`.

---

## 5. Bài viết — `/api/news`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/news` | Công khai | List + filter |
| GET | `/api/news/{id}` | Công khai | Chi tiết theo id |
| GET | `/api/news/slug/{slug}` | Công khai | Chi tiết theo slug (**+1 view**) |
| POST | `/api/news` | ADMIN/SUPERADMIN | Tạo |
| PUT | `/api/news/{id}` | ADMIN/SUPERADMIN | Cập nhật (partial) |
| PATCH | `/api/news/{id}/status` | ADMIN/SUPERADMIN | Đổi trạng thái |
| DELETE | `/api/news/{id}` | ADMIN/SUPERADMIN | Xoá |

**Filter list**: `title` (like), `status` (`DRAFT`/`PUBLISHED`), `newsCategoryId`.
Ví dụ: `GET /api/news?status=PUBLISHED&newsCategoryId=2&page=1&size=10&sortBy=publishedAt&sortDirection=DESC`

Body tạo:

```json
{
  "title": "Khai trương showroom mới",   // bắt buộc
  "thumb": "https://cdn/thumb.jpg",       // bắt buộc, ≤255 (URL)
  "shortContent": "Tóm tắt ngắn...",     // bắt buộc
  "des": "{\"blocks\":[]}",              // bắt buộc (chuỗi JSON nội dung)
  "slug": "",                              // tuỳ chọn; bỏ trống tự sinh từ title
  "priority": 0,                           // tuỳ chọn
  "status": "DRAFT",                       // tuỳ chọn (mặc định DRAFT); PUBLISHED → set publishedAt
  "newsCategoryId": 2,                     // bắt buộc
  "seoTitle": "...", "seoDescription": "...", "seoImage": "https://cdn/seo.jpg"
}
```

Body cập nhật (`PUT`) — partial (chỉ gửi field muốn đổi). Body đổi trạng thái (`PATCH /{id}/status`): `{ "status": "PUBLISHED" }`.

`NewsResponse`:

```json
{
  "id": 5, "title": "...", "thumb": "https://…", "shortContent": "...",
  "des": "{...}", "slug": "khai-truong-showroom-moi", "priority": 0,
  "viewCount": 128, "status": "PUBLISHED", "publishedAt": "2026-07-10T08:00:00Z",
  "category": { "id": 2, "name": "Thời sự", "slug": "thoi-su" },
  "seoTitle": "...", "seoDescription": "...", "seoImage": "https://…",
  "createdAt": "2026-07-10T07:00:00Z", "updatedAt": "2026-07-10T08:00:00Z"
}
```

## 6. DTO cho FE (TypeScript)

```ts
export type ContentStatus = 'DRAFT' | 'PUBLISHED';

export interface NewsCategoryResponse {
  id: number; name: string; slug: string; priority: number;
  createdAt: string; updatedAt: string;
}
export interface NewsCategoryBrief { id: number; name: string; slug: string; }

export interface NewsResponse {
  id: number; title: string; thumb: string; shortContent: string; des: string;
  slug: string; priority: number; viewCount: number; status: ContentStatus;
  publishedAt: string | null; category: NewsCategoryBrief;
  seoTitle: string | null; seoDescription: string | null; seoImage: string | null;
  createdAt: string; updatedAt: string;
}
```

## 7. Bảng tổng hợp endpoint

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/news-categories` | Công khai | List danh mục tin |
| GET | `/api/news-categories/{id}` | Công khai | Chi tiết danh mục |
| GET | `/api/news-categories/slug/{slug}` | Công khai | Chi tiết danh mục theo slug (SEO) |
| POST | `/api/news-categories` | ADMIN/SUPERADMIN | Tạo danh mục |
| PUT | `/api/news-categories/{id}` | ADMIN/SUPERADMIN | Sửa danh mục |
| DELETE | `/api/news-categories/{id}` | ADMIN/SUPERADMIN | Xoá danh mục |
| GET | `/api/news` | Công khai | List bài viết |
| GET | `/api/news/{id}` | Công khai | Chi tiết theo id |
| GET | `/api/news/slug/{slug}` | Công khai | Chi tiết theo slug (+1 view) |
| POST | `/api/news` | ADMIN/SUPERADMIN | Tạo bài viết |
| PUT | `/api/news/{id}` | ADMIN/SUPERADMIN | Sửa bài viết |
| PATCH | `/api/news/{id}/status` | ADMIN/SUPERADMIN | Đổi trạng thái |
| DELETE | `/api/news/{id}` | ADMIN/SUPERADMIN | Xoá bài viết |

---

## 8. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. GET công khai; thao tác ghi thay `<ACCESS_TOKEN>` bằng token ADMIN/SUPERADMIN.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# ============ DANH MỤC TIN ============
curl "http://localhost:8080/api/news-categories?name=&slug=&page=1&size=10&sortBy=priority&sortDirection=DESC"
# Chi tiết danh mục theo slug (SEO — CÔNG KHAI)
curl http://localhost:8080/api/news-categories/slug/kien-thuc-gom-su
curl http://localhost:8080/api/news-categories/1
curl -X POST http://localhost:8080/api/news-categories \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Thời sự","slug":"","priority":10}'
curl -X PUT http://localhost:8080/api/news-categories/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"priority":5}'
curl -X DELETE http://localhost:8080/api/news-categories/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# ============ BÀI VIẾT ============
# List (public) + filter
curl "http://localhost:8080/api/news?title=&status=PUBLISHED&newsCategoryId=1&page=1&size=10&sortBy=publishedAt&sortDirection=DESC"
# Chi tiết theo id (public)
curl http://localhost:8080/api/news/1
# Chi tiết theo slug (public, +1 view)
curl http://localhost:8080/api/news/slug/khai-truong-showroom-moi
# Tạo (ADMIN)
curl -X POST http://localhost:8080/api/news \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{
    "title":"Khai trương showroom mới","thumb":"https://cdn/thumb.jpg","shortContent":"Tóm tắt...",
    "des":"{\"blocks\":[]}","slug":"","priority":0,"status":"PUBLISHED","newsCategoryId":1,
    "seoTitle":"Khai trương","seoDescription":"...","seoImage":"https://cdn/seo.jpg"
  }'
# Cập nhật (ADMIN) — partial
curl -X PUT http://localhost:8080/api/news/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"title":"Tiêu đề mới","priority":3}'
# Đổi trạng thái (ADMIN)
curl -X PATCH http://localhost:8080/api/news/1/status \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"status":"PUBLISHED"}'
# Xoá (ADMIN)
curl -X DELETE http://localhost:8080/api/news/1 -H "Authorization: Bearer <ACCESS_TOKEN>"
```
