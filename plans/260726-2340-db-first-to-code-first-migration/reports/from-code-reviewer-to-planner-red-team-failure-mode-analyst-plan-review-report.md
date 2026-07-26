# Red Team Review: DB-First to Code-First Migration Plan (Failure Mode Analyst)

Reviewed: `plan.md`, `phase-01..04-*.md`. All findings verified against the live
backend at `C:\Users\minhlong\Desktop\projects\vu-gia-fullstack\vu-gia-backend-api`
(`src/main/resources/db/migration/V1__init_db.sql`, `V2__seed_db.sql`, entities,
`application.yaml`, `docker-compose.yml`, `.env`/`.env.example`).

## Finding 1: Plan's core safety assumption ("FK-violation crashes") is factually wrong — the schema has zero DB-level foreign keys

- **Severity:** Critical
- **Location:** Task framing + Phase 2, sections "Architecture" (SeedRunner FK-safe order) and "Reset order"; Phase 2 Risk Assessment
- **Flaw:** The plan repeatedly treats FK ordering mistakes as a *loud* failure mode ("a mistake in either ordering causes FK-violation crashes"). `V1__init_db.sql` declares **no `FOREIGN KEY`/`CONSTRAINT` clauses anywhere** — verified via `grep -n -i "foreign key\|references\|constraint" src/main/resources/db/migration/V1__init_db.sql` (zero matches across all 412 lines / 21 tables). Relationships exist only as plain `BIGINT` columns (e.g. `cart_items.user_id`, `cart_items.product_id`) referenced at the JPA layer via `@JoinColumn` (`CartItemEntity.java:39-44`) with no `foreignKey=...` attribute. MySQL will accept an insert into `product_images` or `orders` referencing a non-existent parent ID silently.
- **Failure scenario:** If `SeedRunner`'s hardcoded 12-seeder forward/reverse order (Phase 2, "Architecture") has a mistake — e.g. `productSeeder.seed()` runs before `productCategorySeeder.seed()`, or the reset order skips a step — there is **no DB error**. The insert just succeeds with a dangling `product_category_id` / `product_id` pointing at rows that don't exist yet (or, worse, at stale rows left over from an incomplete prior reset). Nothing in Phase 2's verification step 11 ("confirm no duplicate-key errors") or Phase 4's manual spot-checks is designed to catch this — a dangling FK produces no error and no visibly-wrong data unless someone opens the specific joined record and notices it's empty/wrong.
- **Evidence:** `grep -n -i "foreign key\|references\|constraint" src/main/resources/db/migration/V1__init_db.sql` → no output. `CartItemEntity.java:39-44` uses bare `@JoinColumn(name = "product_id", nullable = false)` with no `foreignKey` attribute.
- **Suggested fix:** Either (a) add real `FOREIGN KEY` constraints to the entities via `@JoinColumn(foreignKey = @ForeignKey(name=...))` as part of this migration (turning silent corruption into loud, catchable errors — arguably the actual right code-first fix), or (b) explicitly acknowledge in Phase 2 that ordering bugs are silent and add a real integrity check (e.g. a post-seed SQL/JPQL query asserting no orphaned `product_category_id`/`product_id`/`news_category_id` values) instead of relying on "no duplicate-key errors" as the correctness signal.

## Finding 2: No rollback story for the irreversible Phase 3 deletion; plan runs directly on `main` with no backup step

- **Severity:** Critical
- **Location:** Phase 3, Risk Assessment; `plan.md` frontmatter (`branch: "main"`)
- **Flaw:** `grep -in "backup\|rollback\|revert\|git tag" plan.md phase-*.md` returns zero hits outside of the frontmatter `branch:` field. Phase 3's own Risk Assessment states verbatim: "deleting `db/seed/V2__seed_db.sql` before Phase 2's seeder is fully verified means losing the only source-of-truth reference for fixing any transcription bugs found later" — and the "Mitigation" is purely procedural ("do not start this phase until Phase 2's success criteria are all checked"), not a technical safeguard. There is no git tag, no DB dump, no branch isolation instruction anywhere in the plan. `plan.md:6` sets `branch: "main"`, meaning this 4-phase migration — including deleting the only SQL source of the seed data and the schema-defining migration file — is scoped to run directly on `main`.
- **Failure scenario:** Phase 2's "manual verification" (a human eyeballing row counts and restart logs, Phase 2 step 11) passes, Phase 3 runs and deletes `db/migration/V1__init_db.sql` and `db/seed/V2__seed_db.sql` from the working tree. Phase 4 then finds a transcription bug that Finding 1 above shows would be silent (e.g. a wrong FK reference, or the JSON-fabrication issue in Finding 4 below) — the only way to recover the original values is `git log`/`git show` on a deleted file, which requires the deletion to have already been committed with correct history, and works only if nobody has amended/rebased since. There is no explicit instruction anywhere to keep the deletion as its own atomic, revertible commit, nor to dump the current dev DB before flipping `ddl-auto` to `update` (which, combined with Finding 5, means schema mistakes also cannot be diffed after the fact).
- **Evidence:** `grep -in "backup\|rollback\|revert\|git tag\|branch" plan.md phase-*.md` → only frontmatter `branch: "main"` matches; Phase 3 lines 14, 53 (quoted above).
- **Suggested fix:** Require a feature branch (not `main`) for this plan, a `git tag` or DB dump taken immediately before Phase 3's deletion step, and make the deletion its own isolated commit so `git revert` is a real option if Phase 4 verification fails.

## Finding 3: Phase 1's `ShippingMethodEntity.code` NOT NULL/UNIQUE change breaks the existing create-shipping-method endpoint at runtime, and no phase actually fixes it

- **Severity:** High
- **Location:** Phase 1, Implementation Step 1 and Risk Assessment (2nd bullet); Phase 2 (no corresponding fix)
- **Flaw:** Phase 1 adds `code` as `@Column(unique = true, length = 50, nullable = false)`. `ShippingMethodServiceImpl.create()` (`src/main/java/vn/springboot/service/impl/ShippingMethodServiceImpl.java:80-85`) builds a `ShippingMethodEntity` from `ShippingMethodCreateRequest` and never sets `code` — and `ShippingMethodCreateRequest.java` (`src/main/java/vn/springboot/dto/request/shipping/ShippingMethodCreateRequest.java`) has **no `code` field at all** (only `name`, `fee`, `sortOrder`, `isActive`). Phase 1's own Risk Assessment acknowledges this exact scenario ("adding `unique = true`... before the seeder exists could break current runtime if any code path already inserts shipping methods without a code") and proposes a mitigation ("make it nullable in this phase and tighten in Phase 2 once seed data is in place") — but no implementation step in Phase 1, Phase 2, or Phase 3 actually updates `ShippingMethodCreateRequest`/`ShippingMethodUpdateRequest`/`ShippingMethodServiceImpl` to accept/require `code`. The mitigation is described but never scheduled as work.
- **Failure scenario:** After Phase 1 ships, Hibernate's `ddl-auto=validate` boot check (Phase 1's stated gate) passes fine — `validate` only checks schema shape at boot, not runtime data. The break only surfaces the first time an admin calls `POST /shipping-methods` (or whatever the create endpoint is) without a `code`, and it fails with a `DataIntegrityViolationException` (NOT NULL constraint) that the plan's Phase 1 acceptance gate ("app still boots successfully") never exercises.
- **Evidence:** `ShippingMethodCreateRequest.java:16-31` (no `code` field); `ShippingMethodServiceImpl.java:80-85` (builder omits `code`); Phase 1 line 53 (self-identified risk, unaddressed by any implementation step).
- **Suggested fix:** Add an explicit implementation step (Phase 1 or 2) to add `code` to `ShippingMethodCreateRequest`/`UpdateRequest`, wire it through `ShippingMethodServiceImpl.create()`/`update()`, and add validation — before flipping `nullable = false`, not as an unscheduled "risk mitigation."

## Finding 4: Phase 2 instructs "literal transcription" of JSON columns that don't exist in the real seed data — invites fabricated content

- **Severity:** High
- **Location:** Phase 2, "Architecture" section, `ProductSeeder` paragraph
- **Flaw:** The plan states: "Simple JSON fields (`description`, `detail_sections`, `functions`, `combo_gallery`) can be Java text blocks transcribed literally from the SQL's `JSON_OBJECT()`/`JSON_ARRAY()` calls." The actual `INSERT INTO products (...)` column list in `V2__seed_db.sql` (line 229-248) is: `id, name, thumb, sku, type, price, compare_at_price, sold_count, is_featured, status, description, combo_products, slug, priority, product_category_id, seo_title, seo_description, created_at, updated_at`. **`detail_sections`, `functions`, and `combo_gallery` are absent from every single row's column list**, including product #12 (the combo, lines 594-630+), which only populates `description` and `combo_products`. These three columns are always `NULL` in the real seed data — there is no `JSON_OBJECT()`/`JSON_ARRAY()` source to "transcribe literally" for them.
- **Failure scenario:** An implementer (plan explicitly anticipates an AI coding agent doing this work) follows the instruction literally, invents plausible-looking JSON content for `detail_sections`/`functions`/`combo_gallery` to satisfy "transcribed literally... text blocks," and ships fabricated data that never existed in production — violating the plan's own "port seed data 1:1 (no reduced/curated dataset)" decision and the project's "no fake data" development rule, while looking correct in a code review (the JSON is well-formed, just invented).
- **Evidence:** `src/main/resources/db/seed/V2__seed_db.sql:229-248` (INSERT column list, `detail_sections`/`functions`/`combo_gallery` absent); lines 594-630 (product #12 combo row, same absence confirmed at the row level).
- **Suggested fix:** Correct Phase 2's architecture text to state these three columns are `NULL` for all 12 seeded products in the current dataset and must be left unset (not fabricated) — the 1:1 port is "leave null," not "transcribe."

## Finding 5: Global `ddl-auto=update` (single `application.yaml`, no per-profile override) silently no-ops on failed ALTER TABLE and never drops orphaned columns — permanently, for prod too

- **Severity:** High
- **Location:** Phase 3, Implementation Step 2; `plan.md` "Confirmed decisions"
- **Flaw:** `application.yaml` is the only Spring config file with a `ddl-auto` key (verified: `grep -n ddl-auto` finds exactly one hit, `application.yaml:21`; `application-development.yaml` only overrides `spring.flyway.clean-disabled`, and Phase 3 deletes it anyway). There is no `application-production.yaml`. Flipping `ddl-auto` to `update` therefore applies identically to whatever environment `APP_ENV` resolves to, including production (`docker-compose.yml:48` defaults `APP_ENV` to `production` when unset). Hibernate's `update` schema tool is well known to (a) never drop columns/tables when an entity field is removed, and (b) on MySQL, silently log-and-continue rather than fail the boot when an `ALTER TABLE ADD CONSTRAINT`/`ADD INDEX` fails (e.g., against a table that already has conflicting data). Neither behavior produces a boot failure or any signal in the plan's manual verification steps.
- **Failure scenario:** Any future entity field removal (inevitable in ongoing development, not hypothetical) leaves the physical column in place forever in every environment with zero warning — no failed build, no failed boot, no log a developer would notice by default. Combined with Finding 2 (no DB dump/backup step), there's no artifact to diff against later to even discover the drift.
- **Evidence:** `application.yaml:17-21` (single `ddl-auto` key, `validate` today); no `application-production.yaml` found (`find src/main/resources -iname "application*.yaml"` → only `application.yaml` and `application-development.yaml`, the latter deleted by Phase 3 step 3); `docker-compose.yml:48` (`APP_ENV: ${APP_ENV:-production}`).
- **Suggested fix:** At minimum, add a process step (post-migration) requiring a schema-diff check (e.g. `SHOW CREATE TABLE` snapshot comparison, or a lightweight schema-validation test) whenever an entity field is removed, since `ddl-auto=update` will never surface this on its own. This is a known, accepted trade-off per the "Confirmed decisions" — flagged here as an unmitigated operational gap, not to relitigate the decision itself.

## Finding 6: Phase 2's only correctness signal ("no duplicate-key errors") cannot detect the ordering bugs it's meant to catch

- **Severity:** Medium
- **Location:** Phase 2, Implementation Step 11; Phase 4, Implementation Step 1
- **Flaw:** Phase 2's manual verification step says: "restart, confirm full reset+reseed happened again cleanly (no duplicate-key errors)." Given Finding 1 (no FK constraints in the schema), a wrong seeder order does not raise a duplicate-key error — duplicate-key errors only fire on `UNIQUE` columns (`sku`, `slug`, `username`, etc.), which are unrelated to FK sequencing. A reset/reseed ordering bug (e.g., `productSeeder.reset()` running after `productCategorySeeder.reset()` instead of before) produces either silently orphaned rows or, if IDs are reused via auto-increment reset behavior, silently *wrong* references — neither of which throws.
- **Failure scenario:** The stated acceptance bar for "dev-mode restart fully wipes and re-seeds without errors" (Phase 2 success criteria) can be met while the actual FK-order contract used by `SeedRunner` is broken, because the verification method (watching for exceptions) is structurally incapable of detecting the failure mode it's supposed to guard against.
- **Evidence:** Phase 2 line 85 ("no duplicate-key errors" as the pass signal); cross-referenced with Finding 1's `grep` evidence that no FK constraints exist to raise errors on misorder.
- **Suggested fix:** Add an explicit orphan-reference check (e.g., a JPQL/SQL assertion query per domain: "no `product_images.product_id` without a matching `products.id`") to the verification step, not just "no exceptions thrown."

## Finding 7: Deleting `DataInitializer.java` leaves orphaned `app.init.*` config in `application.yaml` — no phase cleans it up, and it's also where the "losing" admin email actually lives

- **Severity:** Medium
- **Location:** Phase 2, Implementation Step 10 ("Delete `DataInitializer.java`"); `plan.md` "Confirmed decisions" (4th bullet)
- **Flaw:** The plan attributes the `admin@gmail.com` default to "`DataInitializer.java`'s losing `admin@gmail.com` default" (`plan.md:26`). In fact `DataInitializer.java:31` defaults to `admin@springboot.vn` (`@Value("${app.init.admin-email:admin@springboot.vn}")`); the actual `admin@gmail.com` value that wins today comes from `application.yaml:99-104`'s `app.init.admin-email: admin@gmail.com` property block. No phase's "Related Code Files"/"Implementation Steps" mentions removing this `app.init:` block (`enabled`, `admin-username`, `admin-email`, `admin-password`) from `application.yaml` once the class that reads it is deleted.
- **Failure scenario:** Not destructive, but leaves dead, misleading configuration in `application.yaml` referencing a deleted feature — a maintainer six months from now sees `app.init.admin-email: admin@gmail.com` in prod config and reasonably assumes it still does something, when it's inert. Minor drift risk given the plan's own stated goal of eliminating stale references (Phase 4's exhaustive doc-grep goal doesn't cover `application.yaml` properties).
- **Evidence:** `src/main/java/vn/springboot/config/DataInitializer.java:31` (real code default is `admin@springboot.vn`, not `admin@gmail.com`); `src/main/resources/application.yaml:99-104` (`app.init:` block with `admin-email: admin@gmail.com`, the actual source of the value the plan calls out).
- **Suggested fix:** Add `app.init.*` block removal from `application.yaml` to Phase 2 or Phase 3's file list, and correct the plan's factual attribution (config property, not Java default) for anyone using this plan as a reference later.

## Finding 8: "No real production data yet" is asserted as settled fact but never verified against the actual deployment before irreversible steps run

- **Severity:** Medium
- **Location:** `plan.md`, "Confirmed decisions" (1st bullet); Phase 3 (no verification step)
- **Flaw:** The plan states as a "Confirmed decision (user, 2026-07-26)": "project has no real production data yet, optimize for clean code over migration history." This is presented as the justification for global `ddl-auto=update` and destructive Flyway removal. However, the repo shows live-deployment signals: `docker-compose.yml:48` defaults `APP_ENV` to `production`, and the local `.env` references `APP_STORAGE_PUBLIC_URL=https://api-vugia.minhlong03.site` — a real, non-localhost public domain, suggesting an actual deployed instance exists. No phase includes a step to confirm the real production database (as opposed to a local dev DB) is in fact empty/disposable before Phase 3 executes.
- **Failure scenario:** If the assumption is wrong (a production DB was seeded from an earlier deploy and has since accumulated real orders/users), Phase 3's `ddl-auto=update` flip plus the loss of Flyway's version history removes the team's only structured mechanism for auditing what schema/data state production is actually in — with no rollback path per Finding 2.
- **Evidence:** `docker-compose.yml:48` (`APP_ENV: ${APP_ENV:-production}`); `.env:APP_STORAGE_PUBLIC_URL=https://api-vugia.minhlong03.site` (public domain, not localhost).
- **Suggested fix:** Not disputing the user's decision — flagging that no phase step operationally confirms the premise against the real target environment before Phase 3 ships. Add a one-line verification step ("confirm target prod DB row counts are zero/seed-only before this phase runs") to Phase 3.

---

## Fact-Check Summary (sampled claims)

| # | Claim | Result |
|---|-------|--------|
| 1 | `ddl-auto: validate` currently set, single location | VERIFIED (`application.yaml:21`) |
| 2 | Flyway deps in `pom.xml` (`flyway-core`, `flyway-mysql`) | VERIFIED (`pom.xml:122-127`) |
| 3 | `FlywayDevCleanConfig.java` exists, `@Profile("development")` clean-on-mismatch | VERIFIED (`FlywayDevCleanConfig.java:18-32`) |
| 4 | `ShippingMethodEntity` missing `code`/`description`/`estimatedDelivery` | VERIFIED (`ShippingMethodEntity.java:24-41`, absent) |
| 5 | `V1` `shipping_methods` has `code VARCHAR(50) NOT NULL UNIQUE` etc. | VERIFIED (`V1__init_db.sql:340-354`) |
| 6 | `UserEntity`/`CartItemEntity` lack the composite unique constraints at JPA level | VERIFIED (no `@Table(uniqueConstraints=...)` on either); DB already enforces both via V1 (`V1__init_db.sql:29`, `:186`) — gap is metadata-only, not a live functional gap today |
| 7 | `OrderEntity` already has `uidx_orders_user_idempotency` | VERIFIED (`OrderEntity.java:35-36`) |
| 8 | V2 seed file is 3160 lines | VERIFIED (`wc -l` → 3160) |
| 9 | Admin account `admin@gomvugia.vn`/`admin123` in V2 seed | VERIFIED (`V2__seed_db.sql:45,64,69`) |
| 10 | `DataInitializer.java`'s default admin email is `admin@gmail.com` | **FAILED** — actual code default is `admin@springboot.vn` (`DataInitializer.java:31`); `admin@gmail.com` comes from `application.yaml:104` property override |
| 11 | Product #12 combo references ids 9,10,11 | VERIFIED (`V2__seed_db.sql:593`) |
| 12 | `detail_sections`/`functions`/`combo_gallery` populated in seed, transcribable | **FAILED** — absent from INSERT column list entirely (`V2__seed_db.sql:229-248`) |
| 13 | `.env`/`.env.example` currently describe Flyway clean/remigrate behavior | VERIFIED (`.env:4-6`, `.env.example` same block) |
| 14 | README/docs Flyway references at cited locations | VERIFIED (`README.md:4,21,58-59,139,158,215`) |

## Unresolved Questions

- Is there an actual deployed production instance with real data behind `api-vugia.minhlong03.site`, or is that domain currently unused/staging-only? This directly affects whether Finding 8 is a real risk or a non-issue.
- Does the team intend to add real `FOREIGN KEY` constraints as part of "code-first," or is the DB intentionally FK-constraint-free by design (e.g., for soft-delete flexibility)? This changes the severity framing of Finding 1 from "plan is wrong about the failure mode" to "plan should also fix the underlying gap."
