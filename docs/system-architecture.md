# 🏛 Gốm Sứ Vũ Gia — System Architecture & Technical Design

> **Tài liệu Kiến trúc hệ thống**: Chi tiết thiết kế luồng xử lý, luồng xác thực JWT/OAuth2, luồng đặt hàng & thanh toán, cơ chế lưu trữ đĩa local và mô hình dữ liệu.

---

## 1. Tổng quan Kiến trúc Hệ thống (System Architecture)

Hệ thống Backend Gốm Sứ Vũ Gia xây dựng trên nền **Spring Boot 3.5.10 (Java 21)** kết hợp với **MySQL 8.0**, schema quản lý code-first qua Hibernate (`ddl-auto=update`) và seed dữ liệu qua framework `DomainSeeder` (Java, xem mục 6).

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

---

## 6. Quản lý Schema Code-First & Seed Dữ liệu (`vn.springboot.seed`)

Từ 2026-07, dự án chuyển từ quản lý schema qua migration SQL sang **code-first**: JPA Entity là nguồn chân lý duy nhất của schema, không còn file migration SQL nào.

```mermaid
graph TD
    Boot[App Startup] -->|ddl-auto=update| Hibernate[Hibernate tạo/cập nhật schema từ Entity]
    Hibernate --> SeedRunner[SeedRunner CommandLineRunner]
    SeedRunner -->|APP_ENV=development| ResetAll[Reset toàn bộ 12 domain + TransactionalDataCleaner]
    SeedRunner -->|other profile| SeedEmpty[Seed từng domain nếu rỗng]
    ResetAll --> SeedForward[Seed lại theo thứ tự FK-safe]
    SeedForward --> OrphanCheck[OrphanReferenceChecker]
    SeedEmpty --> OrphanCheck
    OrphanCheck -->|throw nếu có orphan| Boot
```

- **12 `DomainSeeder`** (interface `isEmpty()`/`reset()`/`seed()`), điều phối bởi `SeedRunner` theo thứ tự FK-safe cố định (không dựa vào thứ tự Spring tự inject `List<DomainSeeder>`).
- **`APP_ENV=development`**: mỗi lần khởi động xoá sạch (children-first) và seed lại toàn bộ 12 domain **và** 6 bảng giao dịch không seed (`orders`, `order_items`, `cart_items`, `payment_transactions`, `refresh_tokens`, `contact_requests`) qua `TransactionalDataCleaner` — DB coi là hoàn toàn disposable.
- **Profile khác**: mỗi domain chỉ seed nếu đang rỗng (idempotent), không bao giờ đụng vào dữ liệu đã có; 6 bảng giao dịch không bị `TransactionalDataCleaner` chạm tới.
- **`OrphanReferenceChecker`**: chạy sau mỗi lần seed, ở mọi profile — `V1` (cũ) không khai báo `FOREIGN KEY` thật nào ở tầng CSDL, nên một lỗi thứ tự seed sẽ tạo ra tham chiếu treo (dangling reference) âm thầm thay vì lỗi loud; checker kiểm tra 3 tham chiếu chéo thực tế (`product_images.product_id`, `products.product_category_id`, `news.news_category_id`) và `throw` nếu phát hiện orphan.

### Đánh đổi đã chấp nhận (Accepted Tradeoffs)

1. **`ddl-auto=update` áp dụng ở mọi môi trường, kể cả production** (không có override theo profile). Đây là một anti-pattern được biết đến của Hibernate: `update` không bao giờ tự xoá cột/bảng mồ côi khi một field bị xoá khỏi entity, và không còn file migration nào để review diff schema qua code review (công cụ migration trước đây có tính năng này). Chấp nhận có chủ đích vì dự án chưa có dữ liệu production thật ở thời điểm chuyển đổi (2026-07-27); không có kế hoạch bổ sung tooling diff schema ở quy mô hiện tại.
2. **`/actuator/health` có thể báo `UP` trước khi `SeedRunner` seed xong.** `SeedRunner` là một `CommandLineRunner`, chạy sau khi Tomcat đã khởi động xong — nghĩa là có một khoảng hở ngắn mà health check có thể trả về "khỏe mạnh" trong khi catalog vẫn đang rỗng (đặc biệt rõ ở lần boot đầu tiên trên DB trống). Chấp nhận không thêm `HealthIndicator` tùy chỉnh để đóng khoảng hở này; ghi nhận như một giới hạn đã biết.

---

## 7. Site Settings Singleton Config (Feature Flag: Cart-Mode Toggle)

Quản lý cấu hình toàn site (chẳng hạn như bật/tắt chế độ giỏ hàng) thông qua một entity singleton.

```mermaid
sequenceDiagram
    autonumber
    participant Client as Frontend
    participant GET as GET /api/site-settings
    participant PUT as PUT /api/site-settings
    participant Seeder as SiteSettingSeeder
    participant DB as MySQL DB

    Seeder->>DB: Boot: nếu count(site_setting)==0 → insert 1 row (cartEnabled=true)
    Note over Seeder: Ngăn chặn race condition: admin viết + seeder viết cùng lúc
    
    Client->>GET: Fetch cấu hình công khai
    GET->>DB: SELECT * FROM site_setting LIMIT 1
    GET-->>Client: {cartEnabled: boolean} (Công khai, không cần token)
    
    Note over Client, PUT: Admin mở trang settings
    Client->>PUT: PUT /api/site-settings {cartEnabled: boolean}
    PUT->>PUT: Kiểm tra hasRole('SUPERADMIN')
    PUT->>DB: UPDATE site_setting SET cartEnabled = ?
    PUT-->>Client: {cartEnabled: boolean} (trả lại giá trị sau khi lưu)
```

**Lưu ý về thực thi (Enforcement Boundary):** `cartEnabled=false` là một **UI-only gate** trên client (header, giỏ hàng, checkout bị ẩn; Product pages/Customizer mở contact form modal). Backend **không** kiểm tra flag này trên các endpoint cart/coupon — `/api/cart`, `/api/coupons/validate`, `POST /api/orders` đều vẫn chấp nhận request. Điều này là đã chấp nhận: frontend hoàn toàn kiểm soát UX hiển thị, backend tin tưởng vào quyết định frontend.

---

## 8. Contact Request Notification Email Flow

Mỗi khi khách hàng gửi form liên hệ (thông qua `/lien-he` page hoặc modal từ product page), hệ thống gửi email thông báo tới admin.

```mermaid
sequenceDiagram
    autonumber
    participant Client as Frontend
    participant POST as POST /api/contact-requests
    participant EventBus as Spring Event Publisher
    participant Listener as ContactRequestEmailListener
    participant Mail as SMTP Email Server
    participant Admin as Admin (CONTACT_NOTIFY_EMAIL)

    Client->>POST: Gửi {name, email, phone, content}
    POST->>POST: Validate + Save ContactRequestEntity
    POST->>EventBus: Publish ContactRequestSubmittedEvent(contactRequestId)
    POST-->>Client: HTTP 200 OK (đơn xin được lưu, UX phản hồi thành công ngay)
    
    async Email (Background)
        EventBus->>Listener: @Async @TransactionalEventListener(AFTER_COMMIT)
        Listener->>Listener: Render HTML via Thymeleaf (contact-notification.html)
        Listener->>Mail: Gửi email tới CONTACT_NOTIFY_EMAIL
        Mail->>Admin: Nhận email thông báo (nếu CONTACT_NOTIFY_EMAIL != empty)
    end
    
    Note over POST, Admin: Nếu CONTACT_NOTIFY_EMAIL trống → không gửi email (no-op, không lỗi)
```

**Cấu hình:**
- **`app.mail.contact-notify-to`** / **`CONTACT_NOTIFY_EMAIL`**: Địa chỉ email nhận thông báo liên hệ. Mặc định trống (rỗng string, không phải null) → email không gửi.
- **`contact-notification.html`**: Thymeleaf template hiển thị thông tin contact (name, email, phone, content) với `th:text` (không `th:utext`) để tránh HTML injection từ trường `content` của khách.
- **Async & No-Block:** Email được gửi bất đồng bộ trong background (`@Async`), không chặn response HTTP. Nếu gửi email thất bại, request vẫn trả về 200 OK (failure-safe).
