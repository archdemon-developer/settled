# 0009. Schema management with Flyway

**Status:** Accepted

## Context

The schema has to change safely over many phases, and Hibernate should never be the thing that alters it.

## Decision

- Flyway with plain SQL migrations in `src/main/resources/db/migration`, named `V{n}__Description.sql`, one table per file (`V1` accounts, `V2` transactions, `V3` postings, `V4` idempotency keys).
- Hibernate runs with `ddl-auto: validate` in dev and in tests. It checks the entities against the schema but never changes it.
- Liquibase was rejected as needlessly complicated for this project.
- **While Settled is local-only, an already-applied migration may be edited in place.** Reset the local database with `docker compose down -v` to re-run it. This is how the `transactions.status` default was corrected from `POSTED` to `DRAFT`.

## Consequences

- **This rule ends at the first deployed environment.** From then on migrations are forward-only: a change means a new `V{n}` file, and applied files are never edited, because editing one changes its checksum and Flyway will refuse to start.
- Plain SQL keeps the schema readable and reviewable.
- Some tooling (the checksum check) will fail loudly on a local database that has the old version applied. The fix is the volume reset above.
