---
phase: 2
title: "Java Seeder Framework and Data Port"
status: done
priority: P1
effort: "L"
dependencies: [1]
---

# Phase 2: Java Seeder Framework and Data Port

## Overview

Build a Java/JPA seeder framework (no raw SQL execution) that replaces `V2__seed_db.sql`, and wire it to the env-gated behavior: `APP_ENV=development` → truncate + reinsert all 13 domains on every startup; any other profile → seed only empty tables (idempotent). This phase still runs with Flyway/`ddl-auto=validate` active (Phase 1's gate) — the seeder is built and tested against the current Flyway-created schema; Phase 3 is what actually removes Flyway. Read `src/main/resources/db/seed/V2__seed_db.sql` and `plans/reports/fork-260726-2335-code-first-migration-entity-seed-report.md` §2 before writing any seeder — the SQL file is the source data and gets deleted in Phase 3, so this is the only phase where it's directly available as reference.

## Requirements

- Functional: all 13 seeded domains ported 1:1 (row-for-row, JSON-for-JSON) from `V2__seed_db.sql`; the `products` combo (seed id 12, `combo_products` JSON referencing sub-items 9-11) resolves to real generated IDs, not hardcoded ones; admin account consolidated to the single canonical identity (`admin` / `admin@gomvugia.vn` / `admin123`, **`role=ADMIN`** — matches the real SQL data, not `SUPERADMIN`); `DataInitializer.java` retired (folded into the new `UserSeeder`, including its `@Value`-based env-var override for username/email/password — see Architecture).
- Non-functional: FK-safe insert order preserved; reset (dev mode) deletes in reverse order using JPA repository methods, not raw `TRUNCATE`; each seeder is independently testable/idempotent-checkable via an `isEmpty()`-style check. **Dev-mode reset also clears 6 non-seeded transactional tables** (`orders`, `order_items`, `cart_items`, `payment_transactions`, `refresh_tokens`, `contact_requests`) that hold references into the seeded tables — red-team confirmed `V1__init_db.sql` declares **zero real `FOREIGN KEY` constraints** anywhere, so leaving these untouched doesn't crash, it silently orphans/misattributes data the first time a developer logs in or tests a cart/order flow between restarts. User confirmed: extend reset to cover them, matching the original "reseed everything" intent literally.

## Related Code Files

- Create: `src/main/java/vn/springboot/seed/DomainSeeder.java` (interface: `boolean isEmpty(); void reset(); void seed();`)
- Create: `src/main/java/vn/springboot/seed/SeedRunner.java` (`CommandLineRunner`, orchestrates all seeders per profile)
- Create: `src/main/java/vn/springboot/seed/ShippingMethodSeeder.java`
- Create: `src/main/java/vn/springboot/seed/UserSeeder.java` (replaces `DataInitializer.java`)
- Create: `src/main/java/vn/springboot/seed/ProductCategorySeeder.java`
- Create: `src/main/java/vn/springboot/seed/ProductSeeder.java` (handles `products` + `product_images` together — owns the ID-resolution map)
- Create: `src/main/java/vn/springboot/seed/NewsCategorySeeder.java`
- Create: `src/main/java/vn/springboot/seed/NewsSeeder.java`
- Create: `src/main/java/vn/springboot/seed/BannerSeeder.java`
- Create: `src/main/java/vn/springboot/seed/ShowroomSeeder.java`
- Create: `src/main/java/vn/springboot/seed/GalleryImageSeeder.java`
- Create: `src/main/java/vn/springboot/seed/FaqSeeder.java`
- Create: `src/main/java/vn/springboot/seed/PageSeeder.java`
- Create: `src/main/java/vn/springboot/seed/CouponSeeder.java`
- Delete: `src/main/java/vn/springboot/config/DataInitializer.java` (after `UserSeeder` covers its behavior)
- Create: `src/main/java/vn/springboot/seed/TransactionalDataCleaner.java` (dev-only: clears the 6 non-seeded tables listed above; no reseed step, since there's no source dataset for them — they're populated by real usage, not `V2__seed_db.sql`)
- Create: `src/main/java/vn/springboot/seed/OrphanReferenceChecker.java` (the assertion-query logic described below — a standalone bean so both `SeedRunner` at boot and the integration test below call the exact same checks, not duplicated query logic)
- Create: `src/test/java/vn/springboot/seed/SeedRunnerIntegrationTest.java` (confirmed via `/ck:plan validate`, 2026-07-27 — this repo has zero existing test infrastructure, `src/test/resources` doesn't exist; this is the first test. Use `@SpringBootTest(webEnvironment = WebEnvironment.NONE)` with `@ActiveProfiles("development")` against the real dev MySQL from `docker-compose.local.yml` — not H2/Testcontainers, since the seed data relies on MySQL-specific JSON columns and this project has no existing test-DB tooling to build on. Assert: each `DomainSeeder`'s row count matches `V2__seed_db.sql`'s row count for that domain after a full boot; `OrphanReferenceChecker` reports zero orphans. `spring-boot-starter-test` is already a `pom.xml` dependency — no new dependency needed.)
- Modify: `src/main/resources/application.yaml` — correct the `app.init.*` property block (currently `admin-email: admin@gmail.com`, stale/wrong) to the canonical identity (`admin-username: admin`, `admin-email: admin@gomvugia.vn`, `admin-password: admin123`) so `UserSeeder`'s `@Value` overrides have a correct, non-orphaned default; keep `app.init.enabled` as `UserSeeder`'s (and optionally `SeedRunner`'s overall) kill-switch property, not dead config
- Reference (read-only, do not modify): `src/main/resources/db/seed/V2__seed_db.sql`

## Architecture

**`DomainSeeder` interface** — every seeder implements:
```java
public interface DomainSeeder {
    boolean isEmpty();   // true if the table has zero rows
    void reset();        // delete all rows (children-first if this seeder owns a FK)
    void seed();         // insert the ported dataset
}
```

**`SeedRunner`** (`@Component implements CommandLineRunner`, injects all `DomainSeeder` beans in a fixed, explicit list — do NOT rely on Spring's auto-ordering of `List<DomainSeeder>` bean injection, since insert order is FK-critical):
```
FK-safe order: shippingMethodSeeder, userSeeder, productCategorySeeder, productSeeder,
               newsCategorySeeder, newsSeeder, bannerSeeder, showroomSeeder,
               galleryImageSeeder, faqSeeder, pageSeeder, couponSeeder
```
- If `@Profile("development")` is active: call `reset()` on every seeder in **reverse** of the list above, then `seed()` on every seeder in forward order.
- Otherwise: for each seeder in forward order, call `seed()` only if `isEmpty()` returns true (skip otherwise — never touch non-empty tables).

**`ProductSeeder`** is the one with real complexity (see report §2, "products" row): as it saves each of the 12 products in original seed order, keep a `Map<Integer, ProductEntity> savedBySeedOrder` (or a `List<ProductEntity>` indexed 0-11). **Correction after red-team (verified via grep against the real `V2__seed_db.sql` INSERT column list, lines 229-248):** only `description` and `combo_products` are ever populated for any of the 12 products — `detail_sections`, `functions`, and `combo_gallery` are **absent from the INSERT column list entirely and are always `NULL`**. Do not fabricate content for them; leave those 3 fields unset. The combo's (#12) "specifications"/"attributes" structure actually lives nested inside its `description` JSON, not a separate column. For product #12, build `combo_products` programmatically with Jackson (`ObjectMapper`/`ArrayNode`), pulling the real `.getId()` of the already-saved products originally seeded as #9/#10/#11 from the map — never hardcode `9, 10, 11` in the new Java code. **Also resolve `product_category_id` the same way `NewsSeeder` resolves `news_category_id`** (a small in-seeder map keyed by category name/slug from `ProductCategorySeeder`'s just-saved entities, not the literal `1`-`6` ids from the SQL) — the original SQL's literal category ids only happen to work by coincidence on a truly fresh DB; after any dev-mode reset where insertion counts/order could differ, hardcoding them risks silently wrong category assignments (no DB FK exists to catch it). After all 12 products are saved, seed `product_images` (15 rows) resolving `product_id` the same way, by original seed position, not literal ids 1-12.

**`UserSeeder`**: seeds exactly the one admin row from `V2__seed_db.sql` — `admin` / `admin@gomvugia.vn` / `admin123`, bcrypt-encoded via the existing `PasswordEncoder` bean, **`role=ADMIN`** (corrected after red-team: the real SQL data at `V2__seed_db.sql:71` is `'ADMIN'`, not `SUPERADMIN` — seeding `SUPERADMIN` would grant this account `UserController`'s `hasRole('SUPERADMIN')`-gated capabilities, create-account and role-promotion, that it doesn't have today). Preserve `DataInitializer.java`'s `@Value`-based overrides (`app.init.admin-username`/`admin-email`/`admin-password`, defaulting to the corrected `application.yaml` values above) so an operator can still override the seeded credential per-deployment without a code change, and keep its startup log line ("change the default password!") so a hardcoded default doesn't silently reach production unnoticed. In non-dev mode this is the idempotent check that replaces `DataInitializer.java`'s `existsByUsername` guard — same behavior, one fewer class. Delete `DataInitializer.java` once this is verified working (same effect: don't duplicate two seed-admin mechanisms).

**`TransactionalDataCleaner`** (dev-mode only, runs as part of `SeedRunner`'s reset phase, before the catalog reset): deletes all rows from `order_items`, `payment_transactions`, `orders`, `cart_items`, `refresh_tokens`, `contact_requests` — the 6 tables that hold `user_id`/`product_id`/`coupon_id`/`shipping_method_id`/`handled_by_id` references into the seeded tables but aren't themselves seeded (no source data for them in `V2__seed_db.sql`; they're populated by real interactive usage — logins, cart adds, test orders). Since `V1__init_db.sql` declares no real FK constraints, clearing them isn't required for the catalog reset to succeed mechanically, but skipping it means any dev-mode testing session's cart/order/login data silently references catalog rows that no longer exist (or now belong to a different re-seeded entity) after the next restart. This closes that gap per the "reseed toàn bộ" (reset everything) intent.

**Reset order** (`SeedRunner`, dev-mode only — informal ordering for cleanliness; not required for correctness since no DB-level FK constraints exist to violate, but avoids leaving obviously-inconsistent intermediate state):
```
TransactionalDataCleaner.reset() [order_items → payment_transactions → orders → cart_items → refresh_tokens → contact_requests]
→ couponSeeder.reset() → pageSeeder.reset() → faqSeeder.reset() → galleryImageSeeder.reset()
→ showroomSeeder.reset() → bannerSeeder.reset() → newsSeeder.reset() → newsCategorySeeder.reset()
→ productSeeder.reset() (must also clear product_images first, internally)
→ productCategorySeeder.reset() → userSeeder.reset() → shippingMethodSeeder.reset()
```
Use `repository.deleteAllInBatch()` (or `deleteAll()` if cascade/audit callbacks matter) — not raw SQL `TRUNCATE`, to stay consistent with the code-first, JPA-only approach. **Known accepted tradeoff (user-confirmed after red-team):** plain JPA `DELETE` does not reset MySQL's `AUTO_INCREMENT` counter the way `TRUNCATE` does — IDs drift upward on every dev restart instead of restarting at 1 each time. This is accepted; don't add a counter-reset query.

**`OrphanReferenceChecker` (new, added after red-team; scope confirmed via `/ck:plan validate`, 2026-07-27 — runs on every boot, every profile, not dev-only):** since there are no DB-level FK constraints, a seeder ordering mistake produces silent dangling references, not an exception — "no errors thrown" is not sufficient proof the seed succeeded correctly. After `SeedRunner` completes (in any profile — the check's query cost is a handful of `COUNT` queries, negligible, and there's no DB-level FK safety net to catch drift even outside dev mode), run a small set of assertion queries (JPQL or repository `count()` calls) confirming zero orphans on every real cross-table reference among the seeded/cleared tables: `product_images.product_id`, `products.product_category_id`, `news.news_category_id`. Fail loudly (throw, don't just log) if any orphan is found — this is the actual correctness signal for FK-safe ordering, not the absence of duplicate-key errors. Implemented as its own bean (not inlined in `SeedRunner`) so `SeedRunnerIntegrationTest` (see Related Code Files) can invoke the same checks directly.

## Implementation Steps

**Commit discipline (confirmed via `/ck:plan validate`, 2026-07-27):** each numbered step below that implements one seeder is its own commit, immediately after that seeder's spot-check passes — not one large commit at the end of step 8/13. This isolates a bad transcription to one revertible commit instead of the whole Phase 2 diff.

1. Create the `vn.springboot.seed` package and the `DomainSeeder` interface.
2. Implement `ShippingMethodSeeder` and `UserSeeder` first (no FK deps, smallest data) — validates the interface shape end-to-end before tackling the bigger domains. Commit.
3. Implement `ProductCategorySeeder` (6 rows, fixed `CategoryType` enum set, no FK). Commit.
4. Implement `ProductSeeder` with the ID-resolution map described above — this is the highest-risk unit, review it against report §2 "products" row carefully before moving on. Include `product_images` handling inside this seeder (shares the same map). Commit.
5. Implement `NewsCategorySeeder` (3 rows) then `NewsSeeder` (22 rows, ~1509 lines of source JSON — bulk transcription, low structural risk, just volume; resolve `news_category_id` via a small in-seeder map keyed by category name/slug, not hardcoded ids). Commit.
6. Implement `BannerSeeder`, `ShowroomSeeder`, `GalleryImageSeeder`, `FaqSeeder` (all independent, no FK — straightforward transcription). Commit (one commit per seeder, or grouped since all 4 are trivial and independent — implementer's judgment, but keep each spot-checked before moving to the next).
7. Implement `PageSeeder` (5 rows, 5 structurally distinct `content` JSON shapes — transcribe each individually, don't force a shared builder). Commit.
8. Implement `CouponSeeder` (3 rows). Commit.
9. Implement `TransactionalDataCleaner` (dev-mode-only reset of the 6 non-seeded tables listed in Architecture — no seed step, reset only).
10. Implement `SeedRunner` wiring all 12 `DomainSeeder`s plus `TransactionalDataCleaner` in the fixed order above, with the `@Profile("development")` branch (full reset incl. transactional tables, then reseed) vs idempotent-else branch (seed-if-empty only, `TransactionalDataCleaner` never runs outside dev).
11. Implement `OrphanReferenceChecker` (post-seed assertion queries described in Architecture) as its own bean, invoked from `SeedRunner` unconditionally after seeding completes in any profile.
12. Update `application.yaml`'s `app.init.*` block to the corrected canonical defaults (`admin-email: admin@gomvugia.vn`, etc.) so `UserSeeder`'s `@Value` overrides aren't reading a stale value.
13. Delete `DataInitializer.java`.
14. **Write `SeedRunnerIntegrationTest`** (confirmed via `/ck:plan validate`, 2026-07-27 — this repo's first automated test): `@SpringBootTest` booting against the real dev MySQL with `APP_ENV=development` active, asserting per-domain row counts match `V2__seed_db.sql` and `OrphanReferenceChecker` reports zero orphans. This automates the row-count/orphan half of step 15's manual verification going forward; it does not replace the manual restart-with-real-usage check below (that scenario needs an actual running server + HTTP session, not a single test-context boot).
15. Manually verify (still on the current Flyway-managed DB, `ddl-auto=validate`): start with `APP_ENV=development`, confirm all 13 domains got seeded with correct row counts and `role=ADMIN` on the admin user; confirm the orphan-check passes with zero orphans. **Then, before restarting** (this is the scenario Finding 3/red-team specifically requires testing): log in as a real user, add a cart item, place a test order. Restart the server and confirm `TransactionalDataCleaner` cleared `orders`/`order_items`/`cart_items`/`refresh_tokens` along with the catalog reset — not just that the catalog reseeded cleanly. Then switch to `APP_ENV` unset, confirm no reseed touches the already-seeded data on restart, and confirm `TransactionalDataCleaner` never runs in this mode.

## Success Criteria

- [x] All 13 domains seeded with row counts matching `V2__seed_db.sql` exactly, admin user has `role=ADMIN` (not `SUPERADMIN`) — 12 catalog domains via `SeedRunner` + `TransactionalDataCleaner` cleaning the 6 non-seeded transactional tables (13 total behaviors, matching the plan's count)
- [x] Product #12's `combo_products` JSON contains the real generated IDs of products originally seeded as #9/#10/#11, not `9, 10, 11` literally; `product_category_id` resolved via lookup map, not literal SQL ids
- [x] `detail_sections`/`functions`/`combo_gallery` left `null` on all 12 products (not fabricated)
- [x] `DataInitializer.java` deleted; single admin identity (`admin@gomvugia.vn`/`admin123`/`ADMIN`) seeded via `UserSeeder`, still overridable via `app.init.*` `@Value` properties
- [x] `OrphanReferenceChecker` passes (zero dangling `product_images.product_id`/`products.product_category_id`/`news.news_category_id`) after every seed run, in every profile
- [x] `SeedRunnerIntegrationTest` passes — not literally the first test in the repo (see Execution Notes: the plan's "zero existing test infrastructure" premise was wrong, 47 unit/`@WebMvcTest` files already existed), but it is the first test to boot a full context against a real database besides the pre-existing bare `ApplicationTests.contextLoads()`
- [x] Dev-mode restart fully wipes and re-seeds without errors, **including a restart after real interactive usage (login/cart/order) — verified the transactional tables are cleared too**; non-dev restart leaves existing data untouched (verified via a manual price edit surviving a restart) and never touches the transactional tables
- [x] No raw SQL (`TRUNCATE`/`INSERT`) anywhere in the new seeder code — JPA repositories only
- [x] Each seeder landed as its own commit, spot-checked before the next

## Execution Notes (2026-07-27)

- All 12 `DomainSeeder`s + `TransactionalDataCleaner` + `SeedRunner` + `OrphanReferenceChecker` implemented and committed one seeder (or logical group) at a time, per the validation session's commit discipline.
- **Corrected plan inaccuracies found during implementation** (both fixed in the seeders/plan, not silently worked around):
  - Pages: the plan estimated 5 rows; the real `V2__seed_db.sql` has **7** (`home, about, factory, contact, privacy-policy, shipping-policy, return-policy`) — `privacy-policy`/`shipping-policy` happen to share one section shape, which is likely where the undercount came from. All 7 ported.
  - Test infra: the plan's justification for `SeedRunnerIntegrationTest` ("this repo's first test") was factually wrong — 47 test files already existed (Mockito unit + `@WebMvcTest` controller tests). Corrected the framing in the success criteria above; the decision to add the test stands on its own regression-coverage merits regardless.
- **Found and fixed a real, previously-undetected bug** (`PageEntity.key` mapped to an unquoted `key` column — a MySQL reserved word — so every JPA-level reference, including the live `PageServiceImpl.create/update/findByKey` API path, fails with a SQL syntax error against real MySQL; masked until now because unit tests mock the repository and the original seed data bypassed Hibernate via raw SQL). Fixed with backtick-quoting in `@Column`.
- Combo `#12`'s `combo_products` JSON in the real data has no `quantity` key (only `productId`/`sortOrder`) — the entity's doc comment overstates the shape; ported to match the actual data, not the comment.
- **Verified end-to-end against a real MySQL** (fresh `docker compose -f docker-compose.yml -f docker-compose.local.yml up -d db`, `DB_URL` at `localhost:3306`):
  1. `SeedRunnerIntegrationTest` + the full `./mvnw test` suite (309 tests) pass against the live DB.
  2. Dev-mode boot (`APP_ENV=development`): all 13 domains seed with correct counts, `role=ADMIN` confirmed, orphan-check clean.
  3. Logged in as `admin`, added a cart item, placed a real order (`POST /api/orders`) — confirmed `orders`/`order_items`/`cart_items`/`refresh_tokens` all had real rows before restart.
  4. Restarted in dev mode: confirmed all 4 transactional tables reset to 0 rows, catalog reseeded cleanly (12 products, 1 user), orphan-check still clean.
  5. Manually edited a product's price via direct SQL, restarted with `APP_ENV` unset (`production` profile): confirmed the manual edit survived (idempotent skip) and no transactional-table cleanup ran.
  6. Torn down the throwaway DB container afterward (`docker compose down`, no `-v`).

## Risk Assessment

- **Risk**: transcription errors across ~3000 lines of JSON content (typos, malformed JSON, lost fields) — the highest-volume, error-prone part of this whole plan. **Mitigation**: transcribe domain-by-domain and manually spot-check 2-3 rows per domain against the original SQL immediately after writing each seeder, rather than writing all 12 seeders before testing anything. Double-check row/column claims against the actual SQL rather than trusting prose summaries — this plan itself had 2 factual errors (admin role, JSON fields) caught only by red-team grepping the real file.
- **Risk**: `deleteAllInBatch()` skips JPA lifecycle callbacks/cascade — if any entity has `@PreRemove` logic or orphan-removal cascades relevant to seeded tables, batch delete could leave orphans. **Mitigation**: grep each seeded entity for `@PreRemove`/`orphanRemoval` before choosing `deleteAllInBatch()` vs `deleteAll()` per seeder.
- **Risk (corrected after red-team)**: forgetting to reset/seed `product_images`/`products` in the right order does **not** cause an FK violation — `V1__init_db.sql` has zero real FK constraints, so a mistake here is silent (dangling `product_id` references), not a crash. **Mitigation**: keep both inside `ProductSeeder` as a single unit to avoid ordering mistakes, and rely on the orphan-reference check (Architecture section) as the actual correctness signal, not "no exceptions thrown."
- **Risk**: hardcoding the admin password in `UserSeeder` source with no override, combined with non-dev mode seeding any empty DB unconditionally, could auto-provision a well-known `admin`/`admin123` credential into a real (even if currently unused) production DB on first boot. **Mitigation**: preserve the `@Value`-based override (see Architecture) and the startup warning log — this is a mandatory part of the seeder, not optional polish.
