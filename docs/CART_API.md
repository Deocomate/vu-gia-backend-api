# CART API — Giỏ hàng

Tài liệu bàn giao FE cho module **Cart (giỏ hàng theo user đăng nhập)**.

## 1. Tổng quan & phân quyền

- **Tất cả endpoint yêu cầu đăng nhập** (JWT). Giỏ hàng luôn thuộc **chính user đang đăng nhập** — không có
  truy cập chéo giữa các user, nên chỉ cần xác thực (mọi role: `CUSTOMER`/`ADMIN`/`SUPERADMIN`).
- Mỗi cặp `(user, product)` chỉ có **1 dòng** trong giỏ: thêm lại sản phẩm đã có → **cộng dồn** `quantity`.
- **Mọi thao tác đều trả về toàn bộ giỏ hàng** (danh sách item + `totalQuantity` + `totalAmount`) để FE
  render lại trong 1 lượt gọi — không cần gọi lại `GET /api/cart`.
- `unitPrice` lấy **giá hiện tại** của sản phẩm; `lineTotal = unitPrice * quantity`.
- `comboItems` là chuỗi JSON, chỉ có ý nghĩa khi sản phẩm là **COMBO** (FE tự định nghĩa cấu trúc).
- Thao tác trên item **không thuộc giỏ của mình** → trả `4058` (coi như không tìm thấy).

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation (thiếu `productId`/`quantity`, `quantity` ≤ 0) |
| 4010 | 401 | Chưa xác thực |
| 4044 | 404 | Không tìm thấy sản phẩm (`productId` không tồn tại) |
| 4058 | 404 | Không tìm thấy cart item (id sai hoặc không thuộc giỏ của bạn) |

## 3. Endpoint

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/cart` | Đăng nhập | Lấy giỏ hàng hiện tại |
| POST | `/api/cart/items` | Đăng nhập | Thêm sản phẩm vào giỏ (cộng dồn nếu đã có) |
| PUT | `/api/cart/items/{itemId}` | Đăng nhập | Đặt lại số lượng của 1 dòng |
| DELETE | `/api/cart/items/{itemId}` | Đăng nhập | Xoá 1 dòng khỏi giỏ |
| DELETE | `/api/cart` | Đăng nhập | Xoá sạch giỏ |

### 3.1 Lấy giỏ hàng

`GET /api/cart`

Response `200`:

```json
{
  "code": 1000,
  "message": null,
  "data": {
    "items": [
      {
        "id": 1,
        "productId": 10,
        "productName": "Ghế sofa Milano",
        "productThumb": "https://.../sofa.jpg",
        "productSlug": "ghe-sofa-milano",
        "unitPrice": 5000000,
        "quantity": 2,
        "comboItems": null,
        "lineTotal": 10000000
      }
    ],
    "totalQuantity": 2,
    "totalAmount": 10000000
  },
  "timestamp": "2026-07-11T02:00:00Z"
}
```

### 3.2 Thêm vào giỏ

`POST /api/cart/items`

```json
{
  "productId": 10,       // bắt buộc, phải tồn tại
  "quantity": 2,         // bắt buộc, > 0
  "comboItems": null     // tuỳ chọn, chuỗi JSON (chỉ dùng cho sản phẩm COMBO)
}
```

- Nếu `(user, product)` đã có trong giỏ → `quantity` được **cộng dồn** (không tạo dòng mới).
- Response `200`: trả **toàn bộ giỏ** sau khi thêm (message `Added to cart`).
- Lỗi: `4001` (thiếu/`quantity` ≤ 0), `4044` (sản phẩm không tồn tại).

### 3.3 Cập nhật số lượng

`PUT /api/cart/items/{itemId}`

```json
{
  "quantity": 5   // bắt buộc, > 0 (đặt lại số lượng tuyệt đối)
}
```

Response `200`: trả toàn bộ giỏ (message `Updated successfully`). Lỗi `4058` nếu item không tồn tại /
không thuộc giỏ của bạn, `4001` nếu `quantity` ≤ 0.

### 3.4 Xoá 1 dòng

`DELETE /api/cart/items/{itemId}` → trả toàn bộ giỏ sau khi xoá (message `Removed from cart`). Lỗi `4058`
nếu item không thuộc giỏ của bạn.

### 3.5 Xoá sạch giỏ

`DELETE /api/cart` → trả giỏ rỗng (`items: []`, totals = 0), message `Cart cleared`.

## 4. DTO cho FE (TypeScript)

```typescript
// Response
interface CartItemResponse {
  id: number;            // id của dòng giỏ hàng (dùng cho update/remove)
  productId: number;
  productName: string;
  productThumb: string;
  productSlug: string;
  unitPrice: number;     // giá hiện tại
  quantity: number;
  comboItems: string | null;
  lineTotal: number;     // unitPrice * quantity
}

interface CartResponse {
  items: CartItemResponse[];
  totalQuantity: number;
  totalAmount: number;
}

// POST /api/cart/items
interface CartItemAddRequest {
  productId: number;
  quantity: number;      // > 0
  comboItems?: string;
}

// PUT /api/cart/items/{itemId}
interface CartItemUpdateRequest {
  quantity: number;      // > 0
}

// Envelope chung
interface ApiResponse<T> {
  code: number;          // 1000 = success
  message: string | null;
  data: T;
  timestamp: string;
}
```

## 5. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Mọi request cần `<ACCESS_TOKEN>` của user đang đăng nhập.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# Lấy giỏ hàng
curl http://localhost:8080/api/cart -H "Authorization: Bearer <ACCESS_TOKEN>"

# Thêm vào giỏ (cộng dồn nếu đã có)
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"productId":10,"quantity":2}'

# Thêm sản phẩm COMBO kèm cấu hình
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"productId":22,"quantity":1,"comboItems":"[{\"productId\":5,\"quantity\":1},{\"productId\":6,\"quantity\":2}]"}'

# Cập nhật số lượng 1 dòng
curl -X PUT http://localhost:8080/api/cart/items/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"quantity":5}'

# Xoá 1 dòng
curl -X DELETE http://localhost:8080/api/cart/items/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# Xoá sạch giỏ
curl -X DELETE http://localhost:8080/api/cart -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## 6. Tổng hợp endpoint

| Method | Path | Auth | Quyền | Mô tả |
|---|---|---|---|---|
| GET | `/api/cart` | JWT | Đăng nhập | Lấy giỏ hàng |
| POST | `/api/cart/items` | JWT | Đăng nhập | Thêm sản phẩm (cộng dồn) |
| PUT | `/api/cart/items/{itemId}` | JWT | Đăng nhập | Cập nhật số lượng |
| DELETE | `/api/cart/items/{itemId}` | JWT | Đăng nhập | Xoá 1 dòng |
| DELETE | `/api/cart` | JWT | Đăng nhập | Xoá sạch giỏ |
