# COUPON API — Mã giảm giá

Tài liệu bàn giao FE cho module **Coupon (mã giảm giá)**.

## 1. Tổng quan & phân quyền

- **Quản lý (list/chi tiết/tạo/sửa/xoá) chỉ dành cho `ADMIN`/`SUPERADMIN`** — cố ý **không** phơi
  danh sách mã ra công khai (tránh lộ mã ẩn).
- **`POST /api/coupons/validate` là công khai** — dùng cho giỏ hàng: nhập mã → kiểm tra + tính số tiền giảm.
- `code` **duy nhất**, luôn được chuẩn hoá **viết hoa & trim** khi lưu/kiểm tra.
- **PUT là partial update**: chỉ gửi field muốn đổi; field không gửi (null) → giữ nguyên. Ràng buộc "bắt buộc" chỉ áp dụng khi **tạo mới (POST)**.
- Loại giảm giá (`discountType`):
  - `PERCENT` — `discountValue` là %; số giảm = `orderAmount * value / 100`, chặn trần bởi `maxDiscountAmount` (nếu có).
  - `FIXED` — `discountValue` là số tiền giảm cố định (không vượt quá giá trị đơn).
  - `FREE_SHIP` — miễn phí ship; `discountAmount = 0` và cờ `freeShipping = true` (phí ship do tầng đơn hàng/vận chuyển xử lý).
- Số tiền giảm luôn được kẹp trong `[0, orderAmount]`.

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation |
| 4010 | 401 | Chưa xác thực |
| 4030 | 403 | Không đủ quyền |
| 4052 | 404 | Không tìm thấy coupon (theo id) |
| 4099 | 409 | Code coupon đã tồn tại |

## 3. Endpoint

`sortBy` cho phép: `id, code, discountValue, usedCount, startsAt, endsAt, createdAt`. Phân trang 1-based
(`page` mặc định 1, `size` 10 kẹp 1–100). Filter list: `code` (like), `discountType`, `isActive`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/coupons` | ADMIN/SUPERADMIN | Danh sách + filter |
| GET | `/api/coupons/{id}` | ADMIN/SUPERADMIN | Chi tiết |
| POST | `/api/coupons` | ADMIN/SUPERADMIN | Tạo mới |
| PUT | `/api/coupons/{id}` | ADMIN/SUPERADMIN | Cập nhật |
| DELETE | `/api/coupons/{id}` | ADMIN/SUPERADMIN | Xoá |
| POST | `/api/coupons/validate` | **Công khai** | Kiểm tra & tính giảm giá cho giỏ hàng |

### 3.1 Tạo / cập nhật

```json
{
  "code": "SALE10",             // bắt buộc, ≤50, duy nhất (tự viết hoa)
  "description": "Giảm 10%",    // tuỳ chọn, ≤500
  "discountType": "PERCENT",    // bắt buộc: PERCENT | FIXED | FREE_SHIP
  "discountValue": 10,          // bắt buộc, > 0 (PERCENT: %, FIXED: số tiền)
  "minOrderAmount": 200000,     // tuỳ chọn (đơn tối thiểu để áp dụng)
  "maxDiscountAmount": 50000,   // tuỳ chọn (trần giảm cho PERCENT)
  "usageLimit": 1000,           // tuỳ chọn (tổng lượt dùng; null = không giới hạn)
  "usageLimitPerUser": 1,       // tuỳ chọn (giới hạn theo user; kiểm tra khi có đơn hàng)
  "startsAt": "2026-07-01T00:00:00Z", // tuỳ chọn
  "endsAt": "2026-07-31T23:59:59Z",   // tuỳ chọn
  "isActive": true              // tuỳ chọn (mặc định true)
}
```

`CouponResponse`: `{id, code, description, discountType, discountValue, minOrderAmount,
maxDiscountAmount, usageLimit, usageLimitPerUser, usedCount, startsAt, endsAt, isActive,
createdAt, updatedAt}`. Lưu ý: `usedCount` chỉ đọc (không nhận qua create/update).

Lỗi: `4001` (validation), `4099` (code trùng), `4052` (không tìm thấy khi update/get).

### 3.2 Áp mã (validate) — công khai

Request:

```json
{ "code": "SALE10", "orderAmount": 500000 }   // orderAmount = tạm tính giỏ hàng (VND)
```

Response `200` — `data` là `CouponValidationResponse` (**luôn 200**, kể cả khi mã không hợp lệ):

```json
{
  "valid": true,
  "code": "SALE10",
  "discountType": "PERCENT",
  "discountAmount": 50000,   // số tiền được giảm (đã chặn trần & kẹp theo đơn)
  "freeShipping": false,
  "message": "Áp dụng mã giảm giá thành công"
}
```

Khi không hợp lệ (không tồn tại / vô hiệu hoá / chưa hiệu lực / hết hạn / hết lượt / chưa đạt đơn tối
thiểu): `valid=false`, `discountAmount=0`, `message` mô tả lý do (tiếng Việt) — FE hiển thị inline.

> **Giới hạn theo user (`usageLimitPerUser`)** hiện chỉ lưu, **chưa kiểm tra** ở bước validate vì cần
> đếm `orders.coupon_id` theo user — sẽ bổ sung khi làm module Đơn hàng. `usedCount` cũng sẽ được
> tăng khi đặt hàng thành công (thuộc module Đơn hàng).

## 4. DTO cho FE (TypeScript)

```ts
export type DiscountType = 'PERCENT' | 'FIXED' | 'FREE_SHIP';

export interface CouponResponse {
  id: number; code: string; description: string | null;
  discountType: DiscountType; discountValue: number;
  minOrderAmount: number | null; maxDiscountAmount: number | null;
  usageLimit: number | null; usageLimitPerUser: number | null; usedCount: number;
  startsAt: string | null; endsAt: string | null; isActive: boolean;
  createdAt: string; updatedAt: string;
}

export interface CouponValidateRequest { code: string; orderAmount: number; }

export interface CouponValidationResponse {
  valid: boolean; code: string; discountType: DiscountType | null;
  discountAmount: number; freeShipping: boolean; message: string;
}
```

## 5. Bảng tổng hợp

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/coupons` | ADMIN/SUPERADMIN | List coupon |
| GET | `/api/coupons/{id}` | ADMIN/SUPERADMIN | Chi tiết coupon |
| POST | `/api/coupons` | ADMIN/SUPERADMIN | Tạo coupon |
| PUT | `/api/coupons/{id}` | ADMIN/SUPERADMIN | Sửa coupon |
| DELETE | `/api/coupons/{id}` | ADMIN/SUPERADMIN | Xoá coupon |
| POST | `/api/coupons/validate` | Công khai | Áp mã, tính giảm giá (giỏ hàng) |

---

## 6. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Quản lý coupon cần `<ACCESS_TOKEN>` ADMIN/SUPERADMIN; `validate` là công khai.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# List coupon (ADMIN) — filter + phân trang
curl "http://localhost:8080/api/coupons?code=&discountType=PERCENT&isActive=true&page=1&size=10&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Chi tiết (ADMIN)
curl http://localhost:8080/api/coupons/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# Tạo (ADMIN)
curl -X POST http://localhost:8080/api/coupons \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"code":"SALE10","description":"Giảm 10%","discountType":"PERCENT","discountValue":10,"minOrderAmount":200000,"maxDiscountAmount":50000,"usageLimit":1000,"usageLimitPerUser":1,"startsAt":"2026-07-01T00:00:00Z","endsAt":"2026-07-31T23:59:59Z","isActive":true}'

# Cập nhật (ADMIN) — partial
curl -X PUT http://localhost:8080/api/coupons/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"discountValue":15,"isActive":false}'

# Xoá (ADMIN)
curl -X DELETE http://localhost:8080/api/coupons/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# Áp mã / tính giảm giá (CÔNG KHAI — không cần token)
curl -X POST http://localhost:8080/api/coupons/validate \
  -H "Content-Type: application/json" \
  -d '{"code":"SALE10","orderAmount":500000}'
```
