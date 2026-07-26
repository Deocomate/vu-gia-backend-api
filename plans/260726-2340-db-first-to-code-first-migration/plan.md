---
title: "DB-first to code-first migration (Hibernate ddl-auto + Java seeders)"
description: ""
status: done
priority: P2
branch: "main"
tags: []
blockedBy: []
blocks: []
created: "2026-07-26T16:43:44.698Z"
createdBy: "ck:plan"
source: skill
---

# DB-first to code-first migration (Hibernate ddl-auto + Java seeders)

## Overview

Migrate `vu-gia-backend-api` from DB-first (Flyway `V1__init_db.sql` schema + `V2__seed_db.sql` 3160-line SQL seed) to fully code-first: JPA entities become the sole schema source of truth (`spring.jpa.hibernate.ddl-auto=update` in every profile, Flyway removed entirely — dependency, config, migration/seed SQL files), and a Java-based seeder framework replaces the SQL seed with 1:1 ported data. When `APP_ENV=development`, the seeder truncates and re-inserts every seeded table on **every** server startup (DB treated as disposable); in any other profile it seeds only tables that are empty (idempotent bootstrap, never touches existing data).

**Confirmed decisions (user, 2026-07-26):**
- `ddl-auto=update` everywhere (dev + prod) — project has no real production data yet, optimize for clean code over migration history. **Re-confirmed after red-team** (2026-07-27): a public domain appears in `.env` (`APP_STORAGE_PUBLIC_URL=https://api-vugia.minhlong03.site`), which a reviewer flagged as possible evidence of a live deployment; user confirmed it is unused/staging with no real data, so this decision stands unchanged.
- Delete Flyway entirely: `flyway-core`/`flyway-mysql` deps, `db/migration/`, `db/seed/`, `FlywayDevCleanConfig.java`.
- Port seed data 1:1 (no reduced/curated dataset) — all 13 seeded domains, full row counts. Note: `products.detail_sections`, `functions`, and `combo_gallery` are always `NULL` in the actual seed data (absent from the real INSERT column list) — "1:1" means leaving them unset, not fabricating content.
- Add the 3 currently-unmapped `ShippingMethodEntity` columns (`code`, `description`, `estimatedDelivery`) and port their real seed content, rather than dropping them. This requires wiring `code` through the create/update DTOs and service too (see Phase 1) — the entity change alone would break the live shipping-method create endpoint.
- Canonical admin account is `admin` / `admin@gomvugia.vn` / `admin123`, **`role=ADMIN`** (matches what's actually seeded today in `V2__seed_db.sql:71` — not `SUPERADMIN`, corrected after red-team caught a drafting error that would have granted the seeded admin new privileges it doesn't have today). Three stale admin identities exist pre-migration and all three collapse into this one: the SQL seed (`admin@gomvugia.vn`, canonical), `DataInitializer.java`'s actual Java default (`admin@springboot.vn`, dead code — not `admin@gmail.com` as originally miswritten here), and `application.yaml`'s `app.init.admin-email` property (currently `admin@gmail.com`, which is what README's stale doc actually reflects). Retire `DataInitializer.java`, correct `application.yaml`'s `app.init.*` defaults to match the canonical identity, and fix README.
- Reproduce V1's ~40 performance indexes via `@Table(indexes = ...)` for `products`, `orders`, `coupons`, `news` (the hot list/filter endpoints) — skip the rest per YAGNI given current DB size.
- **(Added after red-team, user confirmed)** Dev-mode reset extends beyond the 13 seeded catalog domains to also clear the 6 non-seeded tables that reference them (`orders`, `order_items`, `cart_items`, `payment_transactions`, `refresh_tokens`, `contact_requests`) — matches the original "reseed toàn bộ" (reset everything) intent literally: the dev DB is fully disposable on every restart, not just its catalog data.
- **(Added after red-team, user confirmed)** Reset uses plain JPA `deleteAll()`/`deleteAllInBatch()`, not `TRUNCATE` — this means MySQL `AUTO_INCREMENT` counters are **not** reset between dev restarts (JPA `DELETE` doesn't reset them, only `TRUNCATE` does). IDs drift upward on every restart instead of always starting at 1. Accepted as-is; the "byte-for-byte equivalent" acceptance criterion below is scoped to row content/structure, not IDs.
- **(Added after red-team, user confirmed)** The brief window where `/actuator/health` may report `UP` before `SeedRunner` (a `CommandLineRunner`, which runs after Tomcat has already started) finishes seeding is an accepted risk, not something this plan adds code to fix — noted in Phase 3/4 as a documented limitation.

**Root architectural fact surfaced by red-team:** `V1__init_db.sql` declares **zero real `FOREIGN KEY`/`CONSTRAINT` clauses** across all 21 tables — relationships exist only as plain columns wired via JPA `@JoinColumn` with no `foreignKey` attribute. This means the database itself will never reject a wrong-order seed/reset operation with a loud error; a sequencing mistake produces silent orphaned or misattributed rows instead. Phase 2's verification strategy accounts for this explicitly (an orphan-reference check, not just "no exceptions thrown").

**Research basis:** `plans/reports/fork-260726-2335-code-first-migration-entity-seed-report.md` (full entity-vs-schema gap analysis + seed data catalog with FK order and hardcoded cross-references). Read it before starting Phase 1 or 2 — this plan references its findings by section but doesn't duplicate the row-by-row detail. **Also read the three red-team reports** in `plans/260726-2340-db-first-to-code-first-migration/reports/` (`from-code-reviewer-to-planner-red-team-{security-adversary,failure-mode-analyst,assumption-destroyer}-plan-review-report.md`) — several corrections above (role, JSON fields, admin identity) came directly from evidence in those reports.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Entity Schema Fidelity Fixes](./phase-01-entity-schema-fidelity-fixes.md) | Done |
| 2 | [Java Seeder Framework and Data Port](./phase-02-java-seeder-framework-and-data-port.md) | Done |
| 3 | [Remove Flyway and Switch to Code-First](./phase-03-remove-flyway-and-switch-to-code-first.md) | Done |
| 4 | [Verification and Documentation](./phase-04-verification-and-documentation.md) | Done |

## Dependencies

None — no other unfinished plan in `plans/` touches the backend schema, seeding, or Flyway config.

## Acceptance Criteria

- App boots with `spring.jpa.hibernate.ddl-auto=update` and zero Flyway references anywhere in the codebase.
- `APP_ENV=development`: every restart against the same DB fully wipes and re-seeds all 13 catalog domains, and also clears the 6 non-seeded transactional tables (`orders`, `order_items`, `cart_items`, `payment_transactions`, `refresh_tokens`, `contact_requests`); seeded data is equivalent in row content/structure to today's `V2__seed_db.sql` (IDs are explicitly excluded from this equivalence — see "Confirmed decisions" on `AUTO_INCREMENT`).
- Any other `APP_ENV`: first boot on an empty DB seeds once; subsequent restarts never touch existing rows even if they were manually edited.
- No data correctness regression: `users(provider, provider_id)` and `cart_items(user_id, product_id)` uniqueness enforced at the DB level again; `ShippingMethodEntity` exposes `code`/`description`/`estimatedDelivery` **and** they're reachable through the create/update API (not just the entity); the products combo (id 12 referencing 9-11) and every other cross-domain reference (`product_category_id`, `news_category_id`, `product_id` on images) resolves to real generated IDs, not hardcoded ones — verified by an explicit orphan-reference check, not just absence of exceptions (there are no DB-level FK constraints to catch this otherwise).
- Seeded admin account role is `ADMIN` (matching the real source data), not `SUPERADMIN`.
- README.md and the 4 affected `docs/*.md` files no longer describe Flyway/DB-first behavior.

## Red Team Review

### Session — 2026-07-27
**Findings:** 20 raw findings across 3 reviewers (Security Adversary, Failure Mode Analyst, Assumption Destroyer), deduplicated to 14 unique findings (14 accepted, 0 rejected — all passed the evidence filter with file:line citations, and adjudication found every one materially correct).
**Severity breakdown:** 5 Critical, 6 High, 3 Medium
**Reports:** `reports/from-code-reviewer-to-planner-red-team-security-adversary-plan-review-report.md`, `reports/from-code-reviewer-to-planner-red-team-failure-mode-analyst-plan-review-report.md`, `reports/from-code-reviewer-to-planner-red-team-assumption-destroyer-plan-review-report.md`

| # | Finding | Severity | Disposition | Applied To |
|---|---------|----------|-------------|------------|
| 1 | Seed admin role spec'd as SUPERADMIN; real data is ADMIN — privilege escalation | Critical | Accept | Phase 2, plan.md |
| 2 | Hardcoded admin password with no env-var override, auto-provisions on first empty-DB boot (incl. prod) | Critical | Accept | Phase 2 |
| 3 | Dev-mode reset only covers 13 seeded tables; 6 tables with references into them are untouched, silently orphaning on restart (no real FK to prevent it) | Critical | Accept (user chose to extend reset scope) | Phase 2, plan.md |
| 4 | No DB-level FK constraints anywhere — seeder ordering mistakes are silent data corruption, not loud crashes; "no duplicate-key errors" can't detect this | Critical | Accept | Phase 2, plan.md |
| 5 | No rollback/backup/atomic-commit step before Phase 3's irreversible SQL-file deletion | Critical | Accept | Phase 3 |
| 6 | `ShippingMethodEntity.code` (`NOT NULL`) has no DTO/service wiring anywhere in the plan — breaks the live create endpoint | High | Accept | Phase 1 |
| 7 | Phase 2 instructs transcribing JSON for `detail_sections`/`functions`/`combo_gallery`, which don't exist in the real seed data (always NULL) — invites fabricated data | High | Accept | Phase 2, plan.md |
| 8 | `ddl-auto=update` in production (Hibernate anti-pattern) with no compensating control | High | Accept (documented as a re-confirmed accepted risk, not reversed) | Phase 3 |
| 9 | "No real prod data" premise unverified against a real public domain found in `.env` | High | Accept (user confirmed premise still holds) | plan.md |
| 10 | Flyway's schema-change audit trail disappears with no replacement | Medium | Accept (documented limitation) | Phase 4 docs |
| 11 | Plan misattributed `DataInitializer`'s default email, and deleting it leaves orphaned `app.init.*` config | Medium | Accept | Phase 2, plan.md |
| 12 | `deleteAllInBatch()`/`deleteAll()` never resets `AUTO_INCREMENT` — IDs drift upward forever | High | Accept (user chose to accept drift over adding a reset query) | plan.md |
| 13 | `/actuator/health` has no readiness gate tied to seed completion — brief empty-catalog window on boot | High | Accept (user chose to document as accepted risk, no new code) | Phase 3/4 docs |
| 14 | No ID-resolution guidance for `product_category_id`, unlike the explicit treatment given to `news_category_id` | Medium | Accept | Phase 2 |

### Whole-Plan Consistency Sweep
- Files reread: `plan.md`, `phase-01-entity-schema-fidelity-fixes.md`, `phase-02-java-seeder-framework-and-data-port.md`, `phase-03-remove-flyway-and-switch-to-code-first.md`, `phase-04-verification-and-documentation.md`
- Decision deltas checked: 14 (role ADMIN not SUPERADMIN; admin identity attribution corrected to 3 distinct pre-migration identities; JSON fields `detail_sections`/`functions`/`combo_gallery` always null; extended dev reset scope to 6 transactional tables; AUTO_INCREMENT drift accepted; healthcheck risk accepted; ShippingMethodEntity DTO/service wiring scheduled; orphan-reference check added to verification; product_category_id resolution guidance added; admin credential env-var override restored; app.init.* config corrected not orphaned; Flyway audit-trail loss documented; ddl-auto=update prod risk documented; rollback/atomic-commit guidance added to Phase 3)
- Reconciled stale references: all of the above propagated into Phase 1/2/3/4 (see phase files) and the Acceptance Criteria section above
- Unresolved contradictions: 0

## Validation Log

### Session 1 — 2026-07-27
**Trigger:** User requested `/ck:plan validate` before implementation, after the red-team pass above.
**Questions asked:** 4 (below min of the configured 3-8 range is not applicable here — 4 falls within range)

#### Verification Results
Skipped per validate-workflow guard: `## Red Team Review` above already contains verification evidence (Fact Checker/Contract Verifier findings with file:line citations from the red-team session), and no `[UNVERIFIED]` tags exist anywhere in the plan (checked via grep, zero matches).

#### Questions & Answers

1. **[Scope]** Phase 2 is the largest, riskiest phase (12 seeders, ~3000 lines of JSON hand-transcribed from SQL into Java text blocks). Should it be broken into smaller commit/verify steps instead of one large phase committed and tested at the end?
   - Options: Commit + spot-check after every seeder (Recommended) | Implement all 12 seeders then test/commit once
   - **Answer:** Commit + spot-check after every seeder
   - **Rationale:** Isolates a bad transcription (e.g., in the 1509-line `NewsSeeder`) to one revertible commit instead of the entire Phase 2 diff.

2. **[Risk]** This plan does global schema-strategy surgery (removes Flyway, flips `ddl-auto`). The existing repo's commit history shows work happens directly on `main`. Should this plan use a dedicated feature branch instead?
   - Options: Stay on main (Recommended) | Use a feature branch for the whole plan
   - **Answer:** Stay on main
   - **Rationale:** Matches the project's existing solo-dev workflow; the atomic-commit + `git revert` safety net already added to Phase 3 (red-team Finding 5) is judged sufficient at this project's scale.

3. **[Tradeoff]** Phase 4's verification is entirely manual (restart server, click Swagger, read logs) and this repo has zero existing automated tests (`src/test/resources` doesn't exist). Should an automated integration test be added for the seeder/orphan-check, or is manual verification acceptable?
   - Options: No, keep manual verification as-is (Recommended) | Yes, add a Spring Boot integration test for seed + orphan-check
   - **Answer:** Yes, add a Spring Boot integration test for seed + orphan-check (user overrode the recommendation)
   - **Rationale:** Regression safety for future seeder edits outweighs the one-time cost of writing this repo's first test. Added as `SeedRunnerIntegrationTest` in Phase 2 (co-located with the code it tests), re-run as part of Phase 4's final gate.

4. **[Architecture]** The `OrphanReferenceChecker` (added during red-team) didn't specify whether it runs only in dev or on every boot in every profile. Which scope?
   - Options: Every boot, every profile (Recommended) | Dev-mode only
   - **Answer:** Every boot, every profile
   - **Rationale:** Query cost is negligible (a handful of `COUNT`s) and there's no DB-level FK safety net in any environment, so the check is equally valuable outside dev.

#### Confirmed Decisions
- Phase 2 seeders: one commit per seeder, spot-checked before moving to the next — not one final commit.
- Branch strategy: commit directly to `main`, no dedicated feature branch — atomic-commit safety net (already in Phase 3) is sufficient.
- Testing: add `SeedRunnerIntegrationTest` (this repo's first automated test) asserting per-domain row counts and zero orphans; automated but does not replace the manual restart-with-real-usage scenario.
- `OrphanReferenceChecker` scope: runs after every seed completion, in every profile (not dev-gated).

#### Action Items
- [x] Phase 2: made per-seeder commit discipline explicit in Implementation Steps
- [x] Phase 2: added `OrphanReferenceChecker` as its own bean (not inlined) and `SeedRunnerIntegrationTest` as new Related Code Files + Implementation Step + Success Criteria
- [x] Phase 4: added a step to run `./mvnw test` as part of the final verification gate

#### Impact on Phases
- Phase 2: Implementation Steps renumbered (14 → 15 steps) to insert the integration test as step 14 before final manual verification (now step 15); Related Code Files gained `OrphanReferenceChecker.java` and `SeedRunnerIntegrationTest.java`; Success Criteria gained 2 new checkboxes (test passes, per-seeder commits).
- Phase 4: Implementation Steps gained a new step 8 (`./mvnw test`), renumbering the final grep-check to step 9.
- Phase 1, Phase 3: no changes from this validation session.

### Whole-Plan Consistency Sweep
- Files reread: `plan.md`, `phase-01-entity-schema-fidelity-fixes.md`, `phase-02-java-seeder-framework-and-data-port.md`, `phase-03-remove-flyway-and-switch-to-code-first.md`, `phase-04-verification-and-documentation.md`
- Decision deltas checked: 4 (per-seeder commits, main-branch confirmed, integration test added, orphan-checker scope confirmed as every-profile)
- Reconciled stale references: renamed "orphan-reference check" to `OrphanReferenceChecker` consistently in Phase 2 and Phase 4 (was implied inline logic in Phase 2, now an explicit bean); Phase 2 and Phase 4 implementation step numbering re-verified after insertions
- Unresolved contradictions: 0
