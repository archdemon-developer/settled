# 0005. Money is a string in API responses

**Status:** Accepted

## Context

Settled is a ledger as a service, so the API contract is the product. A JSON number is parsed as a floating-point value by many clients, including JavaScript, which would quietly round money.

## Decision

- Amounts in responses are JSON strings, for example `"100.50"`. `PostingResponse.amount` carries `@JsonFormat(shape = STRING)`.
- Request parsing already accepts strings for `BigDecimal`, so request DTOs did not change.
- Frontend types declare `amount: string`.

## Consequences

- Clients that need to do arithmetic must parse the string with a decimal type.
- Requests still accept a JSON number as well as a string, because Jackson coerces both. A client that sends a float literal can still lose precision before it reaches us. If that becomes a real risk, strict string-only input is a possible follow-up.
- Only `PostingResponse.amount` is a money field in P0. Any new money field must be annotated the same way.
