# CLAUDE.md — Hướng dẫn phát triển dự án `vu-gia`

> Tài liệu quy trình cho Claude (và team) khi thêm/chỉnh chức năng. **Bám sát các quy ước
> dưới đây và tái sử dụng hạ tầng sẵn có — không tự chế lại.** Chức năng mẫu hoàn chỉnh để
> tham chiếu là **User** (`controller/UserController.java` → `service/impl/UserServiceImpl.java`
> → `repository/UserRepository.java` + `repository/specification/UserSpecification.java`).

## 1. Tổng quan dự án

- Spring Boot **3.5.10**, Java **21**, Maven (dùng wrapper `./mvnw`). Base package `vn.springboot`.
- DB **MySQL** (`jdbc:mysql://localhost:3306/dev_db`), Hibernate `ddl-auto: update`, `open-in-view: false`.
- Bảo mật **JWT + RBAC**: `User —(M:N)→ Role —(M:N)→ Permission`; kiểm quyền ở endpoint bằng
  `@PreAuthorize("hasAuthority('PERMISSION_CODE')")`.
- Thư viện chính: Spring Data JPA, **MapStruct 1.6.3**, Lombok, Bean Validation, springdoc/Swagger,
  jjwt (JWT). File upload lưu **local filesystem** dưới `data/` (`app.storage.*`), phục vụ qua
  `/files/**`; DB lưu **relative path**, field JSON annotate `@StorageUrl` tự resolve absolute
  URL lúc đọc/ghi — xem `docs/FILE_STORAGE_API.md`.

## 2. Kiến trúc phân tầng & quy ước (BẮT BUỘC)

Luồng 1 request:
`Controller → Service (interface) → ServiceImpl → Repository (+ Specification) → Entity`,
rồi map `Entity → Response DTO` bằng **MapStruct**, cuối cùng bọc trong `ApiResponse<T>`.

Vị trí & đặt tên (ví dụ domain `product`):

| Tầng | Package | Tên file |
|---|---|---|
| Entity | `entity.product` | `ProductEntity` (extends `BaseEntity`) |
| Repository | `repository` | `ProductRepository extends JpaRepository<…, Long>, JpaSpecificationExecutor<…>` |
| Specification | `repository.specification` | `ProductSpecification` |
| Request DTO | `dto.request.product` | `ProductSearchRequest`, `ProductCreateRequest`, `ProductUpdateRequest` |
| Response DTO | `dto.response.product` | `ProductResponse` |
| Mapper | `mapper` | `ProductMapper` (`@Mapper(componentModel = "spring")`) |
| Service | `service` + `service.impl` | `ProductService` + `ProductServiceImpl` |
| Controller | `controller` | `ProductController` (`/api/products`) |

Quy ước cứng:

- Inject bằng `@RequiredArgsConstructor` + `private final` (KHÔNG dùng `@Autowired`).
- Entity hậu tố `Entity`; DTO hậu tố `Request`/`Response`; **mọi entity extends `BaseEntity`**.
- `@Table(name=…)` + `@Column(name=…)` **snake_case tường minh** cho mọi field.
- Service: `@Service`; đọc dùng `@Transactional(readOnly = true)`, ghi dùng `@Transactional`.
- Controller **trả thẳng `ApiResponse<T>`** (không dùng `ResponseEntity` cho nhánh success).

## 3. Hạ tầng dùng chung — TÁI SỬ DỤNG, không tự chế lại

- **Envelope**: `common/response/ApiResponse.java` — `{code, message, data, timestamp}`, success
  `code=1000`. Dùng `ApiResponse.success(data)` hoặc `ApiResponse.success("message", data)`.
- **Phân trang**: `dto/response/PageResponse.java` — `{content, pageNumber, pageSize,
  totalElements, totalPages, first, last}`. List endpoint trả `ApiResponse<PageResponse<XxxResponse>>`.
- **Lỗi**: ném `AppException(ErrorCode.XXX)` từ service; `GlobalExceptionHandler`
  (`@RestControllerAdvice`) tự map ra envelope + đúng HTTP status. **Không** tự bắt/format lỗi ở controller.
- **Error code**: thêm vào enum `common/exception/ErrorCode.java` theo dải sẵn có — 9xxx server,
  4000/4001 bad-request/validation, 401x auth, 403x authz, **404x not-found**, **409x conflict**.
  Ví dụ: `PRODUCT_NOT_FOUND(4044, "Product not found", NOT_FOUND)`,
  `PRODUCT_CODE_EXISTED(4094, "Product code already exists", CONFLICT)`.
- **Auditing**: `common/entity/BaseEntity.java` đã có sẵn `id` (`GenerationType.IDENTITY`) +
  `created_at/updated_at/created_by/updated_by` (bật qua `config/JpaAuditingConfig.java`). Entity mới
  **KHÔNG khai lại** các field này; và **script migrate của bảng phải có** các cột
  `id, created_at, updated_at, created_by, updated_by`.
- **Bảo mật**: gắn `@PreAuthorize("hasAuthority('<PERMISSION>')")` trên endpoint. Cần permission
  mới thì seed trong `config/DataInitializer.java`.

## 4. Quy trình chuẩn phát triển 1 chức năng (7 bước)

### Bước 1 — Đọc schema từ script migrate
Nguồn chân lý của schema là **file migrate `.sql`** (bạn cung cấp/đặt trong repo). Đọc file migrate
của bảng liên quan để lấy: tên bảng, cột, kiểu dữ liệu, PK, FK, UNIQUE, INDEX, NOT NULL, default.
*(Hiện repo chưa có thư mục migration; khi có, quy ước đặt tại `src/main/resources/db/migration/`,
nếu dùng Flyway đặt tên `V{n}__{mo_ta}.sql`.)* Nếu chưa có migrate cho bảng đó → hỏi/yêu cầu file
DDL trước khi code, đừng đoán schema.

### Bước 2 — Đối chiếu / tạo entity
Tìm entity ứng với bảng. Đã có → so cột↔field, bổ sung cho khớp. Chưa có → tạo
`XxxEntity extends BaseEntity`, map từng cột bằng `@Column(name=…)` đúng tên/độ dài/nullable, dựng
quan hệ (`@ManyToOne(fetch = LAZY)` cho FK; `@ManyToMany` + `@JoinTable` cho bảng nối) đúng như
migrate. Nhớ `id` + 4 cột audit đã nằm ở `BaseEntity`.

### Bước 3 — Viết code theo tầng
Theo đúng thứ tự & mẫu ở Mục 2/3: Repository (+ Specification cho filter động) → DTO request/response
→ Mapper MapStruct → Service interface + Impl → Controller. Mỗi lớp bám sát bản mẫu `User*`.

### Bước 4 — Bắt lỗi, check null, validation (đầy đủ)
- DTO body (`create/update`): annotate `@NotNull/@NotBlank/@Size/@Email/@Positive…`; controller nhận
  `@Valid @RequestBody`. Lỗi validation tự thành `code 4001` kèm map `field → message`.
- Không tìm thấy: `...orElseThrow(() -> new AppException(ErrorCode.XXX_NOT_FOUND))`.
- Trùng dữ liệu: kiểm tra `existsBy…(…)` trước khi ghi → ném `…_EXISTED` (409).
- Giá trị có thể vắng dùng `Optional`; **không để `NullPointerException` lọt ra ngoài**.
- Search request bind bằng `@ModelAttribute` (query params); tự vệ ở service (xem Bước 5).

### Bước 5 — Danh sách LUÔN có phân trang + sort (an toàn)
Bắt buộc cho mọi endpoint list, đúng khuôn `UserServiceImpl`:
- `XxxSearchRequest` có `page=0, size=10, sortBy="id", sortDirection="ASC"` (dùng `@Builder.Default`).
- `PageRequest.of(Math.max(0, page), Math.clamp(size, 1, MAX_PAGE_SIZE /*=100*/), resolveSort(request))`.
- `resolveSort`: **whitelist** `SORTABLE_FIELDS` (chỉ cho phép sort các field an toàn; mặc định `id`)
  để tránh `PropertyReferenceException`/500; hướng `DESC` nếu khớp (không phân biệt hoa thường), còn lại `ASC`.
- Filter động qua `Specification` (null/blank → `criteriaBuilder.conjunction()`; text dùng
  `like lower(%…%)`).
- Trả `PageResponse` build từ `Page<XxxEntity>` (map content bằng `mapper::toResponse`).

### Bước 6 — Viết test đảm bảo API đúng (Service + Controller, KHÔNG cần DB)
- **Service unit test** (`XxxServiceImplTest`): JUnit 5 + Mockito
  (`@ExtendWith(MockitoExtension.class)`, `@Mock` repository/mapper, `@InjectMocks` impl). Stub
  `repository.findAll(any(), any())` bằng `PageImpl<>` và assert `PageResponse`
  (content, totalElements, pageNumber…); assert `getById` ném `AppException(XXX_NOT_FOUND)` khi rỗng;
  assert nhánh trùng dữ liệu ném `…_EXISTED`; assert `size` bị clamp và `sortBy` lạ bị ép về mặc định.
- **Controller test** (`XxxControllerTest`): `@WebMvcTest(XxxController.class)` + `@MockBean` service
  + `spring-security-test`. Dùng `@WithMockUser(authorities = "…")` (hoặc `.with(jwt())`); assert HTTP
  + JSONPath: `$.code == 1000`, `$.data.content`, `$.data.totalElements`, `$.data.first`,
  `$.data.last`; assert **403** khi thiếu quyền; assert **4001** khi body sai validation.
- Chạy `./mvnw test` (hoặc `-Dtest=XxxServiceImplTest,XxxControllerTest`).
  **Chỉ sang Bước 7 khi toàn bộ test XANH.**
- *Lưu ý:* test mới không phụ thuộc DB. Riêng `ApplicationTests.contextLoads()` có sẵn dùng
  `@SpringBootTest` nên cần MySQL thật — nếu chạy `./mvnw test` khi không có DB, chạy riêng các lớp
  test mới bằng `-Dtest=…` (đừng sửa file test cũ trừ khi được yêu cầu).

### Bước 7 — Viết báo cáo bàn giao FE (mỗi chức năng 1 file)
Chỉ viết **sau khi test xanh hết**. Tạo `docs/<TÊN_CHỨC_NĂNG>_API.md` (Tiếng Việt), theo đúng khuôn
`docs/AUTH_RBAC_API.md`:
1. Tổng quan chức năng + quyền yêu cầu.
2. Quy ước envelope + bảng error code liên quan (kèm code mới thêm).
3. **Từng endpoint**: method + path, quyền (`@PreAuthorize`), request JSON (chú thích ràng buộc field),
   response JSON, các mã lỗi có thể gặp.
4. Với list: bảng query params (`page/size/sortBy/sortDirection` + filter) + ví dụ URL + JSON
   `PageResponse<…>`.
5. Các DTO dạng interface TypeScript cho FE.
6. Bảng tổng hợp tất cả endpoint (method/path/auth/quyền/mô tả).

## 5. Checklist "Definition of Done" cho mỗi chức năng

- [ ] Đã đọc migrate; entity khớp bảng (kể cả `id` + 4 cột audit).
- [ ] Đủ tầng: Entity / Repository / Specification / DTO / Mapper / Service(+Impl) / Controller.
- [ ] Validation + check null + ném `AppException` đúng `ErrorCode`; đã thêm error code mới nếu cần.
- [ ] List có phân trang + sort an toàn (whitelist + clamp) trả `PageResponse`.
- [ ] `@PreAuthorize` đúng quyền; mọi response bọc `ApiResponse`.
- [ ] Test Service (Mockito) + Controller (`@WebMvcTest`) **xanh hết**.
- [ ] `docs/<CHỨC_NĂNG>_API.md` đã viết theo khuôn, đủ mọi endpoint.

## 6. Lệnh hay dùng

- Chạy app: `./mvnw spring-boot:run` · Build: `./mvnw clean package`
- Test tất cả: `./mvnw test` · Test 1 lớp: `./mvnw test -Dtest=XxxServiceImplTest`
- Swagger UI: `/swagger-ui.html` · OpenAPI JSON: `/v3/api-docs`
