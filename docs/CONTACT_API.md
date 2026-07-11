# CONTACT API — Yêu cầu liên hệ

Tài liệu bàn giao FE cho module **Contact Request (form liên hệ)**.

## 1. Tổng quan & phân quyền

- **`POST /api/contact-requests` là công khai** — dùng cho form liên hệ trên website: khách gửi
  `name / email / phone / content` → tạo yêu cầu với trạng thái `NEW`.
- **Quản lý (list / chi tiết / đổi trạng thái / xoá) chỉ dành cho `ADMIN`/`SUPERADMIN`**.
- Vòng đời trạng thái (`status`): `NEW → HANDLED → CLOSED`.
- Khi admin chuyển trạng thái sang **khác `NEW`**, hệ thống tự **gán người xử lý** (`handledBy`) là
  tài khoản đang đăng nhập → trả về `handledById` + `handledByUsername`.

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation (thiếu `name`/`content`, email sai định dạng) |
| 4010 | 401 | Chưa xác thực (gọi endpoint quản lý mà chưa đăng nhập) |
| 4030 | 403 | Không đủ quyền (không phải ADMIN/SUPERADMIN) |
| 4056 | 404 | Không tìm thấy yêu cầu liên hệ (theo id) |

## 3. Endpoint

Phân trang 1-based (`page` mặc định 1, `size` 10 kẹp 1–100). `sortBy` cho phép: `id, name, status, createdAt`
(giá trị lạ → ép về `id`). `sortDirection`: `ASC` (mặc định) | `DESC`. Filter list: `name`, `email`, `phone`
(like) và `status`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/contact-requests` | **Công khai** | Gửi yêu cầu liên hệ |
| GET | `/api/contact-requests` | ADMIN/SUPERADMIN | Danh sách + filter + phân trang |
| GET | `/api/contact-requests/{id}` | ADMIN/SUPERADMIN | Chi tiết |
| PUT | `/api/contact-requests/{id}` | ADMIN/SUPERADMIN | Đổi trạng thái |
| DELETE | `/api/contact-requests/{id}` | ADMIN/SUPERADMIN | Xoá |

### 3.1 Gửi liên hệ (công khai)

`POST /api/contact-requests`

```json
{
  "name": "Nguyễn Văn A",        // bắt buộc, ≤255
  "email": "a@example.com",       // tuỳ chọn, đúng định dạng email nếu có, ≤255
  "phone": "0900123456",          // tuỳ chọn, ≤20
  "content": "Tôi cần tư vấn..."  // bắt buộc
}
```

Response `200`:

```json
{
  "code": 1000,
  "message": "Submitted successfully",
  "data": {
    "id": 1,
    "name": "Nguyễn Văn A",
    "email": "a@example.com",
    "phone": "0900123456",
    "content": "Tôi cần tư vấn...",
    "status": "NEW",
    "handledById": null,
    "handledByUsername": null,
    "createdAt": "2026-07-11T02:00:00Z",
    "updatedAt": "2026-07-11T02:00:00Z"
  },
  "timestamp": "2026-07-11T02:00:00Z"
}
```

### 3.2 Danh sách (admin)

`GET /api/contact-requests`

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `name` | string | — | Lọc theo tên (chứa, không phân biệt hoa thường) |
| `email` | string | — | Lọc theo email (chứa) |
| `phone` | string | — | Lọc theo sđt (chứa) |
| `status` | enum | — | `NEW` \| `HANDLED` \| `CLOSED` |
| `page` | int | 1 | Trang (1-based) |
| `size` | int | 10 | Kích thước trang (kẹp 1–100) |
| `sortBy` | string | `id` | `id` \| `name` \| `status` \| `createdAt` |
| `sortDirection` | string | `ASC` | `ASC` \| `DESC` |

Ví dụ: `GET /api/contact-requests?status=NEW&sortBy=createdAt&sortDirection=DESC&page=1&size=20`

Response `data` là `PageResponse<ContactRequestResponse>` (xem cấu trúc `PageResponse` ở mục 4).

### 3.3 Chi tiết (admin)

`GET /api/contact-requests/{id}` → `data` là 1 yêu cầu. Lỗi `4056` nếu không tìm thấy.

### 3.4 Đổi trạng thái (admin)

`PUT /api/contact-requests/{id}`

```json
{
  "status": "HANDLED"   // bắt buộc: NEW | HANDLED | CLOSED
}
```

Chuyển sang `HANDLED`/`CLOSED` → `handledBy` tự gán là admin đang đăng nhập. Response trả bản ghi sau
cập nhật. Lỗi `4056` nếu không tìm thấy, `4001` nếu thiếu/sai `status`.

### 3.5 Xoá (admin)

`DELETE /api/contact-requests/{id}` → `data = null`, message `Deleted successfully`. Lỗi `4056` nếu không tìm thấy.

## 4. DTO cho FE (TypeScript)

```typescript
type ContactStatus = 'NEW' | 'HANDLED' | 'CLOSED';

// Response
interface ContactRequestResponse {
  id: number;
  name: string;
  email: string | null;
  phone: string | null;
  content: string;
  status: ContactStatus;
  handledById: number | null;
  handledByUsername: string | null;
  createdAt: string; // ISO-8601 Instant
  updatedAt: string;
}

// POST /api/contact-requests (công khai)
interface ContactRequestCreateRequest {
  name: string;
  email?: string;
  phone?: string;
  content: string;
}

// PUT /api/contact-requests/{id} (admin)
interface ContactRequestUpdateRequest {
  status: ContactStatus;
}

// GET /api/contact-requests (admin) — query params
interface ContactRequestSearchParams {
  name?: string;
  email?: string;
  phone?: string;
  status?: ContactStatus;
  page?: number;         // default 1
  size?: number;         // default 10, max 100
  sortBy?: 'id' | 'name' | 'status' | 'createdAt';
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
| POST | `/api/contact-requests` | Công khai | — | Gửi yêu cầu liên hệ |
| GET | `/api/contact-requests` | JWT | ADMIN/SUPERADMIN | Danh sách + filter |
| GET | `/api/contact-requests/{id}` | JWT | ADMIN/SUPERADMIN | Chi tiết |
| PUT | `/api/contact-requests/{id}` | JWT | ADMIN/SUPERADMIN | Đổi trạng thái |
| DELETE | `/api/contact-requests/{id}` | JWT | ADMIN/SUPERADMIN | Xoá |

## 6. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Gửi liên hệ là công khai; các endpoint quản lý cần `<ACCESS_TOKEN>` ADMIN/SUPERADMIN.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# Gửi yêu cầu liên hệ (CÔNG KHAI — không cần token)
curl -X POST http://localhost:8080/api/contact-requests \
  -H "Content-Type: application/json" \
  -d '{"name":"Nguyễn Văn A","email":"a@example.com","phone":"0900123456","content":"Tôi cần tư vấn sản phẩm."}'

# List yêu cầu (ADMIN) — filter + phân trang
curl "http://localhost:8080/api/contact-requests?name=&email=&phone=&status=NEW&page=1&size=10&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Chi tiết (ADMIN)
curl http://localhost:8080/api/contact-requests/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# Đổi trạng thái (ADMIN) — tự gán người xử lý
curl -X PUT http://localhost:8080/api/contact-requests/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"status":"HANDLED"}'

# Xoá (ADMIN)
curl -X DELETE http://localhost:8080/api/contact-requests/1 -H "Authorization: Bearer <ACCESS_TOKEN>"
```
