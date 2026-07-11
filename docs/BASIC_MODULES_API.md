# BASIC MODULES API

Tài liệu bàn giao FE cho 5 module CRUD cơ bản (một bảng, không quan hệ phức tạp):
**Banner**, **Showroom**, **GalleryImage**, **FAQ**, **Redirect**.

## 1. Quy ước chung

- **Đọc (GET) công khai**; **ghi (POST/PUT/DELETE) yêu cầu `ADMIN`/`SUPERADMIN`** (`Authorization: Bearer <accessToken>`).
- Envelope `{code, message, data, timestamp}`, `code=1000` = thành công. Lỗi validation `4001`, chưa auth `4010`, thiếu quyền `4030`.
- Mã lỗi not-found riêng: Banner `4047`, Showroom `4048`, GalleryImage `4049`, FAQ `4050`, Redirect `4051`. Redirect trùng `fromPath` → `4098` (409).
- Ảnh (`imageUrl`) là **URL** — upload qua `POST /api/media/upload` (xem PRODUCT_API.md) để lấy URL rồi đưa vào body.
- **Phân trang 1-based**: `page` (mặc định 1), `size` (mặc định 10, kẹp 1–100), `sortBy`, `sortDirection` (`ASC`/`DESC`). `data` trả `PageResponse{content, pageNumber, pageSize, totalElements, totalPages, first, last}`.
- **PUT là partial update**: chỉ gửi field muốn đổi; field **không gửi (null) → giữ nguyên**. Ràng buộc "bắt buộc" trong body dưới đây chỉ áp dụng khi **tạo mới (POST)**.

Mỗi module có 5 endpoint giống nhau: `GET /` (list+filter), `GET /{id}`, `POST /` (tạo), `PUT /{id}` (sửa), `DELETE /{id}`.

---

## 2. Banner — `/api/banners`

- Filter: `title` (like), `position` (`HOME_HERO`/`HOME_CATEGORY`/`HOME_PROMO`), `isActive`.
- `sortBy`: `id, sortOrder, position, createdAt`.

Body tạo/sửa:

```json
{
  "title": "Sale hè",                 // tuỳ chọn, ≤255
  "imageUrl": "https://.../banner.jpg", // bắt buộc, ≤255 (URL)
  "linkUrl": "https://.../khuyen-mai", // tuỳ chọn, ≤255
  "position": "HOME_HERO",            // bắt buộc (enum)
  "sortOrder": 0,                      // tuỳ chọn (mặc định 0)
  "isActive": true,                    // tuỳ chọn (mặc định true)
  "startsAt": "2026-07-01T00:00:00Z", // tuỳ chọn
  "endsAt": "2026-07-31T23:59:59Z"    // tuỳ chọn
}
```

`BannerResponse`: `{id, title, imageUrl, linkUrl, position, sortOrder, isActive, startsAt, endsAt, createdAt, updatedAt}`.

---

## 3. Showroom — `/api/showrooms`

- Filter: `name` (like), `isActive`.
- `sortBy`: `id, name, sortOrder, createdAt`.

Body tạo/sửa:

```json
{
  "name": "Showroom Hà Nội",   // bắt buộc, ≤255
  "phone": "02412345678",       // tuỳ chọn, ≤20
  "address": "123 phố ABC…",   // bắt buộc (text)
  "mapEmbedUrl": "https://maps…", // tuỳ chọn, ≤500
  "openingHours": "8:00–21:00", // tuỳ chọn
  "sortOrder": 0,                // tuỳ chọn (mặc định 0)
  "isActive": true              // tuỳ chọn (mặc định true)
}
```

`ShowroomResponse`: `{id, name, phone, address, mapEmbedUrl, openingHours, sortOrder, isActive, createdAt, updatedAt}`.

---

## 4. GalleryImage — `/api/gallery-images`

- Filter: `title` (like), `category` (like), `isActive`.
- `sortBy`: `id, sortOrder, createdAt`.

Body tạo/sửa:

```json
{
  "imageUrl": "https://.../g1.jpg", // bắt buộc, ≤255 (URL)
  "title": "Không gian bếp",        // tuỳ chọn, ≤255
  "category": "kitchen",             // tuỳ chọn, ≤100
  "sortOrder": 0,                    // tuỳ chọn (mặc định 0)
  "isActive": true                   // tuỳ chọn (mặc định true)
}
```

`GalleryImageResponse`: `{id, imageUrl, title, category, sortOrder, isActive, createdAt, updatedAt}`.

---

## 5. FAQ — `/api/faqs`

- Filter: `question` (like), `category` (like), `isActive`.
- `sortBy`: `id, sortOrder, createdAt`.

Body tạo/sửa:

```json
{
  "question": "Chính sách bảo hành?", // bắt buộc, ≤500
  "answer": "Bảo hành 12 tháng…",    // bắt buộc (text)
  "category": "warranty",             // tuỳ chọn, ≤100
  "sortOrder": 0,                     // tuỳ chọn (mặc định 0)
  "isActive": true                    // tuỳ chọn (mặc định true)
}
```

`FaqResponse`: `{id, question, answer, category, sortOrder, isActive, createdAt, updatedAt}`.

---

## 6. Redirect — `/api/redirects`

- Filter: `fromPath` (like), `toPath` (like), `isActive`.
- `sortBy`: `id, createdAt`.
- `fromPath` **duy nhất** — trùng → lỗi `4098`.

Body tạo/sửa:

```json
{
  "fromPath": "/san-pham-cu",  // bắt buộc, ≤500, duy nhất
  "toPath": "/san-pham-moi",   // bắt buộc, ≤500
  "statusCode": 301,            // tuỳ chọn (mặc định 301)
  "isActive": true             // tuỳ chọn (mặc định true)
}
```

`RedirectResponse`: `{id, fromPath, toPath, statusCode, hitCount, isActive, createdAt, updatedAt}`.
> `hitCount` chỉ đọc (không nhận/không sửa qua create/update).

## 7. DTO TypeScript

```ts
export type BannerPosition = 'HOME_HERO' | 'HOME_CATEGORY' | 'HOME_PROMO';

export interface BannerResponse {
  id: number; title: string | null; imageUrl: string; linkUrl: string | null;
  position: BannerPosition; sortOrder: number; isActive: boolean;
  startsAt: string | null; endsAt: string | null; createdAt: string; updatedAt: string;
}
export interface ShowroomResponse {
  id: number; name: string; phone: string | null; address: string;
  mapEmbedUrl: string | null; openingHours: string | null; sortOrder: number;
  isActive: boolean; createdAt: string; updatedAt: string;
}
export interface GalleryImageResponse {
  id: number; imageUrl: string; title: string | null; category: string | null;
  sortOrder: number; isActive: boolean; createdAt: string; updatedAt: string;
}
export interface FaqResponse {
  id: number; question: string; answer: string; category: string | null;
  sortOrder: number; isActive: boolean; createdAt: string; updatedAt: string;
}
export interface RedirectResponse {
  id: number; fromPath: string; toPath: string; statusCode: number; hitCount: number;
  isActive: boolean; createdAt: string; updatedAt: string;
}
```

---

## 8. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. GET công khai; các thao tác ghi thay `<ACCESS_TOKEN>` bằng token ADMIN/SUPERADMIN.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# ============ BANNER ============
curl "http://localhost:8080/api/banners?title=&position=HOME_HERO&isActive=true&page=1&size=10&sortBy=sortOrder&sortDirection=ASC"
curl http://localhost:8080/api/banners/1
curl -X POST http://localhost:8080/api/banners \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"title":"Sale hè","imageUrl":"https://cdn/banner.jpg","linkUrl":"https://site/khuyen-mai","position":"HOME_HERO","sortOrder":0,"isActive":true,"startsAt":"2026-07-01T00:00:00Z","endsAt":"2026-07-31T23:59:59Z"}'
curl -X PUT http://localhost:8080/api/banners/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"isActive":false}'
curl -X DELETE http://localhost:8080/api/banners/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# ============ SHOWROOM ============
curl "http://localhost:8080/api/showrooms?name=&isActive=true&page=1&size=10&sortBy=sortOrder&sortDirection=ASC"
curl http://localhost:8080/api/showrooms/1
curl -X POST http://localhost:8080/api/showrooms \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Showroom Hà Nội","phone":"02412345678","address":"123 phố ABC, Hà Nội","mapEmbedUrl":"https://maps/...","openingHours":"8:00-21:00","sortOrder":0,"isActive":true}'
curl -X PUT http://localhost:8080/api/showrooms/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"phone":"02499998888"}'
curl -X DELETE http://localhost:8080/api/showrooms/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# ============ GALLERY IMAGE ============
curl "http://localhost:8080/api/gallery-images?title=&category=kitchen&isActive=true&page=1&size=10&sortBy=sortOrder&sortDirection=ASC"
curl http://localhost:8080/api/gallery-images/1
curl -X POST http://localhost:8080/api/gallery-images \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"imageUrl":"https://cdn/g1.jpg","title":"Không gian bếp","category":"kitchen","sortOrder":0,"isActive":true}'
curl -X PUT http://localhost:8080/api/gallery-images/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"title":"Bếp hiện đại"}'
curl -X DELETE http://localhost:8080/api/gallery-images/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# ============ FAQ ============
curl "http://localhost:8080/api/faqs?question=&category=warranty&isActive=true&page=1&size=10&sortBy=sortOrder&sortDirection=ASC"
curl http://localhost:8080/api/faqs/1
curl -X POST http://localhost:8080/api/faqs \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"question":"Chính sách bảo hành?","answer":"Bảo hành 12 tháng...","category":"warranty","sortOrder":0,"isActive":true}'
curl -X PUT http://localhost:8080/api/faqs/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"answer":"Bảo hành 24 tháng..."}'
curl -X DELETE http://localhost:8080/api/faqs/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# ============ REDIRECT ============
curl "http://localhost:8080/api/redirects?fromPath=&toPath=&isActive=true&page=1&size=10&sortBy=id&sortDirection=ASC"
curl http://localhost:8080/api/redirects/1
curl -X POST http://localhost:8080/api/redirects \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"fromPath":"/san-pham-cu","toPath":"/san-pham-moi","statusCode":301,"isActive":true}'
curl -X PUT http://localhost:8080/api/redirects/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"toPath":"/san-pham-moi-2","statusCode":302}'
curl -X DELETE http://localhost:8080/api/redirects/1 -H "Authorization: Bearer <ACCESS_TOKEN>"
```
