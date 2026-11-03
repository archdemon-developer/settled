# 0002. Enums are stored as VARCHAR

**Status:** Accepted

## Context

`AccountType`, `AccountStatus`, `TransactionStatus` and `PostingDirection` are closed sets today, but they will grow, for example new account types or a new transaction state. PostgreSQL has a native `ENUM` type, so we had to choose where the set of valid values lives.

## Decision

Each enum is a Java `enum` stored as `VARCHAR` with `@Enumerated(EnumType.STRING)`. Names are uppercase in Java and in the database (`'ACTIVE'`, `'DRAFT'`, `'DEBIT'`), and column defaults use the same uppercase form.

Rejected alternatives:
- **PostgreSQL ENUM:** adding or changing a value needs `ALTER TYPE` migrations, which are awkward and easy to get wrong.
- **Ordinal integers:** they lose readability in SQL and break silently if the order changes.

## Consequences

- Adding a value is a one-line Java change with no migration.
- The database does not reject an invalid string. The application is the only gate. There are no `CHECK` constraints today, and we can add them later if compliance requires database-level enforcement.
- Raw SQL and the stored data stay readable.
