# 🎨 Gốm Sứ Vũ Gia — API Design Guidelines & Standards

> **Tài liệu Quy chuẩn Thiết kế API**: Định nghĩa các nguyên tắc RESTful API, cấu trúc dữ liệu trao đổi, quy ước đặt tên endpoint, chuẩn mã lỗi và mô hình bảo mật áp dụng thống nhất cho toàn bộ hệ thống `vu-gia-backend-api`.

---

## 1. Nguyên tắc Thiết kế RESTful API

1. **Base Path & URL Hierarchy**:
   - Tất cả các endpoint đều nằm dưới đường dẫn gốc: `/api`
   - Sử dụng danh từ số nhiều cho tên tài nguyên (Resources): `/api/products`, `/api/orders`, `/api/news`.
   - URL sử dụng chữ thường, ngăn cách bằng dấu gạch ngang (kebab-case): `/api/product-categories`, `/api/shipping-methods`.

2. **Sử dụng HTTP Verbs đúng ngữ nghĩa**:
   - `GET`: Tra cứu, đọc dữ liệu (không làm thay đổi trạng thái hệ thống).
   - `POST`: Tạo mới tài nguyên hoặc thực hiện hành động nghiệp vụ (Ví dụ: `/api/auth/login`, `/api/orders`).
   - `PUT`: Cập nhật toàn bộ thông tin tài nguyên.
   - `PATCH`: Cập nhật một phần thông tin tài nguyên.
   - `DELETE`: Xóa hoặc vô hiệu hóa tài nguyên.

---

## 2. Quy chuẩn Định dạng Envelope Response

Tất cả các API response (kể cả thành công và thất bại) đều trả về định dạng bọc (Envelope) thống nhất:

### 2.1. Response Thành công (`ApiResponse<T>`)

```json
{
  "code": 1000,
  "message": "Success message",
  "data": {
    "id": 1,
    "name": "Bình hoa gốm vuốt tay"
  },
  "timestamp": "2026-07-25T10:00:00Z"
}
```

### 2.2. Response Phân trang (`ApiResponse<PageResponse<T>>`)

```json
{
  "code": 1000,
  "message": "Get products successfully",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 45,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2026-07-25T10:00:00Z"
}
```

### 2.3. Response Lỗi (`ApiResponse<Void>`)

```json
{
  "code": 4044,
  "message": "Product not found with id: 99",
  "data": null,
  "timestamp": "2026-07-25T10:00:00Z"
}
```

### 2.4. Response Lỗi Validation Payload (`code 4001`)

```json
{
  "code": 4001,
  "message": "Validation failed",
  "data": {
    "email": "Email format is invalid",
    "password": "Password must be at least 6 characters"
  },
  "timestamp": "2026-07-25T10:00:00Z"
}
```

---

## 3. Mã Lỗi & Phân dải Mã Lỗi (`ErrorCode`)

| Mã lỗi (`code`) | Thông điệp mặc định | HTTP Status | Ý nghĩa |
|---|---|---|---|
| `1000` | Success | 200 OK | Thực thi thành công |
| `4000` | Bad request | 400 Bad Request | Tham số không hợp lệ |
| `4001` | Validation failed | 400 Bad Request | Lỗi Bean Validation DTO |
| `4010` | Unauthorized | 401 Unauthorized | Thiếu hoặc JWT Token không hợp lệ |
| `4011` | Invalid credentials | 401 Unauthorized | Mật khẩu hoặc tài khoản sai |
| `4012` | Token expired | 401 Unauthorized | Access Token đã hết hạn |
| `4030` | Access denied | 403 Forbidden | Không đủ quyền truy cập (RBAC) |
| `4040` | Resource not found | 404 Not Found | Không tìm thấy tài nguyên |
| `4041` | User not found | 404 Not Found | Không tìm thấy người dùng |
| `4044` | Product not found | 404 Not Found | Không tìm thấy sản phẩm |
| `4090` | Resource already exists | 409 Conflict | Dữ liệu bị trùng lặp |
| `4100` | Coupon limit exceeded | 400 Bad Request | Mã giảm giá đã hết lượt sử dụng |
| `9000` | Internal server error | 500 Internal Server Error | Lỗi hệ thống chưa được phân loại |

---

## 4. Quy chuẩn Validation & Payload Constraints

- **Request DTOs**: Sử dụng Jakarta Bean Validation Annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`, `@Pattern`).
- **Controller Layer**: Bắt buộc gắn `@Valid @RequestBody` khi tiếp nhận JSON payload từ client.

---

## 5. Quy chuẩn Bảo mật & Header

- **Xác thực JWT**: Client gửi Token qua HTTP Header:
  ```http
  Authorization: Bearer <your_jwt_access_token>
  ```
- **Xác thực Webhook SePay**: Gửi Signature qua Authorization header hoặc secret query param để kiểm tra chữ ký HMAC.
