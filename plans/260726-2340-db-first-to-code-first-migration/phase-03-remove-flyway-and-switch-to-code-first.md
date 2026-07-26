---
phase: 3
title: "Remove Flyway and Switch to Code-First"
status: pending
priority: P1
effort: "S"
dependencies: [1, 2]
---

# Phase 3: Remove Flyway and Switch to Code-First

## Overview

With Phase 1 (entity fidelity) and Phase 2 (Java seeder, tested against the Flyway-managed schema) both verified, this phase removes Flyway entirely and flips `ddl-auto` to `update`. Order matters: only do this once Phase 2's seeder has been manually confirmed working — this phase deletes the SQL source data (`db/seed/V2__seed_db.sql`) that Phase 2 transcribed from, so there's no going back to "read the SQL again" after this point.

## Requirements

- Functional: app boots with zero Flyway dependency, `ddl-auto=update` creates/updates schema from entities alone, no `db/migration` or `db/seed` directories remain.
- Non-functional: no dangling references to Flyway anywhere in config, comments, or docs (docs handled in Phase 4).

## Related Code Files

- Modify: `pom.xml` — remove `flyway-core` and `flyway-mysql` dependencies
- Modify: `src/main/resources/application.yaml` — remove the `spring.flyway.*` block, change `spring.jpa.hibernate.ddl-auto` from `validate` to `update`
- Delete: `src/main/resources/application-development.yaml` (its only content, `spring.flyway.clean-disabled: false`, becomes moot — confirm no other dev-only override was added to this file before deleting; if any non-Flyway override exists, keep the file and just strip the Flyway line)
- Delete: `src/main/java/vn/springboot/config/FlywayDevCleanConfig.java`
- Delete: `src/main/resources/db/migration/` (entire directory, including `V1__init_db.sql`)
- Delete: `src/main/resources/db/seed/` (entire directory, including `V2__seed_db.sql`)
- Modify: `docker-compose.yml` — update the comment block referencing "Flyway tự clean+remigrate" (lines ~44-48) and the healthcheck `start_period` comment referencing "Flyway chạy V1->V5 gồm seed lớn" (lines ~106-108); re-evaluate whether `start_period: 90s` is still appropriate (Hibernate `ddl-auto=update` + Java seeder full reseed on dev boot may have different timing than Flyway's SQL batch — keep 90s unless testing in Phase 4 shows it needs adjustment)
- Modify: `.env` and `.env.example` — update the `APP_ENV` comment block (currently describes Flyway clean/remigrate behavior; must describe the new seeder reset/idempotent behavior instead)

## Implementation Steps

1. Remove `flyway-core` and `flyway-mysql` from `pom.xml`.
2. In `application.yaml`: delete the `spring.flyway` block entirely; change `hibernate.ddl-auto: validate` → `update`; update the adjacent Vietnamese comment ("Flyway là nguồn chân lý của schema...") to reflect entities as the new source of truth, and add a one-line note recording this as a deliberate, re-confirmed tradeoff: `ddl-auto=update` applies to every profile including production (no `application-production.yaml` override), which is a known Hibernate anti-pattern (never drops orphaned columns/tables on entity field removal, no migration diff to review) — accepted because the project has no real production data yet (re-confirmed 2026-07-27 after red-team flagged a public domain in `.env` as a possible signal otherwise).
3. Delete `application-development.yaml` (after confirming it has no other content worth keeping) — the `spring.profiles.active: ${APP_ENV:production}` mechanism in `application.yaml` still works without a per-profile yaml file; `@Profile("development")` conditional beans (the `SeedRunner` dev branch) don't need one either.
4. Delete `FlywayDevCleanConfig.java`.
5. **Commit Phase 1 and Phase 2 work first if not already committed, then perform this phase's deletions (`pom.xml`, `application.yaml`, `application-development.yaml`, `FlywayDevCleanConfig.java`, `db/migration/`, `db/seed/`) as their own dedicated commit** — added after red-team flagged that this plan has no rollback story otherwise: a single atomic "remove Flyway" commit is a real, one-command `git revert` away from undoing this phase if Phase 4 verification later finds a transcription bug that needs the deleted SQL as reference.
6. Delete `src/main/resources/db/migration/` and `src/main/resources/db/seed/` directories.
7. Update `docker-compose.yml` comments per Related Code Files above. Also add a short comment near the healthcheck noting the accepted risk (user-confirmed 2026-07-27): `SeedRunner` runs as a `CommandLineRunner` after Tomcat has already started, so `/actuator/health` can briefly report `UP` before seeding finishes — no readiness gate is being added for this; it's a documented limitation, not a gap to close here.
8. Update `.env` / `.env.example` `APP_ENV` comment block: explain that `development` now means "seeder truncates and re-seeds all data — including transactional tables like orders/cart_items — on every boot", not "Flyway clean-on-mismatch".
9. Run `./mvnw clean compile` to confirm no leftover Flyway imports/references anywhere (should be zero after steps 1-4).
10. Run `./mvnw spring-boot:run` against a **fresh, empty** database (drop the existing dev DB or point at a new schema) with `APP_ENV=development` — this is the first real test of pure code-first: Hibernate must create all 21 tables from entities alone, then the seeder populates them. Confirm boot succeeds and row counts match Phase 2's verification.

## Success Criteria

- [ ] `grep -ri flyway` across `pom.xml`, `src/`, `docker-compose*.yml`, `.env*` returns zero matches
- [ ] `ddl-auto=update` in `application.yaml`, no per-profile override needed
- [ ] Fresh empty DB + `APP_ENV=development` boots clean: schema auto-created, all 13 domains seeded
- [ ] `db/migration/` and `db/seed/` directories no longer exist

## Risk Assessment

- **Risk**: deleting `db/seed/V2__seed_db.sql` before Phase 2's seeder is fully verified means losing the only source-of-truth reference for fixing any transcription bugs found later. **Mitigation**: this phase is explicitly sequenced after Phase 2's manual verification step — do not start this phase until Phase 2's success criteria are all checked. **Additionally (added after red-team)**: perform the deletion as its own atomic commit (step 5) so it's a clean `git revert` away if a transcription bug surfaces in Phase 4 needing the original SQL back.
- **Risk**: `ddl-auto=update` on a schema that still has Flyway's `flyway_schema_history` table lying around (harmless but stale) — not a functional risk, just cosmetic; optionally note in the PR/commit that a fresh DB avoids this, existing dev DBs will just have an unused leftover table.
- **Risk (accepted, documented per red-team finding)**: `ddl-auto=update` applies globally (no per-profile override) — Hibernate never drops orphaned columns/tables when an entity field is removed, and there's no migration file to diff in code review going forward (Flyway's schema-change audit trail disappears with no replacement). Accepted as a deliberate tradeoff given no real production data exists yet; Phase 4 documents this limitation in `docs/system-architecture.md` rather than adding schema-diff tooling now.
- **Risk (accepted, documented per red-team finding)**: brief window on every boot where `/actuator/health` may report healthy before `SeedRunner` (a `CommandLineRunner`, runs after Tomcat starts) finishes seeding — a request landing in that window could see an empty catalog. Accepted without adding a custom `HealthIndicator`; documented in Phase 4.
