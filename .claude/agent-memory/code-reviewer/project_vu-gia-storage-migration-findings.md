---
name: project-vu-gia-storage-migration-findings
description: Key verified bugs/gaps from reviewing the MinIO-to-local-filesystem storage migration (vu-gia-backend-api)
metadata:
  type: project
---

Reviewed 2026-07-18 (commit range: uncommitted working-tree diff on `main`, no plan doc available —
see [[project-storage-migration-plan-missing]]).

- **Real bug (H1):** `WebStorageConfig.addResourceHandlers()`
  (`src/main/java/vn/springboot/config/WebStorageConfig.java`) registers the static resource
  location via `Paths.get(root).toAbsolutePath().normalize().toUri().toString()`. `Path.toUri()`
  only appends a trailing `/` if the directory **already exists on disk** at call time (verified
  empirically with a standalone JDK test). If `app.storage.root` (default `./data`) doesn't exist
  yet at Spring Boot startup, the resulting resource location is missing its trailing slash, and per
  RFC 3986 §5.3 relative-URI resolution then silently drops the last path segment when resolving
  `products/x.jpg` against it — `/files/**` serving breaks until the app is restarted with the
  directory pre-existing. Masked in Docker (Dockerfile `mkdir -p /app/data` at image build), NOT
  masked on bare `./mvnw spring-boot:run` on a fresh clone (repo ships no `data/` dir, `.gitignore`
  excludes it, no `.gitkeep`). Fix: `Files.createDirectories(base)` before computing `toUri()`.

- **Open question, not a code bug (M1):** if this app ever ran with the old `MinioStorageService` in
  a real environment, existing DB rows on `@StorageUrl` fields hold absolute MinIO URLs, not
  relative paths. The new serializer correctly passes these through unchanged (so they still render
  as long as old MinIO stays up), but there's no backfill/migration path, and `delete()` silently
  no-ops on them. If MinIO gets decommissioned as part of the Docker Compose consolidation, those
  become permanently broken links with no code path to detect it. Needs an explicit product/ops
  decision, not a silent gap.

**Why worth remembering:** both required actually running/reasoning through JDK URI semantics and
tracing DB data lineage across the migration — not visible from a surface read of the diff.

**How to apply:** If this migration resurfaces (plan file located, or further work on
`app.storage.*`), check whether H1 was fixed and whether M1 was answered before treating either as
resolved.
