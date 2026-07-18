---
name: project-storage-migration-plan-missing
description: The MinIO-to-local-filesystem migration plan folder referenced by orchestrator tasks does not exist in this repo
metadata:
  type: project
---

`plans/260718-0909-migrate-minio-to-local-filesystem/` (plan.md + phase-01..04) was cited by the
orchestrator as containing accepted design decisions for the MinIO→local-filesystem storage
migration, but as of 2026-07-18 it does not exist anywhere in this repo — not on disk, not in
`git log --all -- plans/` (the repo has no `plans/` directory in history at all).

**Why:** Reviewed the migration cold, against code only, and had to flag acceptance-criteria
verification as unresolved rather than silently skip or fabricate it.

**How to apply:** If asked to review this migration again (or reference its plan), first check
whether `plans/` now exists before assuming the earlier "missing" state still holds — it may have
been added since. If still missing, say so explicitly rather than reviewing as if it were available.
See [[project-vu-gia-storage-migration-findings]] for the actual code findings from the cold review.
