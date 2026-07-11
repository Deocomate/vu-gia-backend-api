# DASHBOARD API — Thống kê quản trị

Tài liệu bàn giao FE cho **Dashboard admin** (số liệu tổng quan, biểu đồ doanh thu, top sản phẩm).

## 1. Tổng quan & phân quyền

- **Toàn bộ endpoint chỉ dành cho `ADMIN`/`SUPERADMIN`** (guard ở cấp class controller).
- Các con số **doanh thu** (`revenue`) chỉ tính đơn có `paymentStatus = PAID`.
- Đơn vị tiền là VND (số nguyên `long`).

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4010 | 401 | Chưa xác thực |
| 4030 | 403 | Không đủ quyền (không phải ADMIN/SUPERADMIN) |

## 3. Endpoint

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/admin/dashboard/summary` | ADMIN/SUPERADMIN | Số liệu tổng quan (KPI cards) |
| GET | `/api/admin/dashboard/revenue` | ADMIN/SUPERADMIN | Doanh thu đã thanh toán theo ngày (biểu đồ) |
| GET | `/api/admin/dashboard/top-products` | ADMIN/SUPERADMIN | Top sản phẩm bán chạy |

### 3.1 Số liệu tổng quan

`GET /api/admin/dashboard/summary`

Response `200`:

```json
{
  "code": 1000,
  "message": null,
  "data": {
    "totalOrders": 128,
    "paidOrders": 96,
    "totalRevenue": 512000000,
    "todayOrders": 5,
    "todayRevenue": 12000000,
    "monthOrders": 40,
    "monthRevenue": 210000000,
    "ordersByStatus": {
      "PENDING_PAYMENT": 20,
      "PROCESSING": 15,
      "SHIPPING": 8,
      "COMPLETED": 80,
      "CANCELLED": 4,
      "RETURNED": 1
    },
    "totalCustomers": 320,
    "totalProducts": 85,
    "totalProductsSold": 1540,
    "newContactRequests": 3,
    "activeSubscribers": 540
  },
  "timestamp": "2026-07-11T02:00:00Z"
}
```

- `ordersByStatus`: **luôn đủ 6 key** (trạng thái không có đơn = 0).
- `todayOrders/todayRevenue`, `monthOrders/monthRevenue`: mốc "hôm nay"/"tháng này" theo múi giờ server.
- `totalProductsSold`: tổng số lượng đã bán, **chỉ tính đơn `COMPLETED`** (khớp với `product.soldCount`).
- `newContactRequests`: số yêu cầu liên hệ đang ở trạng thái `NEW`.
- `activeSubscribers`: số subscriber newsletter đang `isActive = true`.

### 3.2 Doanh thu theo ngày

`GET /api/admin/dashboard/revenue`

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `from` | ISO-8601 | now − 30 ngày | Mốc bắt đầu (`created_at >=`) |
| `to` | ISO-8601 | now | Mốc kết thúc (`created_at <=`) |

Ví dụ: `GET /api/admin/dashboard/revenue?from=2026-07-01T00:00:00Z&to=2026-07-31T23:59:59Z`

Response `200` — mảng điểm theo ngày (chỉ những ngày **có** đơn PAID):

```json
{
  "code": 1000,
  "message": null,
  "data": [
    { "date": "2026-07-01", "revenue": 12000000, "orders": 4 },
    { "date": "2026-07-02", "revenue": 8500000,  "orders": 3 }
  ],
  "timestamp": "2026-07-11T02:00:00Z"
}
```

> Chỉ trả về ngày có phát sinh doanh thu PAID. Nếu FE cần trục ngày liên tục (kể cả ngày 0đ), tự
> fill các ngày trống ở client.

### 3.3 Top sản phẩm bán chạy

`GET /api/admin/dashboard/top-products`

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `limit` | int | 10 | Số sản phẩm trả về (kẹp 1–50) |

Sắp xếp theo **tổng số lượng bán** giảm dần (tổng hợp trên toàn bộ `order_items`).

```json
{
  "code": 1000,
  "message": null,
  "data": [
    { "productId": 10, "productName": "Ghế sofa Milano", "totalQuantity": 120, "totalRevenue": 600000000 },
    { "productId": 22, "productName": "Bàn trà gỗ sồi",  "totalQuantity": 85,  "totalRevenue": 170000000 }
  ],
  "timestamp": "2026-07-11T02:00:00Z"
}
```

## 4. DTO cho FE (TypeScript)

```typescript
interface DashboardSummaryResponse {
  totalOrders: number;
  paidOrders: number;
  totalRevenue: number;      // PAID
  todayOrders: number;
  todayRevenue: number;
  monthOrders: number;
  monthRevenue: number;
  ordersByStatus: Record<
    'PENDING_PAYMENT' | 'PROCESSING' | 'SHIPPING' | 'COMPLETED' | 'CANCELLED' | 'RETURNED',
    number
  >;
  totalCustomers: number;
  totalProducts: number;
  totalProductsSold: number;   // chỉ tính đơn COMPLETED
  newContactRequests: number;
  activeSubscribers: number;
}

interface RevenuePointResponse {
  date: string;    // yyyy-MM-dd
  revenue: number; // PAID
  orders: number;
}

interface TopProductResponse {
  productId: number;
  productName: string;
  totalQuantity: number;
  totalRevenue: number;
}
```

## 5. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Cần `<ADMIN_ACCESS_TOKEN>` (ADMIN/SUPERADMIN).
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# KPI tổng quan
curl http://localhost:8080/api/admin/dashboard/summary \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>"

# Doanh thu theo ngày (mặc định 30 ngày gần nhất)
curl "http://localhost:8080/api/admin/dashboard/revenue?from=2026-07-01T00:00:00Z&to=2026-07-31T23:59:59Z" \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>"

# Top 10 sản phẩm bán chạy
curl "http://localhost:8080/api/admin/dashboard/top-products?limit=10" \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>"
```

## 6. Tổng hợp endpoint

| Method | Path | Auth | Quyền | Mô tả |
|---|---|---|---|---|
| GET | `/api/admin/dashboard/summary` | JWT | ADMIN/SUPERADMIN | KPI tổng quan |
| GET | `/api/admin/dashboard/revenue` | JWT | ADMIN/SUPERADMIN | Doanh thu theo ngày |
| GET | `/api/admin/dashboard/top-products` | JWT | ADMIN/SUPERADMIN | Top sản phẩm bán chạy |
