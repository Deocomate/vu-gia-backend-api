# MinIO → Local Filesystem Migration — Review Report

## Scope note (blocking prerequisite)

The referenced plan (`plans/260718-0909-migrate-minio-to-local-filesystem/plan.md` + phase files)
**does not exist anywhere in this working tree or its git history** — `plans/` is not a tracked or
untracked directory at all (`git log --all -- plans/` empty, filesystem search empty). I could not
read the plan's acceptance criteria, phase Success Criteria checklists, red-team pass, or validation
pass as instructed. This review is therefore based entirely on reading the actual diff/code
(`git status`/`git diff` against `main`) and independent verification — not a cross-check against
the plan document. Treat item (a) of the request as **unresolved** until the plan file is located or
re-supplied.

## Overall Assessment

Implementation is solid overall: extension whitelist derived from content-type (not filename) is
correctly enforced, path-traversal is defended via `normalize()` + `startsWith(base)` on both
upload and delete, the `@StorageUrl` round-trip logic is correct for the documented cases, and the
21/22 DTO surface was cross-checked field-by-field against entities — no missed image-URL field
found. One concrete, verifiable bug found in the static resource handler that will break `/files/**`
serving on any non-Docker first boot. One real production-readiness gap (pre-existing MinIO data not
backfilled) that needs an explicit decision, not silently left implicit.

## Critical Issues

None (no auth bypass, no data leak, no secret exposure found).

## High Priority

**H1 — `/files/**` static resource serving breaks if `app.storage.root` doesn't exist at Spring Boot startup**
`src/main/java/vn/springboot/config/WebStorageConfig.java:48-52`

```java
Path base = Paths.get(properties.getRoot()).toAbsolutePath().normalize();
registry.addResourceHandler(resourcePattern())
        .addResourceLocations(base.toUri().toString())
```

`java.nio.file.Path.toUri()` only appends a trailing `/` when the path **already exists on disk as
a directory at the moment `toUri()` is called** — verified empirically:
```
Paths.get("./nonexistent-dir").toAbsolutePath().normalize().toUri()
  -> file:///.../nonexistent-dir            (NO trailing slash)
Paths.get("./existing-dir").toAbsolutePath().normalize().toUri()
  -> file:///.../existing-dir/              (trailing slash)
```
Per RFC 3986 §5.3, resolving a relative reference (`products/x.jpg`) against a base URI **without**
a trailing slash treats the last segment as a "file" and strips it — so relative resolution turns
`file:///.../data` + `products/x.jpg` into `file:///.../products/x.jpg` (silently dropping the
`data` segment), not `file:///.../data/products/x.jpg`. This is the standard base-URI trailing-slash
pitfall, and Spring's resource-location machinery does exactly this URI resolution.

This location string is computed **once**, at `addResourceHandlers()` config time (Spring context
startup), and is not recomputed later. `LocalFileStorageService.uploadImage()` only calls
`Files.createDirectories(...)` lazily on the *first upload after the app is already running*, so it
cannot fix an already-registered malformed location.

- In Docker: masked, because `Dockerfile:47` does `mkdir -p /app/data` at image build time, so the
  directory always exists before the JVM starts. Not exercised in that path.
- On a fresh local clone running `./mvnw spring-boot:run` (the documented dev command per
  `CLAUDE.md` §6) with the default `app.storage.root=./data`: this repo has **no `data/` directory**
  checked in (confirmed — `.gitignore` now excludes `data/`, and no `.gitkeep` was added), so the
  first local run after this migration will have `/files/**` silently misresolve (typically 404) for
  every uploaded file until the app is stopped, a file is uploaded via a channel that pre-creates the
  dir, and the app is restarted.

Fix: `Files.createDirectories(base)` in `WebStorageConfig.addResourceHandlers()` before computing
`base.toUri()` (mirrors what `LocalFileStorageService` already does), or ensure the location string
always has a trailing slash (`base.toUri().toString()` + `"/"` if missing) regardless of directory
existence.

## Medium Priority

**M1 — Pre-existing MinIO image URLs in the DB are not backfilled; open question, not addressed in code**
No file/line — this is a data-migration gap, not a code bug. If this app has ever run against the
old `MinioStorageService` in a real environment, existing rows in `banner`, `news`, `page`,
`product`, `product_image`, `gallery_image`, `user.avatar` etc. hold **absolute MinIO URLs**
(e.g. `https://minio-host:9000/bucket/...`), not the new relative-path format. `StorageUrlSerializer`
correctly passes these through unchanged (they don't match `url-prefix`), so they keep rendering as
long as the old MinIO instance stays reachable — but:
- `LocalFileStorageService.delete()` silently no-ops on them (by design, per the "not managed by
  this storage" comment) — so old images can never be cleaned up through this app once MinIO is gone.
- The Docker Compose consolidation (per the task description) drops MinIO entirely from the stack.
  If/when the old MinIO instance is decommissioned, every pre-migration image reference becomes a
  permanently broken link with no code path to detect or fix it.
No backfill script, no `MinioStorageService` migration/copy utility, and no `docs/FILE_STORAGE_API.md`
callout for this was found. If this is a fresh app with no production data yet, this is a non-issue —
otherwise it needs an explicit accepted decision (documented backfill runbook, or accepted data loss).
I could not verify from the (missing) plan whether this was already decided as out of scope.

## Low Priority / Informational

**L1 — Reflection-based field injection in `StorageUrlSerializer`/`StorageUrlDeserializer` (task item d)**
`src/main/java/vn/springboot/common/storage/StorageUrlSerializer.java:18-19`,
`StorageUrlDeserializer.java:20-21`
The `@Autowired private StorageProperties properties` field relies on Spring Boot's
`JacksonAutoConfiguration` wiring a `SpringHandlerInstantiator` into the auto-configured
`Jackson2ObjectMapperBuilder`-built `ObjectMapper`. Verified this assumption holds for this codebase:
no custom `@Bean ObjectMapper`/`Jackson2ObjectMapperBuilder` exists anywhere in `src/main/java`
(grepped) that would bypass the auto-configured builder — the only other `ObjectMapper` usages
(`PaymentWebhookController`, `SecurityResponseWriter`, `JwtAuthenticationEntryPoint`,
`CustomAccessDeniedHandler`) are all constructor-injected, i.e. they consume the same
autoconfigured, Spring-instantiated bean rather than constructing their own. So the field-injection
trick is sound for the app's actual live wiring, not just in theory.
Trade-off: if the `properties` field is ever renamed without care, or the app later adds a custom
`ObjectMapper` bean that isn't built through `Jackson2ObjectMapperBuilder.applicationContext(...)`,
this fails as an **NPE at serialize/deserialize time** (not at startup) — a silent, hard-to-diagnose
failure mode. Acceptable given current code, but worth a one-line comment noting the dependency (the
class Javadoc doesn't currently state this precondition explicitly, only implies it via "annotation
gets Spring bean autowiring").

**L2 — Unit tests for the (de)serializers bypass the real wiring path (task item d, continued)**
`src/test/java/vn/springboot/common/storage/StorageUrlSerializerTest.java`,
`StorageUrlDeserializerTest.java` (not opened, same pattern per task description) construct the
serializer directly and set the `properties` field via reflection — these do **not** exercise
`SpringHandlerInstantiator` at all, so on their own they would not catch an L1 regression.
However, `MediaControllerTest` (`src/test/java/vn/springboot/controller/MediaControllerTest.java:60-96`)
**is** a real `@WebMvcTest` that serializes `UploadResponse` (annotated `@StorageUrl url`) through the
actual Spring-Boot-autoconfigured `MappingJackson2HttpMessageConverter` inside `MockMvc`, and asserts
the absolute-URL JSON output (`http://localhost:8080/files/products/a.jpg`). That test **does**
prove the Spring wiring path end-to-end for at least one annotated field. Net: the wiring risk in L1
is adequately covered by `MediaControllerTest`, even though the two dedicated serializer/deserializer
unit tests are narrower "logic-only" tests. Not a phantom-test concern — just worth knowing the real
proof-of-wiring lives in the controller test, not the unit tests.

**L3 — `docker-compose.yml` reuses `MYSQL_ROOT_PASSWORD` as the app's DB password**
`docker-compose.yml:35` — `DB_PASSWORD: ${MYSQL_ROOT_PASSWORD}`, app connects as `root`
(`DB_USERNAME:-root`). No dedicated app DB user/least-privilege account. Pre-existing pattern
concern, not introduced by file-storage migration itself, but touched in this diff (compose file was
rewritten wholesale) — flagging since the file was fully rewritten, not just patched. No hardcoded
secret found; `MYSQL_ROOT_PASSWORD` and `APP_JWT_SECRET` etc. have no insecure defaults (required via
`${VAR}` with no `:-` fallback), so compose will fail closed rather than silently run with a weak
password. `.env.example` contains only placeholder/blank values — clean.

**L4 — Comment/doc claim not backed by actual seed data**
`LocalFileStorageService.java:99` / `StorageUrl.java:22-23` describe "seed's `assets/...` paths" as
a pass-through case. Grepped `src/main/resources/db/migration/V1__init_db.sql` for `assets` and
image-extension literals — zero matches. This comment appears to describe frontend seed conventions,
not anything in this backend's own Flyway seed. Not a functional bug (the pass-through logic is
correct regardless of whether the seed currently uses it), just a slightly misleading code comment.

## Verified as Correct (no issue)

- **DTO coverage (task item c):** grepped all `dto/**` for name patterns `url|Url|thumb|Thumb|image|
  Image|logo|Logo|icon|Icon|avatar|Avatar|photo|Photo`; every image-URL-bearing field across product/
  news/page/banner/gallery/user/cart/media domains carries `@StorageUrl`; `PaymentInfoResponse
  .qrImageUrl` and `ShowroomResponse.mapEmbedUrl`/`ShowroomCreateRequest.mapEmbedUrl` (both external
  URLs) and `BannerEntity.linkUrl`/`BannerResponse.linkUrl` (click-through link, not an image) are
  correctly left unannotated.
- **Double-prepend risk:** `StorageUrlSerializer` gates the prepend on `value.startsWith(prefix)`
  where `prefix` is the bare `/files` (not the public base). An already-absolute value
  (`http://host/files/x.jpg`) does not start with `/files`, so it is never double-prefixed. Confirmed
  logically sound for all stated cases (null, external URL, seed-style relative path, already-
  absolute value).
- **Path traversal (upload + delete):** both `LocalFileStorageService.uploadImage` (line 61-64) and
  `.delete` (line 104-107) resolve+normalize the target against the base and reject/no-op if
  `!target.startsWith(base)`. Covered by
  `LocalFileStorageServiceTest.uploadImage_pathTraversalInFolder_isRejected`.
  Filename-derived extension spoofing defended (`validateAndResolveExtension` derives `.ext` purely
  from the whitelisted content-type map, never the original filename) — covered by
  `uploadImage_spoofedHtmlFile_isStoredWithImageExtensionNotHtml` and `uploadImage_svgIsRejected`.
- **Business-logic regression check (task item b):** `ProductImageServiceImpl` (upload/delete) and
  `ProductImageController` are unchanged in logic (only import/type surface affected by the
  interface); `image.getUrl()` passed to `fileStorageService.delete()` is always the raw entity field
  (relative path), matching what `LocalFileStorageService.delete()` expects. `ProductServiceImpl`
  (bulk product delete → cascade image delete) has zero diff — confirmed via `git diff`, so any
  pre-existing "file delete inside a DB transaction, not rolled back on later failure" smell is
  **not** a regression introduced by this migration.
- **MinIO removal cleanliness:** `grep -ri minio src/main pom.xml src/main/resources/application.yaml`
  returns nothing; `io.minio` dependency removed from `pom.xml`; `MinioStorageService/Config/
  Properties/BucketInitializer` all deleted (confirmed in `git status`).
- **SecurityConfig:** `/files/**` added to `PUBLIC_ENDPOINTS` (all-methods permit), but since the
  Spring resource handler only ever responds to GET/HEAD for that pattern and no controller maps
  other verbs there, this is not a write-bypass — consistent with "public read, no JWT" intent.
- **`X-Content-Type-Options: nosniff` filter** (`WebStorageConfig:56-69`) correctly scoped to the
  `/files/*` URL pattern only, defense-in-depth on top of the upload-time whitelist.

## Recommended Actions

1. Fix H1: pre-create `app.storage.root` in `WebStorageConfig.addResourceHandlers()` (or otherwise
   guarantee a trailing-slash location URI) before it's registered as a resource location.
2. Get an explicit answer on M1 (pre-existing MinIO URL backfill) — confirm whether this app has any
   production data with old MinIO URLs, and if so, document the accepted risk or add a backfill step.
3. Locate/re-supply the actual plan file so acceptance criteria and phase Success Criteria checklists
   can be verified as originally requested (item (a) is currently unresolved).
4. Optional: note the `SpringHandlerInstantiator` dependency explicitly in
   `StorageUrlSerializer`/`Deserializer` Javadoc (L1's trade-off), and correct or remove the
   "seed's assets/... paths" comment if it doesn't reflect this backend's actual seed data (L4).

## Unresolved Questions

- Where is `plans/260718-0909-migrate-minio-to-local-filesystem/` actually located? It is not in
  this git repo (tracked or untracked) or on disk under this working directory.
- Does any production/staging database currently hold rows with absolute MinIO URLs in the
  `@StorageUrl`-annotated columns? If yes, M1 needs a concrete backfill/decommission plan before the
  old MinIO instance is torn down.
