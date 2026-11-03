# 0003. Timestamps are UTC Instants

**Status:** Accepted

## Context

A ledger is an audit record, so a timestamp must mean one unambiguous moment. Time zones and local-time types are a common source of subtle bugs in financial data.

## Decision

- All timestamps are `java.time.Instant` in code and `TIMESTAMPTZ` in the database. DTOs use `Instant` as well.
- On the API they are UTC ISO-8601 strings with millisecond precision, `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`, for example `2026-11-04T10:00:00.000Z`.
- `createdAt` and `updatedAt` are filled in by the application through Hibernate's `@CreationTimestamp` and `@UpdateTimestamp`. The `DEFAULT now()` clauses in the migrations are a safety net for direct inserts, not the primary source.
- `postedAt` on a transaction is the business date supplied by the client. `createdAt` is system time. They are different things.

## Consequences

- No time zone ambiguity anywhere in storage or in the API.
- Clients must send the documented format. A timestamp in a different shape is rejected as a malformed request.
- Display-time conversion to a local zone is the frontend's job.
- Postings have no `updatedAt`, on purpose. See ADR 0004.
