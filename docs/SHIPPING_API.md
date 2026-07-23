# SHIPPING API — Phương thức vận chuyển

Tài liệu bàn giao FE cho module **Shipping Method (phương thức vận chuyển)**.

## 1. Tổng quan & phân quyền

- **Đọc (list/chi tiết) là công khai** — storefront gọi lúc checkout để hiển thị các phương thức
  vận chuyển đang bật (`isActive=true`) và phí tương ứng.
- **Quản lý (tạo/sửa/xoá) chỉ dành cho `ADMIN`/`SUPERADMIN`**.
- `fee` tính bằng **VND** (số nguyên, không âm).
- **PUT là partial update**: chỉ gửi field muốn đổi; field không gửi (null) → giữ nguyên. Ràng buộc
  "bắt buộc" chỉ áp dụng khi **tạo mới (POST)**.
- **Tích hợp với đặt hàng**: FE gửi `shippingMethodId` (tuỳ chọn) trong `POST /api/orders`. Server tự
  tra `fee` tại thời điểm đặt hàng và cộng vào `totalAmount` — xem `docs/ORDER_API.md`. Phương thức
  bị **vô hiệu hoá (`isActive=false`)** sẽ bị từ chối ở bước đặt hàng (lỗi `4060`), tránh khách chọn
  nhầm một phương thức đã ngừng dùng.

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation |
| 4010 | 401 | Chưa xác thực |
| 4030 | 403 | Không đủ quyền |
| 4060 | 404 | Không tìm thấy phương thức vận chuyển (id sai hoặc đã bị vô hiệu hoá khi đặt hàng) |

## 3. Endpoint

`sortBy` cho phép: `id, name, fee, sortOrder, createdAt` (mặc định `sortOrder`). Phân trang 1-based
(`page` mặc định 1, `size` 10 kẹp 1–100). Filter list: `name` (like), `isActive`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/shipping-methods` | **Công khai** | Danh sách + filter |
| GET | `/api/shipping-methods/{id}` | **Công khai** | Chi tiết |
| POST | `/api/shipping-methods` | ADMIN/SUPERADMIN | Tạo mới |
| PUT | `/api/shipping-methods/{id}` | ADMIN/SUPERADMIN | Cập nhật |
| DELETE | `/api/shipping-methods/{id}` | ADMIN/SUPERADMIN | Xoá |

### 3.1 Danh sách (storefront: lấy các phương thức đang bật)

`GET /api/shipping-methods?isActive=true&sortBy=sortOrder&sortDirection=ASC&size=100`

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `name` | string | — | Lọc theo tên (chứa, không phân biệt hoa thường) |
| `isActive` | boolean | — | Lọc theo trạng thái bật/tắt |
| `page` | int | 1 | Trang (1-based) |
| `size` | int | 10 | Kích thước trang (kẹp 1–100) |
| `sortBy` | string | `sortOrder` | `id` \| `name` \| `fee` \| `sortOrder` \| `createdAt` |
| `sortDirection` | string | `ASC` | `ASC` \| `DESC` |

Response `200` — `data` là `PageResponse<ShippingMethodResponse>`:

```json
{
  "code": 1000,
  "message": "OK",
  "data": {
    "content": [
      { "id": 1, "name": "Giao hàng tiêu chuẩn", "fee": 30000, "sortOrder": 0, "isActive": true,
        "createdAt": "2026-07-01T00:00:00Z", "updatedAt": "2026-07-01T00:00:00Z" },
      { "id": 2, "name": "Giao hàng hoả tốc", "fee": 60000, "sortOrder": 1, "isActive": true,
        "createdAt": "2026-07-01T00:00:00Z", "updatedAt": "2026-07-01T00:00:00Z" }
    ],
    "pageNumber": 1, "pageSize": 100, "totalElements": 2, "totalPages": 1, "first": true, "last": true
  },
  "timestamp": "2026-07-22T00:00:00Z"
}
```

### 3.2 Chi tiết

`GET /api/shipping-methods/{id}` → `data` là `ShippingMethodResponse`. Lỗi `4060` nếu không tồn tại.

### 3.3 Tạo / cập nhật

```json
{
  "name": "Giao hàng tiêu chuẩn",   // bắt buộc, ≤100
  "fee": 30000,                     // bắt buộc, ≥0 (VND)
  "sortOrder": 0,                   // tuỳ chọn (mặc định 0)
  "isActive": true                  // tuỳ chọn (mặc định true)
}
```

Response `200` — `data` là `ShippingMethodResponse`. Lỗi: `4001` (validation), `4060` (không tìm thấy
khi update/delete), `4030` (không đủ quyền).

### 3.4 Xoá

`DELETE /api/shipping-methods/{id}` → xoá cứng. Lỗi `4060` nếu không tồn tại.

## 4. DTO cho FE (TypeScript)

```ts
export interface ShippingMethodResponse {
  id: number;
  name: string;
  fee: number;          // VND
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ShippingMethodCreateRequest {
  name: string;
  fee: number;
  sortOrder?: number;   // mặc định 0
  isActive?: boolean;   // mặc định true
}

export interface ShippingMethodUpdateRequest {
  name?: string;
  fee?: number;
  sortOrder?: number;
  isActive?: boolean;
}
```

## 5. Bảng tổng hợp

| Method | Path | Auth | Quyền | Mô tả |
|---|---|---|---|---|
| GET | `/api/shipping-methods` | — | Công khai | List phương thức vận chuyển |
| GET | `/api/shipping-methods/{id}` | — | Công khai | Chi tiết |
| POST | `/api/shipping-methods` | JWT | ADMIN/SUPERADMIN | Tạo mới |
| PUT | `/api/shipping-methods/{id}` | JWT | ADMIN/SUPERADMIN | Cập nhật (partial) |
| DELETE | `/api/shipping-methods/{id}` | JWT | ADMIN/SUPERADMIN | Xoá |

---

## 6. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. List/chi tiết công khai; tạo/sửa/xoá cần `<ACCESS_TOKEN>` ADMIN/SUPERADMIN.

```bash
# Danh sách phương thức đang bật (storefront checkout)
curl "http://localhost:8080/api/shipping-methods?isActive=true&sortBy=sortOrder&sortDirection=ASC&size=100"

# Chi tiết
curl http://localhost:8080/api/shipping-methods/1

# Tạo (ADMIN)
curl -X POST http://localhost:8080/api/shipping-methods \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Giao hàng tiêu chuẩn","fee":30000,"sortOrder":0,"isActive":true}'

# Cập nhật (ADMIN) — partial
curl -X PUT http://localhost:8080/api/shipping-methods/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"fee":35000}'

# Xoá (ADMIN)
curl -X DELETE http://localhost:8080/api/shipping-methods/1 -H "Authorization: Bearer <ACCESS_TOKEN>"
```
