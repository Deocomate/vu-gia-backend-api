# 📏 Gốm Sứ Vũ Gia — Code Standards & Architectural Guidelines

> **Tài liệu quy chuẩn phát triển**: Định hướng kiến trúc phân tầng, nguyên tắc thiết kế mã nguồn, xử lý lỗi, phân trang an toàn và kiểm thử áp dụng cho dự án `vu-gia-backend-api`.

---

## 1. Kiến trúc phân tầng (Layered Architecture)

Mọi chức năng trong hệ thống phải tuân thủ nghiêm ngặt luồng dữ liệu 1 chiều:

```text
HTTP Request ──▶ Controller ──▶ Service (Interface) ──▶ ServiceImpl ──▶ Repository / Spec ──▶ Database
                     │                                     │
               ApiResponse<T>                        MapStruct Mapper
                     ▲                                     │
          GlobalExceptionHandler ◀── AppException      Entity ↔ DTO
```

### Quy tắc đặt tên & Vị trí tập tin (Ví dụ domain `Product`):

| Tầng | Package | Tên lớp | Quy ước |
|---|---|---|---|
| **Entity** | `vn.springboot.entity` | `ProductEntity` | Bắt buộc extends `BaseEntity`, hậu tố `Entity`. |
| **Repository** | `vn.springboot.repository` | `ProductRepository` | Extends `JpaRepository<ProductEntity, Long>`, `JpaSpecificationExecutor<ProductEntity>`. |
| **Specification** | `vn.springboot.repository.specification` | `ProductSpecification` | Chứa các hàm static trả về `Specification<ProductEntity>`. |
| **Request DTO** | `vn.springboot.dto.request.product` | `ProductSearchRequest`, `ProductCreateRequest` | Hậu tố `Request`, chứa Validation annotations. |
| **Response DTO** | `vn.springboot.dto.response.product` | `ProductResponse` | Hậu tố `Response`. |
| **Mapper** | `vn.springboot.mapper` | `ProductMapper` | Interface `@Mapper(componentModel = "spring")`. |
| **Service Interface** | `vn.springboot.service` | `ProductService` | Định nghĩa contract các phương thức nghiệp vụ. |
| **Service Implementation**| `vn.springboot.service.impl` | `ProductServiceImpl` | Gắn `@Service`, `@Transactional`. |
| **Controller** | `vn.springboot.controller` | `ProductController` | Gắn `@RestController`, `@RequestMapping("/api/products")`. |

---

## 2. Quy tắc thiết kế Mã nguồn (Coding Rules)

1. **Dependency Injection**:
   - **BẮT BUỘC** dùng `@RequiredArgsConstructor` + `private final` field cho mọi Dependency.
   - **KHÔNG** sử dụng `@Autowired` trên field.

2. **Cơ sở dữ liệu & Entity**:
   - Tất cả Entity **phải kế thừa** `BaseEntity` (đã có sẵn `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`).
   - Sử dụng `@Table(name = "name_in_snake_case")` và `@Column(name = "column_name")` tường minh.
   - Quan hệ FK dùng `@ManyToOne(fetch = FetchType.LAZY)`. Tránh dùng `FetchType.EAGER`.

3. **Quản lý Transaction (`@Transactional`)**:
   - Các phương thức đọc dữ liệu: Đánh dấu `@Transactional(readOnly = true)`.
   - Các phương thức ghi dữ liệu: Đánh dấu `@Transactional`.

4. **Biến đổi DTO ↔ Entity bằng MapStruct**:
   - Sử dụng MapStruct để chuyển đổi dữ liệu compile-time, không viết code thủ công set từng field.
   - Cấu hình `@Mapper(componentModel = "spring")`.

---

## 3. Quy chuẩn Xử lý Lỗi & Envelope Response

1. **Envelope Response thống nhất**:
   - Mọi Controller thành công trả về `ApiResponse<T>`:
     ```json
     {
       "code": 1000,
       "message": "Success message",
       "data": { ... },
       "timestamp": "2026-07-25T10:00:00Z"
     }
     ```
   - Trả trực tiếp `ApiResponse<T>`, **KHÔNG** bọc trong `ResponseEntity<ApiResponse<T>>` ở nhánh thành công.

2. **Ném Lỗi Ngoại lệ (Exceptions)**:
   - Khi gặp lỗi nghiệp vụ, ném `AppException(ErrorCode.XXX)` từ Service.
   - `GlobalExceptionHandler` (`@RestControllerAdvice`) sẽ tự động bắt ngoại lệ và trả về HTTP Status + Error JSON tương ứng.
   - **KHÔNG** dùng khối `try-catch` nuốt lỗi hoặc tự format lỗi tại Controller.

3. **Bảng phân dải mã lỗi (`ErrorCode`)**:
   - `1000`: Thành công.
   - `4000` - `4005`: Lỗi request / validation payload.
   - `4010` - `4019`: Lỗi xác thực JWT / Login.
   - `4030`: Lỗi không đủ quyền (Forbidden).
   - `4040` - `4049`: Không tìm thấy tài nguyên (Not Found).
   - `4090` - `4199`: Conflict / Dữ liệu đã tồn tại / Không đủ điều kiện.
   - `9000` - `9999`: Lỗi máy chủ nội bộ.

---

## 4. Quy chuẩn Phân trang & Tìm kiếm An toàn (Pagination & Filtering)

Mọi API danh sách phải hỗ trợ phân trang an toàn theo chuẩn:

1. **Search Request DTO**:
   - Chứa `page = 0`, `size = 10`, `sortBy = "id"`, `sortDirection = "ASC"`.

2. **Xử lý An toàn tại Service**:
   - Index trang: 0-based.
   - Clamp kích thước trang: `Math.clamp(size, 1, MAX_PAGE_SIZE /*=100*/)`.
   - **Sort Whitelist**: Bắt buộc kiểm tra danh sách các trường được phép sắp xếp (`SORTABLE_FIELDS`) để tránh lỗi `PropertyReferenceException` hoặc SQL Injection.

3. **Trả về `PageResponse<T>`**:
   - Bọc kết quả phân trang trong `PageResponse<T>` chứa `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, `last`.

---

## 5. Quy chuẩn Kiểm thử (Testing Standards)

1. **Service Unit Test (`XxxServiceImplTest`)**:
   - Sử dụng JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`).
   - Mock các Repository và Mapper.
   - Kiểm thử đầy đủ các nhánh thành công, nhánh ném `AppException` (Not Found, Existed) và nhánh phân trang an toàn.

2. **Controller Integration Test (`XxxControllerTest`)**:
   - Sử dụng `@WebMvcTest(XxxController.class)` + `@MockBean` cho Service.
   - Sử dụng `@WithMockUser` kiểm tra phân quyền HTTP Status (200, 401, 403, 4001 validation error).

3. **Chạy Test**:
   ```bash
   ./mvnw test
   ```
