# 0001. Identifiers and money

**Status:** Accepted

## Context

A ledger's identifiers must stay stable for the life of the system, and its amounts must never pick up floating-point error. The original master plan used numeric (`Long`) ids. Changing an id type after data exists means rewriting every foreign key, so the choice had to be right from the first migration.

## Decision

- Every primary and foreign key is a `UUID`. The database default is `gen_random_uuid()`, and the entities use `GenerationType.UUID`.
- Money is `java.math.BigDecimal` in code and `NUMERIC(19,4)` in the database. `float` and `double` are never used for amounts.
- The API exposes ids as UUID strings.

## Consequences

- Ids can be generated anywhere without a central sequence, which keeps later phases (sharding, service decomposition) open.
- UUID keys and indexes are larger than `bigint`, and random UUIDs insert less sequentially than a serial key. We accept that cost.
- Scale 4 limits stored amounts to four decimal places.
- There is no currency column yet. Multi-currency is not modelled, and adding it later will need a migration.
