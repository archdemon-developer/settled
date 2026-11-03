# 0007. DTOs are separate from entities

**Status:** Accepted

## Context

Entities change with the schema. Request and response shapes are the public API. Coupling them means every schema tweak risks a breaking API change.

## Decision

- Services accept request DTOs and return response DTOs. Entities never cross the service boundary.
- Request DTOs carry the Jakarta Bean Validation rules. Business rules are checked in the service layer after those pass.
- List views return a lightweight `TransactionResponse`. The detail view, `TransactionDetailResponse`, extends it with the postings list. Both use Lombok `@SuperBuilder` so the child builder sees the parent's fields.
- `MapperUtil` is the single place that converts entities to responses, as static methods.

Rejected: Jackson `ObjectMapper` conversion and MapStruct. Both add indirection for a mapping layer this small.

## Consequences

- A new field must be added to the entity, the DTO and `MapperUtil`, and `MapperUtilsTests` should be updated with it.
- Static mapper methods are simple but cannot be injected or swapped. That is fine at this size.
- `GET /transactions/{id}` returns the summary without postings. Today the only response that shows a transaction's postings is the one returned by adding a posting. A read endpoint for postings is a good P1 candidate.
