# NEWSLETTER API — Đăng ký nhận bản tin

Tài liệu bàn giao FE cho module **Newsletter Subscriber (người đăng ký nhận bản tin)**.

## 1. Tổng quan & phân quyền

- **`POST /api/newsletter-subscribers` là công khai** — dùng cho form đăng ký nhận bản tin ở footer/landing:
  khách nhập email → tạo bản ghi subscriber.
- **Quản lý (list / chi tiết / cập nhật trạng thái / xoá) chỉ dành cho `ADMIN`/`SUPERADMIN`** — cố ý **không**
  phơi danh sách email ra công khai.
- `email` **duy nhất**: đăng ký lại email đã tồn tại → lỗi `4103` (đã đăng ký).
- Bản ghi mới luôn có `isActive = true`. Admin có thể bật/tắt (`isActive`) để đánh dấu huỷ đăng ký thay vì xoá cứng.
- Bảng `newsletter_subscribers` chỉ có `created_at` (không có `updated_at`).

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation (email trống/sai định dạng) |
| 4010 | 401 | Chưa xác thực (gọi endpoint quản lý mà chưa đăng nhập) |
| 4030 | 403 | Không đủ quyền (không phải ADMIN/SUPERADMIN) |
| 4055 | 404 | Không tìm thấy subscriber (theo id) |
| 4103 | 409 | Email đã được đăng ký |

## 3. Endpoint

Phân trang 1-based (`page` mặc định 1, `size` 10 kẹp 1–100). `sortBy` cho phép: `id, email, createdAt`
(giá trị lạ → ép về `id`). `sortDirection`: `ASC` (mặc định) | `DESC`. Filter list: `email` (like), `isActive`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/newsletter-subscribers` | **Công khai** | Đăng ký email nhận bản tin |
| GET | `/api/newsletter-subscribers` | ADMIN/SUPERADMIN | Danh sách + filter + phân trang |
| GET | `/api/newsletter-subscribers/{id}` | ADMIN/SUPERADMIN | Chi tiết |
| PUT | `/api/newsletter-subscribers/{id}` | ADMIN/SUPERADMIN | Bật/tắt trạng thái `isActive` |
| DELETE | `/api/newsletter-subscribers/{id}` | ADMIN/SUPERADMIN | Xoá subscriber |

### 3.1 Đăng ký (công khai)

`POST /api/newsletter-subscribers`

Request:

```json
{
  "email": "user@example.com"   // bắt buộc, đúng định dạng email, ≤255
}
```

Response `200`:

```json
{
  "code": 1000,
  "message": "Subscribed successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "isActive": true,
    "createdAt": "2026-07-11T02:00:00Z"
  },
  "timestamp": "2026-07-11T02:00:00Z"
}
```

Lỗi có thể gặp: `4001` (email trống/sai định dạng), `4103` (email đã đăng ký).

### 3.2 Danh sách (admin)

`GET /api/newsletter-subscribers`

Query params:

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `email` | string | — | Lọc theo email (chứa, không phân biệt hoa thường) |
| `isActive` | boolean | — | Lọc theo trạng thái |
| `page` | int | 1 | Trang (1-based) |
| `size` | int | 10 | Kích thước trang (kẹp 1–100) |
| `sortBy` | string | `id` | `id` \| `email` \| `createdAt` |
| `sortDirection` | string | `ASC` | `ASC` \| `DESC` |

Ví dụ: `GET /api/newsletter-subscribers?isActive=true&sortBy=createdAt&sortDirection=DESC&page=1&size=20`

Response `200`:

```json
{
  "code": 1000,
  "message": null,
  "data": {
    "content": [
      { "id": 1, "email": "user@example.com", "isActive": true, "createdAt": "2026-07-11T02:00:00Z" }
    ],
    "pageNumber": 1,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-11T02:00:00Z"
}
```

### 3.3 Chi tiết (admin)

`GET /api/newsletter-subscribers/{id}` → `data` là 1 subscriber. Lỗi `4055` nếu không tìm thấy.

### 3.4 Cập nhật trạng thái (admin)

`PUT /api/newsletter-subscribers/{id}`

```json
{
  "isActive": false   // bắt buộc
}
```

Response `200` trả subscriber sau cập nhật. Lỗi `4055` nếu không tìm thấy, `4001` nếu thiếu `isActive`.

### 3.5 Xoá (admin)

`DELETE /api/newsletter-subscribers/{id}` → `data = null`, message `Deleted successfully`. Lỗi `4055` nếu không tìm thấy.

## 4. DTO cho FE (TypeScript)

```typescript
// Response
interface NewsletterSubscriberResponse {
  id: number;
  email: string;
  isActive: boolean;
  createdAt: string; // ISO-8601 Instant
}

// POST /api/newsletter-subscribers (công khai)
interface NewsletterSubscribeRequest {
  email: string;
}

// PUT /api/newsletter-subscribers/{id} (admin)
interface NewsletterSubscriberUpdateRequest {
  isActive: boolean;
}

// GET /api/newsletter-subscribers (admin) — query params
interface NewsletterSubscriberSearchParams {
  email?: string;
  isActive?: boolean;
  page?: number;         // default 1
  size?: number;         // default 10, max 100
  sortBy?: 'id' | 'email' | 'createdAt';
  sortDirection?: 'ASC' | 'DESC';
}

// Envelope chung
interface ApiResponse<T> {
  code: number;      // 1000 = success
  message: string | null;
  data: T;
  timestamp: string;
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
| POST | `/api/newsletter-subscribers` | Công khai | — | Đăng ký nhận bản tin |
| GET | `/api/newsletter-subscribers` | JWT | ADMIN/SUPERADMIN | Danh sách + filter |
| GET | `/api/newsletter-subscribers/{id}` | JWT | ADMIN/SUPERADMIN | Chi tiết |
| PUT | `/api/newsletter-subscribers/{id}` | JWT | ADMIN/SUPERADMIN | Bật/tắt `isActive` |
| DELETE | `/api/newsletter-subscribers/{id}` | JWT | ADMIN/SUPERADMIN | Xoá |

## 6. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Đăng ký là công khai; các endpoint quản lý cần `<ACCESS_TOKEN>` ADMIN/SUPERADMIN.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# Đăng ký nhận bản tin (CÔNG KHAI — không cần token)
curl -X POST http://localhost:8080/api/newsletter-subscribers \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'

# List subscriber (ADMIN) — filter + phân trang
curl "http://localhost:8080/api/newsletter-subscribers?email=&isActive=true&page=1&size=10&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Chi tiết (ADMIN)
curl http://localhost:8080/api/newsletter-subscribers/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# Bật/tắt trạng thái (ADMIN)
curl -X PUT http://localhost:8080/api/newsletter-subscribers/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"isActive":false}'

# Xoá (ADMIN)
curl -X DELETE http://localhost:8080/api/newsletter-subscribers/1 -H "Authorization: Bearer <ACCESS_TOKEN>"
```
