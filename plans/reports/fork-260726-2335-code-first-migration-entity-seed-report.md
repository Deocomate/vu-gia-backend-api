# Code-First Migration Research — Entity/Schema Gaps & Seed Data Catalog

Scope: `vu-gia-backend-api` DB-first (Flyway) → code-first (Hibernate `ddl-auto=update`, Flyway removed) migration. Source files reviewed: all 32 entity classes under `src/main/java/vn/springboot/entity/**` + `common/entity/BaseEntity.java`, `db/migration/V1__init_db.sql` (412 lines, 21 tables), `db/seed/V2__seed_db.sql` (3160 lines, 13 seeded domains).

## 1. Entity/Schema Gaps

General finding: entity annotation quality is high — `@Enumerated(EnumType.STRING)` used everywhere an enum-backed column exists, `@JdbcTypeCode(SqlTypes.JSON)` + `columnDefinition="json"` used correctly for every JSON column, `@ManyToOne`/`@JoinColumn` used for every FK (no raw `Long xxxId` shortcuts found), column lengths mostly match V1. Two systematic gaps and one critical field-level gap below.

### Critical: ShippingMethodEntity is missing 3 mapped columns
`V1__init_db.sql` `shipping_methods` table has `code VARCHAR(50) UNIQUE NOT NULL`, `description VARCHAR(500)`, `estimated_delivery VARCHAR(100)` — none of these three are fields on `ShippingMethodEntity` (only `name`, `fee`, `sortOrder`, `isActive` are mapped). The seed data (`V2__seed_db.sql` lines 79-116) inserts real values into all three via raw SQL (bypassing JPA), so they exist in the DB today, but no Java code reads them (confirmed via grep — no `getCode()`/`getEstimatedDelivery()` call site exists anywhere in `service`/`mapper`/`controller`). Under `ddl-auto=update` these columns will simply not be (re)created — must add the 3 missing `@Column` fields to `ShippingMethodEntity` before/alongside the seeder work, or consciously drop them (confirm with user — they carry real content: shipping codes `STANDARD`/`EXPRESS`, delivery-time copy).

### Systematic: no named indexes anywhere except one unique constraint
V1 declares ~40 named single/composite performance indexes (`idx_users_phone`, `idx_products_type_priority`, `idx_orders_user_status`, `idx_orders_coupon_user`, etc. — every table has 2-8). None of the 32 entities use `@Table(indexes = ...)`. Only `OrderEntity` declares one `@Table(uniqueConstraints = @UniqueConstraint(...))` for `uidx_orders_user_idempotency`; all other V1 `UNIQUE INDEX`/`UNIQUE` columns are already covered via `@Column(unique = true)` (e.g. `users.username`, `products.slug`, `coupons.code`) — those are safe. But `INDEX` (non-unique) declarations are entirely lost under `ddl-auto=update` — MySQL auto-creates an index for FK columns only, not for standalone/composite lookup indexes like `idx_products_type_priority (type, priority)` or `idx_orders_user_status (user_id, status)`. Not a correctness bug (queries still work), but a real performance regression across most list/filter endpoints (products, orders, coupons, news) if not reproduced via `@Table(indexes = ...)`. One exception already covered: `users.uidx_users_provider_provider_id` (composite unique on `provider`+`provider_id`) is NOT currently declared anywhere (no `@Table(uniqueConstraints=...)` on `UserEntity`) — this one **is** a correctness gap (silently loses a real uniqueness guarantee), not just performance.

### Stale doc comment
`V1__init_db.sql` header says "19 bảng cốt lõi" but the file defines 21 tables (`redirects` and `newsletter_subscribers` were added later without updating the comment). Not a code gap, just noise — irrelevant once the file is deleted per the Flyway-removal decision.

### Per-table entity mapping check (all 21 V1 tables)

| Table | Entity | Enum storage | JSON columns | Unique/index gaps | Notes |
|---|---|---|---|---|---|
| users | UserEntity | `role` STRING ✓ | — | Missing composite unique `(provider, provider_id)` — **add via `@Table(uniqueConstraints=...)`** | Missing indexes: phone, role, name, created_at (perf only) |
| pages | PageEntity | `status` STRING ✓ | `content` ✓ | none | Missing index: key (perf only, `key` already has `@Column(unique=true)` so it's indexed anyway via the unique constraint) |
| contact_requests | ContactRequestEntity | `status` STRING ✓ | — | none | Missing indexes: status, handled_by_id (perf only — handled_by_id gets an implicit FK index from the `@ManyToOne`) |
| news_categories | NewsCategoryEntity | — | — | none | Missing index: priority (perf only) |
| news | NewsEntity | `status` STRING ✓ | `des` ✓ | none | Missing indexes: priority, status, published_at (perf only; news_category_id FK auto-indexed) |
| product_categories | ProductCategoryEntity | `category_type` STRING ✓ | `detail_content` ✓ | none | Missing index: priority (perf only) |
| products | ProductEntity | `type`, `status` STRING ✓ | `description`, `detail_sections`, `combo_products`, `functions`, `combo_gallery` all ✓ | none | Missing 6 indexes incl. 2 composites (`type+priority`, `status+is_featured`) — perf only, but products listing/filtering is a hot path, worth reproducing |
| product_images | ProductImageEntity | — | — | none | No `BaseEntity` (matches V1 — table has no audit columns). Missing indexes: product_id (auto via FK), priority (perf only) |
| cart_items | CartItemEntity | — | `combo_items` ✓ | Missing composite unique `(user_id, product_id)` — **add via `@Table(uniqueConstraints=...)`** | No `BaseEntity` (matches V1). Not in seed scope but flagged since it's a real correctness gap like the users one |
| coupons | CouponEntity | `discount_type` STRING ✓ | — | none | Missing 6 indexes (perf only) |
| orders | OrderEntity | `status`, `payment_status`, `payment_method` STRING ✓ | — | Composite unique already correctly declared ✓ | Missing several indexes incl. composites (perf only) |
| order_items | OrderItemEntity | `product_type` STRING ✓ | `combo_items` ✓ | none | No `BaseEntity` (matches V1) |
| payment_transactions | PaymentTransactionEntity | — | — | none | `sepay_id` unique ✓ via `@Column`. No `BaseEntity`, uses `@CreationTimestamp` instead (matches V1 — no `updated_at` column) |
| refresh_tokens | RefreshTokenEntity | — | — | none | `token` unique ✓. No `BaseEntity` (matches V1 — no audit columns) |
| banners | BannerEntity | `position` STRING ✓ | — | none | Missing indexes: position, is_active (perf only) |
| showrooms | ShowroomEntity | — | — | none | Missing indexes: is_active, sort_order (perf only) |
| shipping_methods | ShippingMethodEntity | — | — | **3 missing columns — see Critical above** | `code` unique constraint also lost as a side effect of the missing field |
| gallery_images | GalleryImageEntity | — | — | none | Missing indexes: is_active, sort_order (perf only) |
| faqs | FaqEntity | — | — | none | Missing indexes: is_active, sort_order, category (perf only) |
| redirects | RedirectEntity | — | — | none | `from_path` unique ✓. Missing index: is_active (perf only) |
| newsletter_subscribers | NewsletterSubscriberEntity | — | — | none | `email` unique ✓. No `BaseEntity`, `@CreationTimestamp` only (matches V1) |

**Action items for the implementer:** (1) add the 3 missing fields to `ShippingMethodEntity` (or confirm dropping them), (2) add `@Table(uniqueConstraints=...)` for `users(provider, provider_id)` and `cart_items(user_id, product_id)` — genuine correctness gaps, not cosmetic, (3) decide whether to reproduce the ~40 performance indexes via `@Table(indexes=...)` across entities (recommended for `products`, `orders`, `coupons`, `news` at minimum — these back real list/filter endpoints) or accept the regression per YAGNI given DB size is currently tiny.

## 2. Seed Data Catalog

Required insert order (FK-safe): **shipping_methods, users, product_categories → products → product_images, news_categories → news, banners, showrooms, gallery_images, faqs, pages, coupons.** Only two real FK chains exist in the seeded set: `product_categories → products → product_images`, and `news_categories → news`. Everything else is independent.

| Domain | Rows | Line range | FK deps | Hardcoded cross-refs | Notes |
|---|---|---|---|---|---|
| users | 1 | 45-76 | none | none | Explicit `id=2` (id=1 intentionally skipped, `AUTO_INCREMENT` reset to 3 after). **Conflicts with `DataInitializer.java`**: that class seeds a *different* admin (`admin@gmail.com`/`admin123`) but only if `username=admin` doesn't already exist — since this seed always creates `username=admin` first (email `admin@gomvugia.vn`, same `admin123` password per the bcrypt comment), `DataInitializer` is currently dead code in practice. **README.md documents the wrong email** (`admin@gmail.com`) — actual seeded login is `admin` / `admin123` / `admin@gomvugia.vn`. Decide: keep one seeding mechanism (recommend folding into the new Java seeder, delete/repurpose `DataInitializer.java`), and fix the README. |
| shipping_methods | 2 | 79-116 | none | none | Explicit ids 1 (STANDARD), 2 (EXPRESS). Needs the 3 missing entity fields fixed first (see Section 1) or the seeder can't populate them. |
| product_categories | 6 | 121-220 | none | none | Explicit ids 1-6, one row per `CategoryType` enum value (fixed set, matches entity comment "never created/deleted at runtime"). |
| products | 12 | 228-796 | `product_category_id` → product_categories 1-6 | **Yes — critical**: product id=12 (`combo_products` JSON) hardcodes `productId: 9, 10, 11` referencing 3 other rows in the *same* insert batch (ids 9-11 are the combo's sub-items, inserted earlier in the same statement). | JSON fields (`description`, `detail_sections`, `combo_products`, `functions`, `combo_gallery`) are built via MySQL `JSON_OBJECT()`/`JSON_ARRAY()` functions in the SQL — must be transcribed to literal JSON text (Java text blocks or `ObjectMapper`-built strings) since entities store JSON as a plain `String` field. Row sizes vary a lot: simple SINGLE products have a 1-block description (~10 lines), the combo (id=12) has a full `attributes`+`specifications` structure (~160 lines). **Design recommendation**: don't rely on IDENTITY auto-increment happening to reproduce ids 1-12 in order (fragile, breaks if insertion order changes) — instead capture each saved `ProductEntity` into a `Map<Integer originalSeedId, ProductEntity>` (or a `List` indexed by insertion order) as the seeder runs, then build `combo_products` JSON for id=12 using the *real* generated ids of the already-saved sub-items 9-11. |
| product_images | 15 | 801-821 | `product_id` → products 1-12 (by position, no own explicit id) | Same product-id dependency as above — must resolve via the id-map from the products step, not literal ids 1-12 | Straightforward once products exist; no JSON, no cross-image dependency. |
| news_categories | 3 | 826-859 | none | none | Explicit ids 1-3. |
| news | 22 | 862-2370 | `news_category_id` → news_categories 1-3 | none beyond the category FK | By far the largest section (~1509 lines) purely due to per-article JSON body length (each `des` block is a list of paragraph/image/heading JSON objects, 10-70 lines per article) — high transcription volume, low structural complexity. 21 regular articles + 1 "detail demo" article, comment marks which 3 map to which category. |
| banners | 6 | 2374-2445 | none | none | No explicit id (auto-increment). Split 3 `HOME_HERO` + 3 `HOME_CATEGORY` (enum `BannerPosition`). |
| showrooms | 1 | 2450-2473 | none | none | No explicit id. One row, includes a long Google Maps embed URL. |
| gallery_images | 5 | 2478-2533 | none | none | No explicit id. |
| faqs | 19 | 2538-2724 | none | none | No explicit id. 5 category groups by row count: Sản phẩm (5), Báo giá (5), Vận chuyển & thời gian giao hàng (5), Chính sách bảo hành (2), Đổi trả (2). `category` is a plain string label, not an FK. |
| pages | 5 | 2729-3075 | none | none | No explicit id, keyed by unique `key` string: `home`, `about`, `privacy-policy`, `shipping-policy`, `return-policy`. `content` JSON structure differs per page (some use `sections[].type=productList/heading/block/image`, no shared schema) — each page's JSON must be transcribed individually, no shared builder helper is likely to pay off (5 distinct shapes). |
| coupons | 3 | 3080-3145 | none | none | No explicit id. `WELCOME10` (PERCENT), `GIAM50K` (FIXED), `FREESHIP` (FREE_SHIP) — matches `DiscountType` enum. |

Trailing `ALTER TABLE ... AUTO_INCREMENT = N` statements (lines 3147-3159) exist only to resync MySQL's counter after the explicit-id inserts (users, product_categories, products, news_categories) — not needed once seeding goes through JPA `save()` (IDENTITY columns stay in sync automatically), so these can be dropped entirely rather than ported.

## Unresolved questions
- Confirm whether to keep or drop the 3 unmapped `ShippingMethodEntity` columns (`code`, `description`, `estimated_delivery`) — they have real seed content but zero current Java call sites.
- Confirm which admin identity is canonical once `DataInitializer.java` and the SQL-seeded admin are consolidated into one Java seeder (`admin@gomvugia.vn` per the SQL seed that's actually active today, vs `admin@gmail.com` per README/`DataInitializer` defaults, which never actually wins).
- Confirm whether to reproduce the ~40 lost performance indexes via `@Table(indexes=...)` (recommended at least for `products`, `orders`, `coupons`, `news`) or accept the regression given current DB size.
