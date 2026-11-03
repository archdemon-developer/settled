# 0006. API contract and error handling

**Status:** Accepted

## Context

For a ledger, a vague error is a dishonest one. Clients need to react to failures programmatically, operators need to trace a single failure, and internal details must never leak to callers.

## Decision

**One envelope for every response:** `status` (`SUCCESS` or `ERROR`), `code` (the HTTP status), `timestamp`, `requestId`, and either `data` or `error`. Each response gets its own freshly generated `requestId`.

**Errors are structured.** `ErrorCode` is the single source of truth. Each entry has a stable code, a message and an HTTP status:

| Prefix | Meaning |
|---|---|
| `ACC_` | accounts |
| `TXN_` | transactions |
| `POST_` | postings and balance |
| `VAL_` | validation and malformed input |
| `IDEM_` | idempotency (reserved) |

Services throw a typed exception (`SettledException` and its subclasses `ResourceNotFoundException`, `DuplicateResourceException`, `InvalidPostingException`), and `GlobalExceptionHandler` turns it into the envelope.

**Handler mapping:**

| Failure | Result |
|---|---|
| `SettledException` | the status and code from its `ErrorCode` |
| Bean validation failure | 400, `VAL_001`, with a per-field `violations` list |
| Unreadable body, such as broken JSON or a bad enum value | 400, `VAL_002` |
| Bad path or query value, such as a non-UUID id | 400, `VAL_003`, with the parameter name in `field` |
| Anything else | 500, `INTERNAL_ERROR`, with no internal message |

Parser and framework messages are logged on the server and are never returned to the client.

## Consequences

- Error codes are part of the public contract. Renaming or renumbering one is a breaking change.
- `requestId` is generated but not yet logged, so a client-reported id cannot yet be matched to a server log line. Correlating the two is a follow-up.
- Some codes are defined but unused today (for example `ACC_004`, `POST_002` and `IDEM_001`) and should be removed or put to use.
