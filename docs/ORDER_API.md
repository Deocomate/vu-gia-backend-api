# ORDER API — Đặt hàng

Tài liệu bàn giao FE cho module **Order (đặt hàng)**.

## 1. Tổng quan & phân quyền

- **Tất cả endpoint yêu cầu đăng nhập** (JWT). Đặt hàng / xem đơn / huỷ đơn thao tác trên đơn của **chính
  user đang đăng nhập** (ADMIN/SUPERADMIN có thể thao tác trên mọi đơn); đổi trạng thái đơn là
  **ADMIN/SUPERADMIN**.
- **Huỷ đơn (khách tự huỷ)**: chỉ cho phép khi đơn đang `PENDING_PAYMENT`/`PROCESSING`. Huỷ thành công →
  `status = CANCELLED`, nếu đơn có áp coupon thì **hoàn lại 1 lượt dùng** của coupon đó (`usedCount - 1`,
  không âm). Hiện tại **chưa có mô hình tồn kho** trong hệ thống nên không có bước hoàn kho.
- **Snapshot khi đặt hàng**: FE gửi danh sách `{productId, quantity}` (KHÔNG gửi `cartId`). Server tự
  đọc sản phẩm sống và **đóng băng** `productName / productType / unitPrice / subtotal` vào `order_items`.
  Đơn hàng vì thế không đổi khi sau này sản phẩm bị sửa giá/tên.
- **Idempotency (chống double-submit)**: FE **bắt buộc** gửi `idempotencyKey` (ví dụ 1 UUID sinh khi mở
  màn thanh toán). Gửi lại cùng key → server trả **đúng đơn đã tạo**, không tạo đơn thứ 2, không trừ
  coupon/giỏ lần nữa, không gửi lại email. Bảo vệ ở tầng DB bằng unique index `(user_id, idempotency_key)`,
  nên kể cả 2 request đồng thời cũng chỉ 1 đơn được tạo.
- **Mã giảm giá**: nếu có `couponCode`, server kiểm tra **đủ mọi điều kiện** (đang bật, trong thời gian
  hiệu lực, chưa hết lượt tổng, chưa hết lượt theo user, đạt giá trị tối thiểu) rồi mới áp. Việc trừ lượt
  dùng bằng **1 câu UPDATE nguyên tử có điều kiện** (`used_count + 1` chỉ khi còn lượt) → an toàn tranh
  chấp, không cần khoá.
- **Email bất đồng bộ**: ghi DB thành công (commit) mới gửi email xác nhận, chạy **async** (`@Async` +
  `@TransactionalEventListener(AFTER_COMMIT)`) nên không chặn response; email lỗi cũng không ảnh hưởng đơn.
  Email là **HTML** (render từ template Thymeleaf `resources/templates/email/order-confirmation.html`,
  gửi bằng `MimeMessage` UTF-8) — có bảng sản phẩm, tạm tính/giảm giá/tổng, và thông tin nhận hàng.
  Cần cấu hình SMTP thật (`spring.mail.*`) thì email mới gửi được; chưa cấu hình thì chỉ log lỗi.
- **Giỏ hàng**: sau khi đặt thành công, mỗi `(productId, quantity)` đã đặt sẽ được **trừ/xoá** khỏi giỏ của
  khách (mua đủ hoặc quá số trong giỏ → xoá dòng; mua ít hơn → giảm số lượng).
- Trạng thái đơn khởi tạo: `status = PENDING_PAYMENT`, `paymentStatus = PENDING`.
- **Phương thức thanh toán** (`paymentMethod`): `COD` (mặc định) hoặc `ONL` (chuyển khoản VietQR).
  - `COD` → xác nhận ngay, **gửi email luôn** (như trên).
  - `ONL` → response trả kèm object **`payment`** (ảnh QR VietQR + số tiền + mã đơn + thông tin CK);
    **CHƯA gửi email** (email sẽ gửi sau khi thanh toán được xác nhận qua webhook — *làm sau*).
    Nội dung chuyển khoản (`des` / `transferContent`) = **mã đơn hàng** để đối soát.
- **Đếm "đã bán" (`product.soldCount`)**: chỉ cộng khi đơn chuyển sang **`COMPLETED`** (không cộng lúc đặt,
  tránh sai lệch khi khách huỷ/hoàn tiền). Nếu đơn **rời khỏi `COMPLETED`** (vd `RETURNED`) thì **trừ lại**
  đúng số lượng. Cộng/trừ bằng UPDATE nguyên tử theo từng sản phẩm.
- **Phí vận chuyển (`shippingMethodId`)**: tuỳ chọn, tham chiếu `GET /api/shipping-methods` (xem
  `docs/SHIPPING_API.md`). Nếu gửi, server tra `fee` của phương thức tại thời điểm đặt hàng và **cộng
  vào `totalAmount`**; phương thức không tồn tại hoặc đã bị vô hiệu hoá (`isActive=false`) → lỗi `4060`.
  Không gửi (hoặc client cũ chưa hỗ trợ) → `shippingFee = 0`, không ảnh hưởng đơn hàng cũ.
  **Coupon `FREE_SHIP`**: nếu mã áp dụng có `discountType = FREE_SHIP`, phí ship được **miễn** khỏi
  `totalAmount` (không cộng vào tổng phải trả); `shippingFee` trong response vẫn trả về mức phí gốc của
  phương thức đã chọn để FE hiển thị đúng "phí ship: 30.000đ → được miễn" thay vì ẩn đi.

## 2. Envelope & mã lỗi

Envelope `{code, message, data, timestamp}`, `code=1000` = thành công.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation (thiếu `items`/`idempotencyKey`/thông tin người nhận, `quantity` ≤ 0) |
| 4010 | 401 | Chưa xác thực |
| 4030 | 403 | Không đủ quyền (đổi trạng thái mà không phải ADMIN/SUPERADMIN) |
| 4044 | 404 | Sản phẩm trong đơn không tồn tại |
| 4052 | 404 | Mã giảm giá không tồn tại |
| 4059 | 404 | Không tìm thấy đơn hàng (id sai hoặc không thuộc bạn) |
| 4060 | 404 | Phương thức vận chuyển không tồn tại hoặc đã bị vô hiệu hoá |
| 4105 | 409 | Mã giảm giá không đủ điều kiện áp dụng (message nêu rõ lý do) |
| 4107 | 409 | Đơn không thể huỷ ở trạng thái hiện tại (không phải `PENDING_PAYMENT`/`PROCESSING`) |

> `4105` dùng chung 1 code, **`message`** cho biết lý do cụ thể: *"Mã giảm giá đã bị vô hiệu hoá" /
> "chưa có hiệu lực" / "đã hết hạn" / "chưa đạt giá trị tối thiểu" / "bạn đã dùng hết lượt" / "đã hết lượt sử dụng"*.

## 3. Endpoint

Phân trang 1-based (`page` mặc định 1, `size` 10 kẹp 1–100). `sortBy` cho phép: `id, orderCode, totalAmount,
status, createdAt` (mặc định `id`, hướng mặc định `DESC`). Filter list: `status`, `paymentStatus`.

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/orders` | Đăng nhập | Đặt hàng (idempotent theo `idempotencyKey`) |
| GET | `/api/orders` | Đăng nhập | Danh sách đơn **của tôi** + filter + phân trang |
| GET | `/api/orders/admin` | ADMIN/SUPERADMIN | Search **toàn bộ đơn** (mọi user) + filter đầy đủ |
| GET | `/api/orders/{id}` | Đăng nhập | Chi tiết đơn (chủ đơn, hoặc ADMIN xem mọi đơn) |
| PATCH | `/api/orders/{id}/status` | ADMIN/SUPERADMIN | Đổi trạng thái đơn / thanh toán |
| POST | `/api/orders/{id}/cancel` | Đăng nhập | Khách tự huỷ đơn của mình (hoặc ADMIN huỷ mọi đơn) |

### 3.1 Đặt hàng

`POST /api/orders`

```json
{
  "idempotencyKey": "b3f1c2a4-...-uuid",   // bắt buộc, ≤100 (FE sinh, giữ nguyên khi retry)
  "items": [                                // bắt buộc, ≥1 phần tử
    { "productId": 10, "quantity": 2 },
    { "productId": 22, "quantity": 1 }
  ],
  "couponCode": "SALE10",                   // tuỳ chọn
  "paymentMethod": "ONL",                   // tuỳ chọn: COD (mặc định) | ONL
  "shippingMethodId": 1,                    // tuỳ chọn — id từ GET /api/shipping-methods; null/omit = phí ship 0
  "receiverName": "Nguyễn Văn A",           // bắt buộc, ≤100
  "receiverPhone": "0900123456",            // bắt buộc, ≤20
  "receiverAddress": "123 Lê Lợi, Q1, HCM", // bắt buộc
  "note": "Giao giờ hành chính"             // tuỳ chọn
}
```

Response `200`:

```json
{
  "code": 1000,
  "message": "Order placed",
  "data": {
    "id": 1,
    "orderCode": "OD3F1C2A4B5D6",
    "status": "PENDING_PAYMENT",
    "paymentStatus": "PENDING",
    "paymentMethod": "ONL",
    "payment": {
      "qrImageUrl": "https://vietqr.app/img?bank=MBBank&acc=686804076868&template=compact&amount=1200000&des=ODF97CD281BE0D&showinfo=true&holder=NGUYEN%20DUY%20DAT",
      "amount": 1200000,
      "bankName": "MBBank",
      "accountNumber": "686804076868",
      "accountHolder": "NGUYEN DUY DAT",
      "transferContent": "ODF97CD281BE0D"
    },
    "totalAmount": 1800000,
    "discountAmount": 200000,
    "couponCode": "SALE10",
    "shippingFee": 30000,
    "shippingMethodName": "Giao hàng tiêu chuẩn",
    "receiverName": "Nguyễn Văn A",
    "receiverPhone": "0900123456",
    "receiverAddress": "123 Lê Lợi, Q1, HCM",
    "note": "Giao giờ hành chính",
    "items": [
      {
        "id": 1,
        "productId": 10,
        "productName": "Ghế sofa Milano",
        "productType": "SINGLE",
        "unitPrice": 1000000,
        "quantity": 2,
        "subtotal": 2000000,
        "comboItems": null
      }
    ],
    "createdAt": "2026-07-11T02:00:00Z",
    "updatedAt": "2026-07-11T02:00:00Z"
  },
  "timestamp": "2026-07-11T02:00:00Z"
}
```

Lỗi có thể gặp: `4001` (body sai), `4044` (sản phẩm không tồn tại), `4052` (coupon không tồn tại),
`4060` (phương thức vận chuyển không tồn tại/đã tắt), `4105` (coupon không đủ điều kiện — xem `message`).

### 3.2 Danh sách đơn của tôi

`GET /api/orders`

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `status` | enum | — | `PENDING_PAYMENT` \| `PROCESSING` \| `SHIPPING` \| `COMPLETED` \| `CANCELLED` \| `RETURNED` |
| `paymentStatus` | enum | — | `PENDING` \| `PAID` \| `FAILED` \| `REFUNDED` |
| `page` | int | 1 | Trang (1-based) |
| `size` | int | 10 | Kích thước trang (kẹp 1–100) |
| `sortBy` | string | `id` | `id` \| `orderCode` \| `totalAmount` \| `status` \| `createdAt` |
| `sortDirection` | string | `DESC` | `ASC` \| `DESC` |

Ví dụ: `GET /api/orders?status=PENDING_PAYMENT&sortBy=createdAt&sortDirection=DESC&page=1&size=20` →
`data` là `PageResponse<OrderResponse>`.

### 3.2b Search toàn bộ đơn (admin)

`GET /api/orders/admin` — dành cho ADMIN/SUPERADMIN, xem đơn của **mọi user** với bộ lọc đầy đủ.

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `orderCode` | string | — | Lọc theo mã đơn (chứa, không phân biệt hoa thường) |
| `status` | enum | — | Trạng thái đơn |
| `paymentStatus` | enum | — | Trạng thái thanh toán |
| `userId` | number | — | Lọc theo 1 khách hàng cụ thể |
| `couponCode` | string | — | Lọc theo mã giảm giá (chứa) |
| `placedFrom` | ISO-8601 | — | Đặt từ ngày (>= `created_at`), vd `2026-07-01T00:00:00Z` |
| `placedTo` | ISO-8601 | — | Đặt đến ngày (<= `created_at`) |
| `page` | int | 1 | Trang (1-based) |
| `size` | int | 10 | Kích thước trang (kẹp 1–100) |
| `sortBy` | string | `id` | `id` \| `orderCode` \| `totalAmount` \| `status` \| `createdAt` |
| `sortDirection` | string | `DESC` | `ASC` \| `DESC` |

Ví dụ: `GET /api/orders/admin?status=PROCESSING&paymentStatus=PAID&placedFrom=2026-07-01T00:00:00Z&sortBy=createdAt&sortDirection=DESC&page=1&size=20`
→ `data` là `PageResponse<OrderResponse>`. Lỗi `4030` nếu không phải staff.

### 3.3 Chi tiết đơn

`GET /api/orders/{id}` → chủ đơn xem đơn của mình; ADMIN/SUPERADMIN xem mọi đơn. Lỗi `4059` nếu không
tồn tại / không thuộc bạn.

### 3.4 Đổi trạng thái (admin)

`PATCH /api/orders/{id}/status`

```json
{
  "status": "PROCESSING",       // bắt buộc
  "paymentStatus": "PAID"       // tuỳ chọn
}
```

Response `200` trả đơn sau cập nhật. Lỗi `4059` nếu không tìm thấy, `4001` nếu thiếu `status`, `4030` nếu
không phải staff.

> **Tác dụng phụ**: chuyển sang `COMPLETED` lần đầu → cộng `soldCount` cho từng sản phẩm theo số lượng;
> chuyển từ `COMPLETED` sang trạng thái khác (`RETURNED`…) → trừ lại. Đổi giữa các trạng thái không phải
> `COMPLETED` (vd `PROCESSING → SHIPPING`) không đụng tới `soldCount`.

### 3.5 Huỷ đơn (khách tự huỷ)

`POST /api/orders/{id}/cancel` — không có request body.

Chủ đơn (hoặc ADMIN/SUPERADMIN) có thể huỷ đơn của mình khi đơn đang ở trạng thái `PENDING_PAYMENT` hoặc
`PROCESSING`. Response `200` trả `OrderResponse` sau khi huỷ (`status = "CANCELLED"`).

Lỗi có thể gặp:
- `4059` — không tìm thấy đơn, **hoặc** đơn không thuộc về bạn (ẩn tồn tại — không phải chủ đơn/không phải
  staff nhận cùng lỗi 404 như `getById`, không lộ thông tin đơn tồn tại hay không).
- `4107` — đơn đang ở trạng thái không thể huỷ (đã `SHIPPING`/`COMPLETED`/`CANCELLED`/`RETURNED`).

> **Tác dụng phụ**: nếu đơn có áp mã giảm giá (`couponCode` != null), server **hoàn lại 1 lượt dùng** của
> coupon đó (`usedCount - 1`, không âm dù có huỷ đôi do race). Không có bước hoàn kho vì hệ thống chưa có
> mô hình tồn kho.

## 4. DTO cho FE (TypeScript)

```typescript
type OrderStatus = 'PENDING_PAYMENT' | 'PROCESSING' | 'SHIPPING' | 'COMPLETED' | 'CANCELLED' | 'RETURNED';
type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';
type ProductType = 'SINGLE' | 'COMBO';

interface OrderItemResponse {
  id: number;
  productId: number;
  productName: string;   // snapshot
  productType: ProductType;
  unitPrice: number;     // snapshot
  quantity: number;
  subtotal: number;
  comboItems: string | null;
}

type PaymentMethod = 'COD' | 'ONL';

// Thông tin thanh toán QR — chỉ có với đơn ONL chưa thanh toán, còn lại null
interface PaymentInfoResponse {
  qrImageUrl: string;        // link ảnh VietQR, FE render trực tiếp
  amount: number;
  bankName: string;
  accountNumber: string;
  accountHolder: string;
  transferContent: string;   // = orderCode (nội dung chuyển khoản)
}

interface OrderResponse {
  id: number;
  orderCode: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;
  payment: PaymentInfoResponse | null;   // != null khi ONL & chưa PAID
  totalAmount: number;   // sau giảm giá, đã cộng shippingFee (trừ khi coupon FREE_SHIP)
  discountAmount: number;
  couponCode: string | null;
  shippingFee: number;          // phí ship đã áp dụng (VND); 0 nếu không chọn phương thức
  shippingMethodName: string | null;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  note: string | null;
  items: OrderItemResponse[];
  createdAt: string;     // = ngày đặt hàng
  updatedAt: string;
}

// POST /api/orders
interface OrderItemRequest { productId: number; quantity: number; }
interface OrderPlaceRequest {
  idempotencyKey: string;   // FE sinh (UUID), giữ nguyên khi retry
  items: OrderItemRequest[];
  couponCode?: string;
  paymentMethod?: PaymentMethod;   // COD (mặc định) | ONL
  shippingMethodId?: number;       // id từ GET /api/shipping-methods; omit = phí ship 0
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  note?: string;
}

// PATCH /api/orders/{id}/status (admin)
interface OrderStatusUpdateRequest {
  status: OrderStatus;
  paymentStatus?: PaymentStatus;
}

interface PageResponse<T> {
  content: T[];
  pageNumber: number; pageSize: number;
  totalElements: number; totalPages: number;
  first: boolean; last: boolean;
}
```

## 5. Endpoint test nhanh (curl → dán vào Postman)

> Base URL `http://localhost:8080`. Mọi request cần `<ACCESS_TOKEN>`; đổi trạng thái cần token ADMIN.
> Postman: **Import → Raw text** rồi dán lệnh `curl`.

```bash
# Đặt hàng COD (idempotencyKey giữ nguyên khi bấm lại "Đặt hàng"), kèm phương thức vận chuyển
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"b3f1c2a4-0001","items":[{"productId":10,"quantity":2}],"couponCode":"SALE10","paymentMethod":"COD","shippingMethodId":1,"receiverName":"Nguyễn Văn A","receiverPhone":"0900123456","receiverAddress":"123 Lê Lợi, Q1, HCM","note":"Giao giờ hành chính"}'

# Đặt hàng ONL → response trả kèm "payment": { qrImageUrl, amount, transferContent=orderCode, ... }
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"b3f1c2a4-0002","items":[{"productId":10,"quantity":1}],"paymentMethod":"ONL","receiverName":"Nguyễn Văn A","receiverPhone":"0900123456","receiverAddress":"123 Lê Lợi, Q1, HCM"}'

# Danh sách đơn của tôi + filter
curl "http://localhost:8080/api/orders?status=PENDING_PAYMENT&page=1&size=20&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Search toàn bộ đơn (ADMIN) — filter đầy đủ
curl "http://localhost:8080/api/orders/admin?status=PROCESSING&paymentStatus=PAID&userId=5&placedFrom=2026-07-01T00:00:00Z&placedTo=2026-07-31T23:59:59Z&page=1&size=20&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>"

# Chi tiết đơn
curl http://localhost:8080/api/orders/1 -H "Authorization: Bearer <ACCESS_TOKEN>"

# Đổi trạng thái đơn (ADMIN)
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Authorization: Bearer <ADMIN_ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"status":"PROCESSING","paymentStatus":"PAID"}'

# Huỷ đơn (khách tự huỷ đơn của mình — chỉ khi PENDING_PAYMENT/PROCESSING)
curl -X POST http://localhost:8080/api/orders/1/cancel \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## 6. Tổng hợp endpoint

| Method | Path | Auth | Quyền | Mô tả |
|---|---|---|---|---|
| POST | `/api/orders` | JWT | Đăng nhập | Đặt hàng (idempotent) |
| GET | `/api/orders` | JWT | Đăng nhập | Danh sách đơn của tôi |
| GET | `/api/orders/admin` | JWT | ADMIN/SUPERADMIN | Search toàn bộ đơn + filter đầy đủ |
| GET | `/api/orders/{id}` | JWT | Đăng nhập | Chi tiết đơn (chủ đơn / admin) |
| PATCH | `/api/orders/{id}/status` | JWT | ADMIN/SUPERADMIN | Đổi trạng thái đơn / thanh toán |
| POST | `/api/orders/{id}/cancel` | JWT | Đăng nhập | Khách tự huỷ đơn (chủ đơn / admin), hoàn lượt coupon |
| POST | `/api/webhooks/sepay` | Chữ ký HMAC | — | Webhook SePay xác nhận thanh toán (server-to-server) |

## 7. Webhook thanh toán SePay — `POST /api/webhooks/sepay`

Endpoint **công khai** (không JWT), xác thực bằng **chữ ký HMAC-SHA256** ở header
`X-SePay-Signature: sha256=<hex>` = `HMAC_SHA256(rawBody, SEPAY_WEBHOOK_SECRET)`. Đặt secret qua env
`SEPAY_WEBHOOK_SECRET` (khớp với cấu hình bên SePay). **Fail-closed: chưa đặt secret → CHẶN mọi webhook (401).**

**Cấu hình mã thanh toán trên SePay**: tiền tố `OD`, phần biến **12 ký tự**, kiểu **chữ và số** (hexa `0-9A-F`
viết hoa) — SePay trích vào trường `code` = **mã đơn hàng**.

Body SePay gửi (rút gọn): `id, gateway, transactionDate, accountNumber, code, content, transferType,
transferAmount, referenceCode, ...`

**Xử lý phía server:**
1. **Idempotent** theo `id` (SePay txn id, lưu UNIQUE) — retry/replay chỉ xử lý 1 lần.
2. Chỉ nhận `transferType = "in"`; khớp đơn theo `code` = `orderCode`.
3. Nếu đơn đang `PENDING` và `transferAmount ≥ totalAmount` → set **`paymentStatus = PAID`**,
   **`status = PROCESSING`**, rồi **gửi email xác nhận** (email của đơn ONL bị hoãn lúc đặt, giờ mới gửi).
4. Ghi log giao dịch vào bảng `payment_transactions`.

**Phản hồi**: luôn `HTTP 200` + body **`{"success": true}`** (đúng chuẩn SePay). Chữ ký sai → `401` +
`{"success": false}`.

```bash
# Test cục bộ: cần secret + tự ký (vì fail-closed). Giả sử SEPAY_WEBHOOK_SECRET=test-secret
SECRET='test-secret'
BODY='{"id":92704,"gateway":"MBBank","transactionDate":"2024-07-02 11:08:33","accountNumber":"686804076868","code":"ODF97CD281BE0D","content":"ODF97CD281BE0D chuyen tien","transferType":"in","transferAmount":1200000,"referenceCode":"FT123"}'
SIG=$(printf '%s' "$BODY" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')
curl -X POST http://localhost:8080/api/webhooks/sepay \
  -H "Content-Type: application/json" -H "X-SePay-Signature: sha256=$SIG" -d "$BODY"
# → {"success":true}   (đơn ODF97CD281BE0D chuyển sang PAID + PROCESSING)
```

> Trên production, **SePay tự gọi kèm chữ ký** — bạn chỉ cần dán cùng một secret vào cấu hình webhook SePay
> và biến môi trường `SEPAY_WEBHOOK_SECRET`.
