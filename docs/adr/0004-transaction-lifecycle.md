# 0004. Transaction lifecycle and immutability

**Status:** Accepted

## Context

Double-entry accounting requires that total debits equal total credits, and that history is never rewritten. We also wanted the workflow to feel like the accounting systems people already use: build a transaction up, check it, then lock it.

## Decision

A transaction moves through these states: `DRAFT`, then `POSTED`, and later `REVERSED`.

1. **Create** a transaction. It starts as `DRAFT`.
2. **Add postings** one at a time (`POST /transactions/{id}/postings`). Each posting is a `DEBIT` or `CREDIT` against an account and is only allowed while the transaction is `DRAFT`.
3. **Post** (`POST /transactions/{id}/post`). This is one atomic operation that requires:
   - the transaction is `DRAFT` (otherwise `TXN_002`, 409)
   - it has at least one posting (otherwise `TXN_004`, 422)
   - total debits equal total credits (otherwise `POST_001`, 422)

Other rules:
- Postings are immutable. There is no update endpoint and no `updated_at` column on `postings`.
- There are no delete endpoints. Corrections happen by reversal, never by deletion.
- `REVERSED` exists in the enum, but reversal is not implemented yet.

Rejected: sending all postings in a single atomic request. Incremental building lets a client get balance feedback as it goes and avoids locking in bad data.

## Consequences

- A `DRAFT` can hold an unbalanced state while it is being built. Only `POST` enforces balance.
- There is currently no way to remove a wrong posting from a `DRAFT`. The workaround is to abandon that transaction and create a new one.
- `postings.transaction_id` has `ON DELETE CASCADE` at the database level. No API path deletes transactions, but a direct SQL delete would remove its postings.
- The empty-transaction check exists because an empty list adds up to 0 on both sides and would pass the balance test.
