---
phase: 1
title: "Entity Schema Fidelity Fixes"
status: done
priority: P1
effort: "M"
dependencies: []
---

# Phase 1: Entity Schema Fidelity Fixes

## Overview

Close the entity/schema gaps found by research (`plans/reports/fork-260726-2335-code-first-migration-entity-seed-report.md`, §1) **while Flyway is still active** (`ddl-auto=validate`). Doing this first, before touching Flyway wiring, means Hibernate's own validation catches any mistake immediately (mismatch = boot failure) instead of silently succeeding under permissive `ddl-auto=update` in Phase 3. This is the safety net for the whole migration.

## Requirements

- Functional: `ShippingMethodEntity` gains `code`, `description`, `estimatedDelivery`; two real uniqueness gaps get enforced; 4 hot-path entities get their composite/lookup indexes back.
- Non-functional: app must still boot cleanly against the current Flyway-managed DB (`ddl-auto=validate` unchanged in this phase) — this is the acceptance gate for the phase, not just a nice-to-have.

## Related Code Files

- Modify: `src/main/java/vn/springboot/entity/showroom/ShippingMethodEntity.java` (find exact path via the entity package — confirm with `Grep -n "class ShippingMethodEntity"`)
- Modify: `src/main/java/vn/springboot/dto/request/shipping/ShippingMethodCreateRequest.java` (add `code`, `description`, `estimatedDelivery` fields — currently only has `name`/`fee`/`sortOrder`/`isActive`)
- Modify: `ShippingMethodUpdateRequest` (same fields — find exact path via `Grep -n "class ShippingMethodUpdateRequest"`)
- Modify: `src/main/java/vn/springboot/service/impl/ShippingMethodServiceImpl.java` (`create()`/`update()` never set `code` today — must wire the new fields through)
- Modify: `ShippingMethodMapper` and/or `ShippingMethodResponse` (find exact paths — expose the 3 new fields on read, per red-team finding: a plan that adds `nullable=false` to the entity without wiring the DTO/service breaks the live `POST` shipping-method endpoint with a `PropertyValueException` the moment anyone calls it)
- Modify: `src/main/java/vn/springboot/entity/user/UserEntity.java`
- Modify: `src/main/java/vn/springboot/entity/cart/CartItemEntity.java`
- Modify: `src/main/java/vn/springboot/entity/product/ProductEntity.java`
- Modify: `src/main/java/vn/springboot/entity/order/OrderEntity.java` (verify it doesn't already declare the composite perf indexes — it already has the `uidx_orders_user_idempotency` unique constraint, just add the missing non-unique indexes alongside)
- Modify: `src/main/java/vn/springboot/entity/coupon/CouponEntity.java`
- Modify: `src/main/java/vn/springboot/entity/news/NewsEntity.java`
- Reference only (do not modify yet): `src/main/resources/db/migration/V1__init_db.sql`, `src/main/resources/db/seed/V2__seed_db.sql` — Phase 3 deletes these; this phase just reads them for exact column/index defs.

## Implementation Steps

1. **`ShippingMethodEntity`**: add `code` (`String`, `@Column(unique = true, length = 50, nullable = false)`), `description` (`String`, `@Column(length = 500)`), `estimatedDelivery` (`String`, `@Column(name = "estimated_delivery", length = 100)`). Match exact lengths from `V1__init_db.sql` `shipping_methods` table.
1a. **Wire `code` through the write path (mandatory, not optional)** — red-team confirmed `ShippingMethodServiceImpl.create()` never sets `code` today and `ShippingMethodCreateRequest` has no such field; adding `nullable = false` without this step breaks the live `POST` shipping-method endpoint with a `PropertyValueException` on the very next call. Add `code`/`description`/`estimatedDelivery` to `ShippingMethodCreateRequest` and the update-request DTO, wire them through `ShippingMethodServiceImpl.create()`/`update()`, and add basic validation (`code` required/unique, matching the DB constraint). Do this in the same commit/step as 1, not deferred.
1b. **Wire the 3 fields into the read path** — add them to `ShippingMethodResponse`/mapper so the API actually exposes them (grep `ShippingMethod` under `dto/response` and `mapper` for the exact MapStruct mapping to extend).
2. **`UserEntity`**: add `@Table(uniqueConstraints = @UniqueConstraint(name = "uidx_users_provider_provider_id", columnNames = {"provider", "provider_id"}))`.
3. **`CartItemEntity`**: add `@Table(uniqueConstraints = @UniqueConstraint(name = "uidx_cart_items_user_product", columnNames = {"user_id", "product_id"}))`. Verify column names match the actual `@JoinColumn` names on the `user`/`product` associations before writing the constraint.
4. **`ProductEntity`**: add `@Table(indexes = {@Index(name = "idx_products_type_priority", columnList = "type, priority"), @Index(name = "idx_products_status_is_featured", columnList = "status, is_featured")})` plus the remaining single-column indexes V1 declares for this table (check `V1__init_db.sql` for the exact full list — report says "6 indexes incl. 2 composites").
5. **`OrderEntity`**: add the missing non-unique indexes from V1 (`idx_orders_user_status`, `idx_orders_coupon_user`, etc. — read the exact set from `V1__init_db.sql`) via `@Table(indexes = ...)`, additive alongside the existing `uniqueConstraints`.
6. **`CouponEntity`**: add its ~6 missing indexes the same way.
7. **`NewsEntity`**: add its missing indexes (`priority`, `status`, `published_at`).
8. After each entity edit, do NOT change `ddl-auto` yet. Run `./mvnw spring-boot:run` (or `./mvnw compile` at minimum) against the existing dev DB to confirm Hibernate's `validate` mode still passes — a typo in a column name or an index on a non-existent column fails loud here, which is the point.

## Success Criteria

- [x] `ShippingMethodEntity` has `code`/`description`/`estimatedDelivery` mapped, writable through `ShippingMethodCreateRequest`/`UpdateRequest` + `ShippingMethodServiceImpl`, and exposed through `ShippingMethodResponse`
- [x] `POST`/`PUT` shipping-method endpoints still work end-to-end after the `nullable = false` change (manually exercise, don't just rely on `ddl-auto=validate` boot success — that only checks schema shape, not runtime writes)
- [x] `UserEntity` and `CartItemEntity` have the two missing composite unique constraints
- [x] `ProductEntity`, `OrderEntity`, `CouponEntity`, `NewsEntity` have their V1 indexes reproduced via `@Table(indexes = ...)`
- [x] App still boots successfully with `ddl-auto=validate` unchanged (proves the new annotations are consistent with the real V1 schema before Flyway is removed)

## Execution Notes (2026-07-27)

- Added `code`/`description`/`estimatedDelivery` to `ShippingMethodEntity`, both request DTOs, `ShippingMethodResponse`, and wired create/update in `ShippingMethodServiceImpl` with a new `SHIPPING_METHOD_CODE_EXISTED` (4108) conflict check (`existsByCode`/`existsByCodeAndIdNot` added to `ShippingMethodRepository`), following the same pattern as `CouponServiceImpl`. `ShippingMethodMapper` needed no change (MapStruct auto-maps by matching field name).
- `UserEntity`/`CartItemEntity`: added the two composite unique constraints via `@Table(uniqueConstraints = ...)`.
- `ProductEntity` (6 indexes), `OrderEntity` (8 indexes, alongside its existing unique constraint), `CouponEntity` (6 indexes), `NewsEntity` (4 indexes) — added via `@Table(indexes = ...)`, matching V1's non-unique indexes exactly (columns already covered by a `unique=true`/existing unique constraint, e.g. `sku`/`slug`/`code`/`order_code`, were skipped since MySQL auto-indexes those).
- Existing `ShippingMethodControllerTest.validCreateRequest()` fixture updated to send `code` (2 tests were failing on `@NotBlank` before the fix); full non-DB test suite (306 tests) passes.
- **Verified against a real MySQL instance** (started `docker compose -f docker-compose.yml -f docker-compose.local.yml up -d db` fresh, `DB_URL` pointed at `localhost:3306`): app booted with `ddl-auto=validate` unchanged — Flyway created the V1/V2 schema+seed, Hibernate validated clean, zero `SchemaManagementException`. Then exercised the live API: `GET /api/shipping-methods` returns the 3 new fields from real seed data; logged in as `admin`/`admin123` (confirmed `role=ADMIN` already, pre-existing seed data); `POST /api/shipping-methods` with `code` succeeds end-to-end; duplicate `code` correctly 409s; `PUT` partial update works; test row deleted afterward. App and the throwaway DB container were both torn down after verification (`docker compose down`, no `-v`, so no stale volume beyond the empty removed one).
- **Correction to a plan premise**: Phase 2's justification for adding `SeedRunnerIntegrationTest` as "the first test in the repo" is factually wrong — `src/test/java` already has 47 existing test files (Mockito unit tests + `@WebMvcTest` controller tests), just no test that boots a full `@SpringBootTest` context against a real DB except `ApplicationTests` (a bare `contextLoads()`, which also hits the real DB and can't run without Docker). The integration test is still worth adding for seeder regression coverage; the "first test" framing in the plan should just be dropped as inaccurate, not the decision itself.

## Risk Assessment

- **Risk**: a typo'd column name in a new `@Index`/`@UniqueConstraint` silently does nothing under `validate` (Hibernate only validates columns it's told about, not arbitrary index definitions against the live schema) — indexes aren't schema-validated the same way columns are. **Mitigation**: after this phase, also visually diff each new annotation against the exact `V1__init_db.sql` column names (copy-paste column names, don't retype).
- **Risk (confirmed by red-team, now mitigated by step 1a/1b above, not deferred)**: adding `unique = true, nullable = false` on `ShippingMethodEntity.code` without also updating `ShippingMethodCreateRequest`/`ShippingMethodServiceImpl.create()` breaks the live `POST /shipping-methods` endpoint immediately (`PropertyValueException`, not caught by `ddl-auto=validate` since that only checks schema shape at boot, not runtime writes). This is why steps 1a/1b are mandatory parts of this phase, not a follow-up.
