# AUTH & USER API

Tài liệu bàn giao FE cho nhóm chức năng **Xác thực (Auth)** và **Người dùng (User)** sau khi
chuyển sang mô hình phân quyền **1 role trên mỗi user** (cột `users.role`), bỏ hoàn toàn bảng
`roles`/`permissions`.

> **Cập nhật quan trọng (refresh-token cookie):** kể từ bản này, refresh token **không còn**
> nằm trong JSON response — nó được cấp dưới dạng **httpOnly cookie**. `access token` vẫn nằm
> trong body như cũ. Xem chi tiết ở mục 1.1 và 3.4/3.5.

## 1. Tổng quan

- Xác thực bằng **JWT access token** (HS512, mặc định sống 1 giờ, trả trong JSON body) +
  **refresh token** lưu DB (opaque UUID, mặc định sống 7 ngày, hỗ trợ thu hồi & xoay vòng, cấp
  qua **httpOnly cookie**, không bao giờ xuất hiện trong JSON).
- Mỗi user có đúng **một** `role` ∈ `SUPERADMIN | ADMIN | CUSTOMER`. Đăng ký mới luôn là `CUSTOMER`.
- Authority trong Spring Security là `ROLE_<role>` (vd `ROLE_ADMIN`) → endpoint quản trị dùng
  `@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")`.
- Đăng nhập nhận diện bằng **username hoặc email**.
- Dùng chung cho **cả admin CMS lẫn customer storefront** — cơ chế cookie không phân biệt loại
  session; mỗi FE tự quản lý access token phía client (memory/state, KHÔNG localStorage).

### 1.1 Cookie & CSRF

| Cookie | Thuộc tính | Ghi chú |
|---|---|---|
| `refresh_token` | `HttpOnly`, `Secure`*, `SameSite=Lax`*, `Path=/api/auth`, `Max-Age`=7 ngày | JS **không đọc được**. Server tự đọc ở `/refresh`, `/logout`. |
| `XSRF-TOKEN` | **không** `HttpOnly`, `Secure`*, `SameSite=Lax`*, `Path=/api/auth`, `Max-Age`=7 ngày | JS đọc được — FE phải echo giá trị này vào header `X-XSRF-TOKEN` khi gọi `/refresh`. |

\* `Secure`/`SameSite` cấu hình qua env `APP_COOKIE_SECURE` (mặc định `true`) và
`APP_COOKIE_SAME_SITE` (mặc định `Lax`); domain qua `APP_COOKIE_DOMAIN` (mặc định host-only).

**CSRF (double-submit cookie)**: `POST /api/auth/refresh` bắt buộc header `X-XSRF-TOKEN` khớp
giá trị cookie `XSRF-TOKEN` hiện có, nếu không → `403` (`code 4031`). Cookie `XSRF-TOKEN` không
`HttpOnly` nên FE đọc bằng `document.cookie` để lấy giá trị (một site khác không đọc được cookie
này do same-origin policy, nên không thể tự tạo header khớp — đây là điểm mấu chốt của double-submit).
`POST /api/auth/logout` không yêu cầu CSRF header (chỉ cần access token hợp lệ — hậu quả tệ nhất
của CSRF ở đây là bị đăng xuất, không rò rỉ token).

**Ràng buộc cross-origin (QUAN TRỌNG cho FE chạy khác origin lúc dev, vd `localhost:3000` →
`localhost:8080`):** `SameSite=Lax` (mặc định) **không** gửi cookie trên các request
`fetch`/`XHR` cross-site (chỉ gửi khi *top-level navigation*, vd gõ URL/redirect) — nghĩa là nếu
FE dev server và BE khác origin, cookie `refresh_token`/`XSRF-TOKEN` sẽ **không** được đính kèm
khi FE gọi `fetch('http://localhost:8080/api/auth/refresh', {credentials:'include'})`, và luồng
refresh sẽ luôn thất bại dù cookie đã được set đúng lúc login. Hai hướng giải quyết chuẩn:

1. **Khuyến nghị cho dev**: proxy các request `/api/*` của FE dev server sang BE (vd Next.js
   `rewrites()` trong `next.config.js`, hoặc Vite `server.proxy`) để trình duyệt thấy FE+BE **cùng
   origin** — khi đó `SameSite=Lax` hoạt động bình thường, không cần đổi gì ở BE.
2. **Nếu bắt buộc cross-origin thật** (khác domain ở prod, hoặc không proxy được lúc dev): đặt
   `APP_COOKIE_SAME_SITE=None` và `APP_COOKIE_SECURE=true` — nhưng `SameSite=None` **bắt buộc**
   `Secure` (chỉ gửi qua HTTPS), nên BE (và cả FE nếu cùng cấp HTTPS) phải chạy HTTPS thật ngay cả
   ở dev (vd `mkcert` cấp cert local) — nằm ngoài phạm vi thay đổi BE này.

BE **không tự** chọn phương án 2 cho dev vì không có hạ tầng HTTPS local sẵn trong repo này —
mặc định giữ `Lax` (an toàn nhất, đúng cho prod same-site/subdomain-qua-proxy) và tài liệu hoá rõ
ràng ràng buộc trên để FE quyết định hướng triển khai dev phù hợp.

| Nhóm | Quyền yêu cầu |
|---|---|
| `POST /api/auth/register`, `/login`, `/google` | Công khai |
| `POST /api/auth/refresh` | Công khai + header CSRF `X-XSRF-TOKEN` bắt buộc (mục 1.1) |
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
| 4031 | 403 | Thiếu/sai CSRF token (header `X-XSRF-TOKEN` không khớp cookie `XSRF-TOKEN`) |
| 4040 | 404 | Không tìm thấy user |
| 4043 | 404 | Không tìm thấy refresh token (kể cả khi cookie `refresh_token` không có/hết hạn cookie) |
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

Response `200` — `data` là `AuthResponse` (**không còn `refreshToken` trong body** — nó được set
là cookie `refresh_token` httpOnly + cookie `XSRF-TOKEN`, xem mục 1.1):

```json
{
  "code": 1000,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": { "id": 12, "username": "john", "role": "CUSTOMER", "...": "..." }
  }
}
```

Response header kèm theo (rút gọn):

```
Set-Cookie: refresh_token=9f1c8e2a-...; Path=/api/auth; Max-Age=604800; HttpOnly; SameSite=Lax
Set-Cookie: XSRF-TOKEN=3b7a1c9e-...; Path=/api/auth; Max-Age=604800; SameSite=Lax
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

Response `200`: giống `AuthResponse` ở mục 3.2 (trong `user.provider = "GOOGLE"`), cùng 2 cookie
`refresh_token`/`XSRF-TOKEN` như login thường.

Lỗi: `4001` (thiếu idToken), `4015` (token không hợp lệ), `4016` (email chưa xác minh).

> **Cấu hình backend**: đặt biến môi trường `GOOGLE_CLIENT_ID` = Google OAuth **Web client ID**
> (khớp `app.oauth2.google.client-id`). Để trống sẽ bỏ qua kiểm tra `aud` (chỉ dùng khi dev).

### 3.4 Làm mới token — `POST /api/auth/refresh` (công khai, CSRF-protected)

**Không còn body.** Server tự đọc refresh token từ cookie `refresh_token`. Bắt buộc header:

```
X-XSRF-TOKEN: <giá trị đọc từ cookie XSRF-TOKEN qua document.cookie>
```

Response `200`: giống `AuthResponse` ở login. **Xoay vòng**: refresh token cũ bị thu hồi, cookie
`refresh_token`/`XSRF-TOKEN` được set lại với giá trị mới (access token mới nằm trong body).

Lỗi: `4031` (thiếu/sai CSRF header), `4043` (không có cookie hoặc token không tồn tại), `4092`
(đã thu hồi), `4093` (đã hết hạn).

### 3.5 Đăng xuất — `POST /api/auth/logout` (đã đăng nhập, cần `Authorization: Bearer`)

**Không còn body.** Server đọc refresh token từ cookie `refresh_token`, thu hồi trong DB (nếu có),
rồi **luôn** clear cả 2 cookie (`refresh_token`, `XSRF-TOKEN`, `Max-Age=0`). Response `200`,
`data = null`. Idempotent (token không tồn tại/không có cookie vẫn trả thành công) — không yêu cầu
CSRF header, chỉ cần access token hợp lệ.

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
  // refreshToken KHÔNG còn trong body — cấp qua cookie httpOnly `refresh_token` (xem mục 1.1).
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
// /api/auth/refresh và /api/auth/logout không nhận body nữa (đọc cookie refresh_token phía
// server); FE chỉ cần gọi fetch(..., { credentials: 'include' }) kèm header X-XSRF-TOKEN cho
// /refresh (đọc từ document.cookie, xem mục 1.1).

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
| POST | `/api/auth/login` | Công khai | — | Đăng nhập; access token trong body, refresh token qua cookie httpOnly |
| POST | `/api/auth/google` | Công khai | — | Đăng nhập bằng Google ID token (find-or-create); cookie như login |
| POST | `/api/auth/refresh` | Công khai + CSRF header | — | Xoay vòng token (đọc cookie, không nhận body) |
| POST | `/api/auth/logout` | Có token | mọi role | Thu hồi refresh token từ cookie + clear cookie |
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
> Sau khi `login`, lấy `data.accessToken` thay vào `<ACCESS_TOKEN>`.
> Dùng `-c cookies.txt -b cookies.txt` để curl tự lưu/gửi lại cookie `refresh_token`/`XSRF-TOKEN`
> (thay cho `<REFRESH_TOKEN>` cũ đã bị bỏ khỏi mọi request body).

```bash
# 1) Đăng ký
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"secret123","name":"John Doe","phone":"0900000000"}'

# 2) Đăng nhập (username HOẶC email vào trường "username") — lưu cookie vào cookies.txt
curl -X POST http://localhost:8080/api/auth/login -c cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret123"}'

# 3) Đăng nhập Google (idToken lấy từ Google Sign-In ở FE)
curl -X POST http://localhost:8080/api/auth/google -c cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"idToken":"<GOOGLE_ID_TOKEN>"}'

# 4) Refresh (xoay vòng token) — gửi lại cookie đã lưu + header CSRF khớp giá trị cookie XSRF-TOKEN
#    (đọc giá trị hiện tại của XSRF-TOKEN trong cookies.txt rồi dán vào <CSRF_TOKEN>)
curl -X POST http://localhost:8080/api/auth/refresh -b cookies.txt -c cookies.txt \
  -H "X-XSRF-TOKEN: <CSRF_TOKEN>"

# 5) Đăng xuất (thu hồi refresh token từ cookie, clear cookie)
curl -X POST http://localhost:8080/api/auth/logout -b cookies.txt -c cookies.txt \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

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
