# Architecture decision records

Short records of the decisions that shape Settled: what we chose, why, and what it costs. Each file is one decision. If a decision changes, add a new record that supersedes the old one and update the old one's status instead of rewriting it.

| # | Decision |
|---|---|
| [0001](0001-identifiers-and-money.md) | UUID identifiers, `BigDecimal` and `NUMERIC(19,4)` for money |
| [0002](0002-enums-as-varchar.md) | Enums stored as `VARCHAR`, not PostgreSQL ENUMs |
| [0003](0003-timestamps.md) | UTC `Instant` timestamps, millisecond ISO-8601 on the wire |
| [0004](0004-transaction-lifecycle.md) | DRAFT, incremental postings, atomic POST; immutability and no deletes |
| [0005](0005-money-as-strings-on-the-wire.md) | Money amounts are strings in API responses |
| [0006](0006-api-contract-and-errors.md) | Response envelope, `ErrorCode`, exception handling |
| [0007](0007-dtos-and-mapping.md) | DTOs separate from entities, `MapperUtil` |
| [0008](0008-testing-strategy.md) | Testcontainers PostgreSQL, no H2, four test layers |
| [0009](0009-schema-management.md) | Flyway SQL migrations, `ddl-auto: validate` |
| [0010](0010-frontend-choices.md) | `fetch` wrapper, TypeScript 6.0 pin, static hosting |

## Template

```
# NNNN. Title

**Status:** Proposed | Accepted | Superseded by NNNN

## Context
## Decision
## Consequences
```
