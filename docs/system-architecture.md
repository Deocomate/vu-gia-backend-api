# 🏛 Gốm Sứ Vũ Gia — System Architecture & Technical Design

> **Tài liệu Kiến trúc hệ thống**: Chi tiết thiết kế luồng xử lý, luồng xác thực JWT/OAuth2, luồng đặt hàng & thanh toán, cơ chế lưu trữ đĩa local và mô hình dữ liệu.

---

## 1. Tổng quan Kiến trúc Hệ thống (System Architecture)

Hệ thống Backend Gốm Sứ Vũ Gia xây dựng trên nền **Spring Boot 3.5.10 (Java 21)** kết hợp với **MySQL 8.0** và **Flyway Database Migration**.

```mermaid
graph TD
    Client[Storefront / Admin Frontend] -->|HTTPS REST API| SecurityFilter[Spring Security Filter Chain]
    SecurityFilter -->|Validate JWT / OAuth2| ControllerLayer[REST Controllers]
    ControllerLayer -->|DTO| ServiceLayer[Business Service Layer]
    ServiceLayer -->|MapStruct| Mapper[DTO Mapper]
    ServiceLayer -->|JPA / Spec| RepoLayer[Spring Data Repositories]
    RepoLayer -->|SQL Query| MySQL[(MySQL 8 Database)]
    
    ServiceLayer -->|Local File IO| FileStorage[Local Filesystem Storage /data]
    ServiceLayer -->|OrderPlacedEvent| EventBus[Spring Event Publisher]
    EventBus -->|Async Event| EmailListener[OrderEmailListener]
    EmailListener -->|SMTP / Thymeleaf| MailServer[SMTP Email Server]
```

---

## 2. Luồng Bảo mật & Xác thực (Authentication & Authorization Flow)

Hệ thống hỗ trợ 2 hình thức xác thực chính: Đăng nhập truyền thống (Username/Email + Password) và Đăng nhập Google (OAuth2 ID Token).

```mermaid
sequenceDiagram
    autonumber
    actor User as Khách hàng / Admin
    participant Client as Frontend (Web/Mobile)
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthService / JwtService
    participant DB as MySQL DB

    alt Đăng nhập Username / Password
        User->>Client: Nhập Credentials
        Client->>AuthCtrl: POST /api/auth/login
        AuthCtrl->>AuthSvc: Validate Username/Password
        AuthSvc->>DB: Query User & Check BCrypt Hash
    else Đăng nhập Google OAuth2
        User->>Client: Login with Google
        Client->>AuthCtrl: POST /api/auth/google (idToken)
        AuthCtrl->>AuthSvc: Verify Google ID Token
    end

    AuthSvc->>DB: Sinh Refresh Token & lưu DB
    AuthSvc-->>Client: Trả về Access Token (JWT) + Refresh Token
    
    Note over Client, AuthCtrl: Các Request tiếp theo
    Client->>AuthCtrl: API Request (Header: Authorization: Bearer <JWT>)
    AuthCtrl->>AuthSvc: JwtAuthenticationFilter giải mã JWT & cấp SecurityContext
```

---

## 3. Luồng Xử lý Đặt hàng & Thanh toán (Order & Payment Processing)

Luồng đặt hàng được thiết kế đảm bảo **Idempotency** (chống đặt trùng đơn) và xử lý giao dịch nguyên tử (Atomic Coupon Redemption & Stock Check).

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách hàng
    participant API as OrderController / OrderService
    participant DB as MySQL DB
    participant Event as Spring Event Publisher
    participant Mail as OrderEmailListener

    Customer->>API: POST /api/orders (Payload + Coupon Code + Idempotency Key)
    API->>DB: Check Trùng Idempotency Key
    API->>DB: Kiểm tra tồn kho & Validate Coupon nguyên tử
    API->>DB: Tạo Order, OrderItems & Cập nhật lượt dùng Coupon
    API->>DB: Xóa các sản phẩm đã đặt khỏi Cart (nếu có)
    API->>DB: Lưu giao dịch PaymentTransaction (PENDING)
    
    API->>Event: Publish OrderPlacedEvent(orderId)
    API-->>Customer: Trả về OrderResponse (Kèm thông tin QR VietQR nếu thanh toán CK)
    
    async Email Notification
        Event->>Mail: OrderEmailListener nhận sự kiện bất đồng bộ (@Async)
        Mail->>Mail: Render HTML Email với Thymeleaf Template
        Mail->>Customer: Gửi email xác nhận đặt hàng thành công
    end
```

---

## 4. Cơ chế Tiếp nhận Webhook SePay (SePay Webhook Integration)

Khi khách hàng thanh toán qua chuyển khoản ngân hàng VietQR, hệ thống nhận webhook tự động từ SePay để cập nhật đơn hàng sang trạng thái đã thanh toán.

```mermaid
sequenceDiagram
    autonumber
    participant SePay as SePay Gateway
    participant WebhookCtrl as PaymentWebhookController
    participant Verifier as SepaySignatureVerifier
    participant OrderSvc as PaymentWebhookService
    participant DB as MySQL DB

    SePay->>WebhookCtrl: POST /api/webhooks/sepay (Body + Authorization Header)
    WebhookCtrl->>Verifier: Kiểm tra Chữ ký HMAC / Secret Config
    alt Chữ ký không hợp lệ
        Verifier-->>WebhookCtrl: Fail Verification
        WebhookCtrl-->>SePay: HTTP 401 / 400 (Unauthorized)
    else Chữ ký hợp lệ
        WebhookCtrl->>OrderSvc: Process Webhook Data (content, amount)
        OrderSvc->>DB: Tìm đơn hàng theo Mã Đơn (trích xuất từ nội dung chuyển khoản)
        OrderSvc->>DB: Kiểm tra số tiền & Cập nhật Order Status -> PAID / COMPLETED
        OrderSvc-->>SePay: HTTP 200 OK (Success Envelope)
    end
```

---

## 5. Kiến trúc Lưu trữ Ảnh Local Filesystem (Local File Storage)

Tất cả tệp tin tải lên (ảnh sản phẩm, banner, tin tức) được lưu trữ trực tiếp trên đĩa cứng local tại thư mục `data/` (`APP_STORAGE_ROOT`) và phục vụ công khai qua HTTP prefix `/files/**`.

```mermaid
graph LR
    Client[Client App] -->|Upload File POST /api/media/upload| Controller[MediaController]
    Controller -->|FileStorageService| Disk[(Local Disk ./data)]
    Disk -->|Read File| StaticResourceHandler[Spring ResourceHttpRequestHandler /files/**]
    StaticResourceHandler -->|Public Image Link| Client
```

- **Entity & DTO**: Cơ sở dữ liệu chỉ lưu đường dẫn tương đối (Relative Path, ví dụ `products/ceramic-vase.jpg`).
- **Annotation `@StorageUrl`**: Tự động biến đổi Relative Path thành Absolute URL đầy đủ khi trả về JSON client (ví dụ `http://localhost:8080/files/products/ceramic-vase.jpg`).
