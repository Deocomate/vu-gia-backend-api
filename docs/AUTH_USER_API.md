# AUTH & USER API

Tài liệu bàn giao FE cho nhóm chức năng **Xác thực (Auth)** và **Người dùng (User)** sau khi
chuyển sang mô hình phân quyền **1 role trên mỗi user** (cột `users.role`), bỏ hoàn toàn bảng
`roles`/`permissions`.

## 1. Tổng quan

- Xác thực bằng **JWT access token** (HS512, mặc định sống 1 giờ) + **refresh token** lưu DB
  (opaque UUID, mặc định sống 7 ngày, hỗ trợ thu hồi & xoay vòng).
- Mỗi user có đúng **một** `role` ∈ `SUPERADMIN | ADMIN | CUSTOMER`. Đăng ký mới luôn là `CUSTOMER`.
- Authority trong Spring Security là `ROLE_<role>` (vd `ROLE_ADMIN`) → endpoint quản trị dùng
  `@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")`.
- Đăng nhập nhận diện bằng **username hoặc email**.

| Nhóm | Quyền yêu cầu |
|---|---|
| `POST /api/auth/register`, `/login`, `/refresh`, `/google` | Công khai |
| `POST /api/auth/logout`, `/api/auth/change-password`, `GET /api/auth/me` | Đã đăng nhập (bất kỳ role) |
| `GET /api/users`, `GET /api/users/{id}`, `PATCH /api/users/{id}/password` | `ADMIN` hoặc `SUPERADMIN` |
| `POST /api/users`, `PATCH /api/users/{id}/role` | **Chỉ `SUPERADMIN`** |

## 2. Quy ước envelope & mã lỗi

Mọi response (kể cả lỗi) bọc trong envelope:

```json
{ "code": 1000, "message": "OK", "data": { }, "timestamp": "2026-07-10T10:00:00Z" }
```

- `code = 1000` là thành công. `data` chứa payload.
- Lỗi giữ nguyên envelope, `code` là mã nghiệp vụ dưới đây, kèm HTTP status tương ứng.

| code | HTTP | Ý nghĩa |
|---|---|---|
| 1000 | 200 | Thành công |
| 4001 | 400 | Lỗi validation (kèm `data` = map `field → message`) |
| 4004 | 400 | Mật khẩu cũ không đúng (đổi mật khẩu) |
| 4010 | 401 | Chưa xác thực / thiếu token |
| 4011 | 401 | Sai username hoặc mật khẩu |
| 4015 | 401 | Google ID token không hợp lệ (sai chữ ký/hết hạn/sai `aud`) |
| 4016 | 401 | Email Google chưa được xác minh |
| 4030 | 403 | Không đủ quyền |
| 4040 | 404 | Không tìm thấy user |
| 4043 | 404 | Không tìm thấy refresh token |
| 4090 | 409 | Username đã tồn tại |
| 4091 | 409 | Email đã tồn tại |
| 4092 | 401 | Refresh token đã bị thu hồi |
| 4093 | 401 | Refresh token đã hết hạn |

---

## 3. Chi tiết endpoint

### 3.1 Đăng ký — `POST /api/auth/register` (công khai)

Request:

```json
{
  "username": "john",          // bắt buộc, 3–50 ký tự, duy nhất
  "email": "john@example.com", // bắt buộc, email hợp lệ, ≤ 50 ký tự, duy nhất
  "password": "secret123",     // bắt buộc, 6–100 ký tự
  "name": "John Doe",          // tuỳ chọn, ≤ 50 ký tự
  "phone": "0900000000"        // tuỳ chọn, đúng 10 chữ số nếu có
}
```

Response `200` — `data` là `UserResponse` (role luôn `CUSTOMER`):

```json
{
  "code": 1000,
  "message": "Registered successfully",
  "data": {
    "id": 12, "username": "john", "email": "john@example.com",
    "name": "John Doe", "phone": "0900000000", "gender": null, "dob": null,
    "avatar": null, "provider": null, "role": "CUSTOMER",
    "createdAt": "2026-07-10T10:00:00Z"
  }
}
```

Lỗi: `4001` (validation), `4090` (username tồn tại), `4091` (email tồn tại).

### 3.2 Đăng nhập — `POST /api/auth/login` (công khai)

Request:

```json
{ "username": "john", "password": "secret123" }  // username = username hoặc email
```

Response `200` — `data` là `AuthResponse`:

```json
{
  "code": 1000,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "9f1c8e2a-...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": { "id": 12, "username": "john", "role": "CUSTOMER", "...": "..." }
  }
}
```

Gửi access token ở các request sau: header `Authorization: Bearer <accessToken>`.

Access token chứa các claim: `sub` (username), `uid`, `email`, `name`, `role`, `authorities`.

Lỗi: `4001` (validation), `4011` (sai thông tin đăng nhập).

### 3.3 Đăng nhập Google — `POST /api/auth/google` (công khai)

Luồng: FE dùng Google Sign-In lấy **ID token** (JWT), gửi lên đây. Backend verify token với Google
(chữ ký + hết hạn + `aud` = client-id cấu hình + `email_verified`), rồi **cấp chính access/refresh
token của hệ thống** — trả cùng cấu trúc `AuthResponse` như login thường.

Tài khoản được xử lý theo thứ tự: (1) khớp `(provider=GOOGLE, providerId=sub)` → đăng nhập; (2) chưa
có nhưng trùng email đã xác minh → **liên kết** Google vào tài khoản đó; (3) chưa có → **tạo mới** tài
khoản `CUSTOMER` (password null, username sinh tự động từ email, avatar = ảnh Google).

Request:

```json
{ "idToken": "eyJhbGci..." }   // bắt buộc — ID token từ Google Sign-In
```

Response `200`: giống `AuthResponse` ở mục 3.2 (trong `user.provider = "GOOGLE"`).

Lỗi: `4001` (thiếu idToken), `4015` (token không hợp lệ), `4016` (email chưa xác minh).

> **Cấu hình backend**: đặt biến môi trường `GOOGLE_CLIENT_ID` = Google OAuth **Web client ID**
> (khớp `app.oauth2.google.client-id`). Để trống sẽ bỏ qua kiểm tra `aud` (chỉ dùng khi dev).

### 3.4 Làm mới token — `POST /api/auth/refresh` (công khai)

Request: `{ "refreshToken": "9f1c8e2a-..." }`

Response `200`: giống `AuthResponse` ở login. **Xoay vòng**: refresh token cũ bị thu hồi, trả về
cặp access + refresh token mới.

Lỗi: `4001`, `4043` (không tồn tại), `4092` (đã thu hồi), `4093` (đã hết hạn).

### 3.5 Đăng xuất — `POST /api/auth/logout` (đã đăng nhập)

Request: `{ "refreshToken": "9f1c8e2a-..." }` → thu hồi token. Response `200`, `data = null`.
Idempotent (token không tồn tại vẫn trả thành công).

### 3.6 Thông tin tài khoản hiện tại — `GET /api/auth/me` (đã đăng nhập)

Response `200` — `data` là `UserResponse` của người đang đăng nhập. Lỗi: `4010` nếu chưa xác thực.

### 3.7 Danh sách user — `GET /api/users` (ADMIN/SUPERADMIN)

Query params:

| Param | Kiểu | Mặc định | Ghi chú |
|---|---|---|---|
| `username` | string | | Lọc chứa, không phân biệt hoa thường |
| `email` | string | | Lọc chứa |
| `name` | string | | Lọc chứa |
| `phone` | string | | Lọc chứa |
| `role` | enum | | `SUPERADMIN\|ADMIN\|CUSTOMER`, khớp chính xác |
| `page` | int | `1` | Trang (bắt đầu từ 1) |
| `size` | int | `10` | Số phần tử/trang, tự kẹp trong `[1,100]` |
| `sortBy` | string | `id` | Whitelist: `id, username, email, name, role, createdAt` (giá trị lạ → `id`) |
| `sortDirection` | string | `ASC` | `ASC` hoặc `DESC` |

Ví dụ: `GET /api/users?role=CUSTOMER&name=john&page=1&size=20&sortBy=createdAt&sortDirection=DESC`

Response `200` — `data` là `PageResponse<UserResponse>`:

```json
{
  "code": 1000,
  "message": "OK",
  "data": {
    "content": [ { "id": 12, "username": "john", "role": "CUSTOMER", "...": "..." } ],
    "pageNumber": 1, "pageSize": 20, "totalElements": 1, "totalPages": 1,
    "first": true, "last": true
  }
}
```

Lỗi: `4010` (chưa đăng nhập), `4030` (không đủ quyền).

### 3.8 Chi tiết user — `GET /api/users/{id}` (ADMIN/SUPERADMIN)

Response `200` — `data` là `UserResponse`. Lỗi: `4010`, `4030`, `4040` (không tìm thấy).

---

## 4. DTO cho FE (TypeScript)

```ts
export type Role = 'SUPERADMIN' | 'ADMIN' | 'CUSTOMER';

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  name: string | null;
  phone: string | null;
  gender: string | null;
  dob: string | null;      // ISO date (yyyy-MM-dd)
  avatar: string | null;
  provider: string | null; // null = tài khoản local
  role: Role;
  createdAt: string;       // ISO instant
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;       // giây
  user: UserResponse;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  name?: string;
  phone?: string;
}

export interface LoginRequest { username: string; password: string; }
export interface GoogleLoginRequest { idToken: string; }
export interface RefreshTokenRequest { refreshToken: string; }

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
```

## 5. Bảng tổng hợp endpoint

| Method | Path | Auth | Quyền | Mô tả |
|---|---|---|---|---|
| POST | `/api/auth/register` | Công khai | — | Đăng ký tài khoản (role `CUSTOMER`) |
| POST | `/api/auth/login` | Công khai | — | Đăng nhập, trả access + refresh token |
| POST | `/api/auth/google` | Công khai | — | Đăng nhập bằng Google ID token (find-or-create) |
| POST | `/api/auth/refresh` | Công khai | — | Xoay vòng token |
| POST | `/api/auth/logout` | Có token | mọi role | Thu hồi refresh token |
| POST | `/api/auth/change-password` | Có token | mọi role | Tự đổi mật khẩu (cần mật khẩu cũ) |
| GET | `/api/auth/me` | Có token | mọi role | Thông tin tài khoản hiện tại |
| GET | `/api/users` | Có token | ADMIN/SUPERADMIN | Danh sách user (phân trang, lọc, sort) |
| GET | `/api/users/{id}` | Có token | ADMIN/SUPERADMIN | Chi tiết một user |
| POST | `/api/users` | Có token | **SUPERADMIN** | Tạo user + set role (vd ADMIN) |
| PATCH | `/api/users/{id}/role` | Có token | **SUPERADMIN** | Đổi role (vd CUSTOMER→ADMIN) |
| PATCH | `/api/users/{id}/password` | Có token | ADMIN/SUPERADMIN | Reset mật khẩu user (ADMIN chỉ reset được CUSTOMER) |

---

## 6. Endpoint test nhanh (curl → dán vào Postman)

> Base URL mặc định `http://localhost:8080`. Postman: **Import → Raw text** rồi dán lệnh `curl`.
> Sau khi `login`, lấy `data.accessToken` thay vào `<ACCESS_TOKEN>`, `data.refreshToken` thay vào `<REFRESH_TOKEN>`.

```bash
# 1) Đăng ký
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"secret123","name":"John Doe","phone":"0900000000"}'

# 2) Đăng nhập (username HOẶC email vào trường "username")
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret123"}'

# 3) Đăng nhập Google (idToken lấy từ Google Sign-In ở FE)
curl -X POST http://localhost:8080/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken":"<GOOGLE_ID_TOKEN>"}'

# 4) Refresh (xoay vòng token)
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<REFRESH_TOKEN>"}'

# 5) Đăng xuất (thu hồi refresh token)
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<REFRESH_TOKEN>"}'

# 6) Tài khoản hiện tại
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# 7) Danh sách user (ADMIN/SUPERADMIN) — lọc + phân trang + sort
curl "http://localhost:8080/api/users?username=&email=&name=&phone=&role=CUSTOMER&page=1&size=20&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# 8) Chi tiết user (ADMIN/SUPERADMIN)
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# 9) Tự đổi mật khẩu (đã đăng nhập) — cần mật khẩu cũ
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"oldPassword":"secret123","newPassword":"newsecret456"}'

# 10) Tạo user + set role (SUPERADMIN) — vd tạo 1 ADMIN
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <SUPERADMIN_TOKEN>" -H "Content-Type: application/json" \
  -d '{"username":"admin2","email":"admin2@example.com","password":"secret123","name":"Admin 2","phone":"0900000001","role":"ADMIN"}'

# 11) Đổi role user (SUPERADMIN) — vd nâng CUSTOMER lên ADMIN
curl -X PATCH http://localhost:8080/api/users/5/role \
  -H "Authorization: Bearer <SUPERADMIN_TOKEN>" -H "Content-Type: application/json" \
  -d '{"role":"ADMIN"}'

# 12) Reset mật khẩu cho user (ADMIN/SUPERADMIN; ADMIN chỉ reset được CUSTOMER)
curl -X PATCH http://localhost:8080/api/users/5/password \
  -H "Authorization: Bearer <ACCESS_TOKEN>" -H "Content-Type: application/json" \
  -d '{"newPassword":"resetpass789"}'
```
