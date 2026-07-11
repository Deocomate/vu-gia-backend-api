# PAGE API — Trang nội dung (CMS)

Tài liệu bàn giao FE cho module **Page (trang CMS: about-us, home, chính sách...)**.

## 1. Tổng quan & phân quyền

- **Đọc công khai** (`GET`): storefront render trang theo `key` (ví dụ `about-us`, `home`).
- **Ghi (tạo/sửa/xoá) chỉ dành cho `ADMIN`/`SUPERADMIN`**.
- `key` **duy nhất** — định danh trang để FE truy vấn (`GET /api/pages/key/{key}`).
- `content` là **chuỗi JSON tự do** (lưu nguyên trong cột `JSON`); FE tự định nghĩa cấu trúc block bên trong.
- `status` (`ContentStatus`): `DRAFT | PUBLISHED`, mặc định `DRAFT` khi tạo.
- **PUT là partial update**: chỉ gửi field muốn đổi; field null → giữ nguyên. Đổi `key` sang giá trị đã tồn tại → `4104`.

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation (thiếu `key`, field vượt độ dài) |
| 4010 | 401 | Chưa xác thực (ghi mà chưa đăng nhập) |
| 4030 | 403 | Không đủ quyền (không phải ADMIN/SUPERADMIN) |
| 4057 | 404 | Không tìm thấy page (theo id / key) |
| 4104 | 409 | `key` đã tồn tại |

## 3. Endpoint

Phân trang 1-based (`page` mặc định 1, `size` 10 kẹp 1–100). `sortBy` cho phép: `id, key, title, status, createdAt`
(giá trị lạ → ép về `id`). `sortDirection`: `ASC` (mặc định) | `DESC`. Filter list: `key`, `title` (like) và `status`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/pages` | **Công khai** | Danh sách + filter + phân trang |
| GET | `/api/pages/{id}` | **Công khai** | Chi tiết theo id |
| GET | `/api/pages/key/{key}` | **Công khai** | Chi tiết theo `key` (storefront) |
| POST | `/api/pages` | ADMIN/SUPERADMIN | Tạo mới |
| PUT | `/api/pages/{id}` | ADMIN/SUPERADMIN | Cập nhật (partial) |
| DELETE | `/api/pages/{id}` | ADMIN/SUPERADMIN | Xoá |

### 3.1 Tạo / cập nhật

`POST /api/pages` (tạo) — `PUT /api/pages/{id}` (cập nhật, mọi field optional):

```json
{
  "key": "about-us",                 // POST: bắt buộc, ≤255, duy nhất
  "title": "Về chúng tôi",           // tuỳ chọn, ≤255
  "content": "{\"blocks\":[]}",      // tuỳ chọn, chuỗi JSON
  "heroTitle": "Vũ Gia",             // tuỳ chọn, ≤255
  "heroSubtitle": "Nội thất cao cấp",// tuỳ chọn, ≤255
  "heroDes": "Mô tả dài...",         // tuỳ chọn (TEXT)
  "heroImage": "https://.../hero.jpg",// tuỳ chọn, ≤255
  "status": "PUBLISHED",             // tuỳ chọn: DRAFT | PUBLISHED (mặc định DRAFT)
  "seoTitle": "Về Vũ Gia",           // tuỳ chọn, ≤255
  "seoDescription": "…",             // tuỳ chọn, ≤500
  "seoImage": "https://.../og.jpg"   // tuỳ chọn, ≤255
}
```

Response `200`:

```json
{
  "code": 1000,
  "message": "Created successfully",
  "data": {
    "id": 1,
    "key": "about-us",
    "title": "Về chúng tôi",
    "content": "{\"blocks\":[]}",
    "heroTitle": "Vũ Gia",
    "heroSubtitle": "Nội thất cao cấp",
    "heroDes": "Mô tả dài...",
    "heroImage": "https://.../hero.jpg",
    "status": "PUBLISHED",
    "seoTitle": "Về Vũ Gia",
    "seoDescription": "…",
    "seoImage": "https://.../og.jpg",
    "createdAt": "2026-07-11T02:00:00Z",
    "updatedAt": "2026-07-11T02:00:00Z"
  },
  "timestamp": "2026-07-11T02:00:00Z"
}
```

### 3.2 Danh sách (công khai)

`GET /api/pages`

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `key` | string | — | Lọc theo key (chứa, không phân biệt hoa thường) |
| `title` | string | — | Lọc theo title (chứa) |
| `status` | enum | — | `DRAFT` \| `PUBLISHED` |
| `page` | int | 1 | Trang (1-based) |
| `size` | int | 10 | Kích thước trang (kẹp 1–100) |
| `sortBy` | string | `id` | `id` \| `key` \| `title` \| `status` \| `createdAt` |
| `sortDirection` | string | `ASC` | `ASC` \| `DESC` |

Ví dụ: `GET /api/pages?status=PUBLISHED&sortBy=createdAt&sortDirection=DESC` → `data` là `PageResponse<PageDetailResponse>`.

### 3.3 Chi tiết

- `GET /api/pages/{id}` → theo id.
- `GET /api/pages/key/{key}` → theo key (dùng cho storefront).

Cả hai lỗi `4057` nếu không tìm thấy.

### 3.4 Xoá (admin)

`DELETE /api/pages/{id}` → `data = null`, message `Deleted successfully`. Lỗi `4057` nếu không tìm thấy.

## 4. DTO cho FE (TypeScript)

```typescript
type ContentStatus = 'DRAFT' | 'PUBLISHED';

// Response (chi tiết page)
interface PageDetailResponse {
  id: number;
  key: string;
  title: string | null;
  content: string | null;      // chuỗi JSON
  heroTitle: string | null;
  heroSubtitle: string | null;
  heroDes: string | null;
  heroImage: string | null;
  status: ContentStatus;
  seoTitle: string | null;
  seoDescription: string | null;
  seoImage: string | null;
  createdAt: string;
  updatedAt: string;
}

// POST /api/pages
interface PageCreateRequest {
  key: string;
  title?: string;
  content?: string;
  heroTitle?: string;
  heroSubtitle?: string;
  heroDes?: string;
  heroImage?: string;
  status?: ContentStatus;      // default DRAFT
  seoTitle?: string;
  seoDescription?: string;
  seoImage?: string;
}

// PUT /api/pages/{id} — tất cả optional (partial update)
type PageUpdateRequest = Partial<PageCreateRequest>;

// GET /api/pages — query params
interface PageSearchParams {
  key?: string;
  title?: string;
  status?: ContentStatus;
  page?: number;               // default 1
  size?: number;               // default 10, max 100
  sortBy?: 'id' | 'key' | 'title' | 'status' | 'createdAt';
  sortDirection?: 'ASC' | 'DESC';
}

interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
```

## 5. Tổng hợp endpoint

| Method | Path | Auth | Quyền | Mô tả |
|---|---|---|---|---|
| GET | `/api/pages` | Công khai | — | Danh sách + filter |
| GET | `/api/pages/{id}` | Công khai | — | Chi tiết theo id |
| GET | `/api/pages/key/{key}` | Công khai | — | Chi tiết theo key |
| POST | `/api/pages` | JWT | ADMIN/SUPERADMIN | Tạo mới |
| PUT | `/api/pages/{id}` | JWT | ADMIN/SUPERADMIN | Cập nhật (partial) |
| DELETE | `/api/pages/{id}` | JWT | ADMIN/SUPERADMIN | Xoá |

## 6. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Đọc là công khai; tạo/sửa/xoá cần `<ACCESS_TOKEN>` ADMIN/SUPERADMIN.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# List page (CÔNG KHAI) — filter + phân trang
curl "http://localhost:8080/api/pages?key=&title=&status=PUBLISHED&page=1&size=10&sortBy=createdAt&sortDirection=DESC"

# Chi tiết theo id (CÔNG KHAI)
curl http://localhost:8080/api/pages/1

# Chi tiết theo key (CÔNG KHAI — dùng cho storefront)
curl http://localhost:8080/api/pages/key/about-us

# Tạo (ADMIN)
curl -X POST http://localhost:8080/api/pages \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"key":"about-us","title":"Về chúng tôi","content":"{\"blocks\":[]}","heroTitle":"Vũ Gia","heroSubtitle":"Nội thất cao cấp","status":"PUBLISHED","seoTitle":"Về Vũ Gia","seoDescription":"Giới thiệu Vũ Gia"}'

# Cập nhật (ADMIN) — partial
curl -X PUT http://localhost:8080/api/pages/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"title":"Về chúng tôi (mới)","status":"DRAFT"}'

# Xoá (ADMIN)
curl -X DELETE http://localhost:8080/api/pages/1 -H "Authorization: Bearer <ACCESS_TOKEN>"
```
