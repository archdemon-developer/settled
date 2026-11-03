# Settled

A double-entry ledger as a service. Settled records transactions against accounts that already exist, keeps every transaction balanced (total debits equal total credits), and never deletes history.

**Status:** P0 (local development foundation) is complete. Auth, idempotent posting, reversals and everything after P0 are still ahead; see [What is not built yet](#what-is-not-built-yet).

---

## Stack

| Layer               | Technology                                                           |
| ------------------- | -------------------------------------------------------------------- |
| Backend             | Java 25, Spring Boot 4.1, Spring Data JPA, Flyway, springdoc-openapi |
| Database            | PostgreSQL (UUID primary keys, `NUMERIC(19,4)` money, `TIMESTAMPTZ`) |
| Frontend            | Vue 3.5, TypeScript 6.0, Vite 8, Pinia, Vue Router, Tailwind CSS 4   |
| Tests               | JUnit 5, Mockito, Testcontainers (PostgreSQL), Vitest                |
| Tooling             | Maven, Spotless (Palantir Java Format), JaCoCo, ESLint, Prettier     |
| Local orchestration | Docker Compose                                                       |

---

## Project structure

```
settled/
├── settled-backend/     Spring Boot service (controllers, services, repositories, Flyway migrations)
├── settled-ui/          Vue 3 single-page app, served by nginx in Docker
├── docker-compose.yaml  Local stack: postgres, redis, backend, frontend
├── .github/             CI workflow and issue/PR templates
├── LICENSE
└── README.md
```

---

## Quick start

### Prerequisites

- Docker with Docker Compose v2 (`docker compose`, with a space)
- Only if you run things outside Docker: Java 25, Maven 3.9+, Node.js 26

### 1. Create a `.env` file at the repository root

Compose reads these variables:

```bash
POSTGRES_DB=settled
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-me
POSTGRES_URL=jdbc:postgresql://postgres:5432/settled
REDIS_HOST=redis
REDIS_PORT=6379
```

The Postgres healthcheck in `docker-compose.yaml` is hard-coded to user `postgres` and database `settled`, so keep those two values as shown or the backend will never start.

### 2. Start the stack

```bash
docker compose up --build
```

| Service     | URL                                   |
| ----------- | ------------------------------------- |
| Frontend    | http://localhost:3000                 |
| Backend API | http://localhost:8080/api/v1          |
| Swagger UI  | http://localhost:8080/swagger-ui.html |
| Health      | http://localhost:8080/actuator/health |
| PostgreSQL  | localhost:5432                        |

The frontend's nginx proxies `/api/` to the backend, so the browser only talks to port 3000.

Flyway applies the migrations in `settled-backend/src/main/resources/db/migration` on startup. To wipe the database and start clean:

```bash
docker compose down -v
```

---

## How a transaction works

A transaction is built up in steps, the way accounting systems do it, and is locked once posted.

1. **Create** a transaction. It starts in `DRAFT`.
2. **Add postings** one at a time. Each posting is a `DEBIT` or `CREDIT` of an amount against an account.
3. **Post** the transaction. This succeeds only if it is in `DRAFT` and total debits equal total credits. A posted transaction can no longer take postings.

Nothing is deleted. Statuses are `DRAFT`, `POSTED` and `REVERSED`.

```bash
# 1. Create two accounts
curl -X POST localhost:8080/api/v1/accounts -H 'Content-Type: application/json' \
  -d '{"code":"CHK001","name":"Main Checking","type":"CHECKING"}'
curl -X POST localhost:8080/api/v1/accounts -H 'Content-Type: application/json' \
  -d '{"code":"SAV001","name":"Savings","type":"SAVINGS"}'

# 2. Create a draft transaction
curl -X POST localhost:8080/api/v1/transactions -H 'Content-Type: application/json' \
  -d '{"reference":"TXN-001","description":"Move to savings","postedAt":"2026-11-03T10:00:00.000Z"}'

# 3. Add a debit and a credit (use the ids returned above)
curl -X POST localhost:8080/api/v1/transactions/<txn-id>/postings -H 'Content-Type: application/json' \
  -d '{"accountId":"<chk-id>","amount":"100.00","direction":"DEBIT"}'
curl -X POST localhost:8080/api/v1/transactions/<txn-id>/postings -H 'Content-Type: application/json' \
  -d '{"accountId":"<sav-id>","amount":"100.00","direction":"CREDIT"}'

# 4. Post it
curl -X POST localhost:8080/api/v1/transactions/<txn-id>/post
```

### Endpoints

| Method | Path                                 | Purpose                                            |
| ------ | ------------------------------------ | -------------------------------------------------- |
| POST   | `/api/v1/accounts`                   | Create an account                                  |
| GET    | `/api/v1/accounts`                   | List accounts, optional `?status=ACTIVE\|ARCHIVED` |
| GET    | `/api/v1/accounts/{id}`              | Get an account by id                               |
| GET    | `/api/v1/accounts/code/{code}`       | Get an account by code                             |
| PUT    | `/api/v1/accounts/{id}/archive`      | Archive an account                                 |
| POST   | `/api/v1/transactions`               | Create a draft transaction                         |
| GET    | `/api/v1/transactions`               | List transactions, optional `?status=`             |
| GET    | `/api/v1/transactions/{id}`          | Get a transaction                                  |
| POST   | `/api/v1/transactions/{id}/postings` | Add a posting to a draft transaction               |
| POST   | `/api/v1/transactions/{id}/post`     | Post a balanced draft transaction                  |

Request and response schemas, and per-endpoint status codes, are in Swagger UI.

### Response format

Every response uses the same envelope.

```json
{
  "status": "SUCCESS",
  "code": 201,
  "timestamp": "2026-11-03T10:00:00.123Z",
  "requestId": "…",
  "data": {}
}
```

Errors carry a machine-readable `errorCode` and a human-readable `message`; validation errors also list each failing field.

```json
{
  "status": "ERROR",
  "code": 409,
  "timestamp": "2026-11-03T10:00:00.123Z",
  "requestId": "…",
  "error": {
    "errorCode": "ACC_003",
    "message": "Account code already exists",
    "details": "…",
    "field": "code"
  }
}
```

Money amounts are sent and returned as JSON strings (`"100.00"`), never numbers, so no client ever rounds them through a float. Timestamps are UTC ISO-8601.

---

## Development

### Backend

```bash
cd settled-backend
mvn test              # unit + integration tests (needs Docker for Testcontainers)
mvn spotless:apply    # format code
mvn verify            # tests, Spotless check, JaCoCo coverage gate (90% line coverage)
```

Integration tests run against a real PostgreSQL container with the real Flyway migrations; there is no in-memory database in the test path.

### Frontend

```bash
cd settled-ui
npm ci
npm run dev           # Vite dev server
npm run test:run      # Vitest
npm run lint
npm run format:check
npm run build         # type-check + production build
```

---

## What is not built yet

- Authentication and authorization
- Idempotent posting. The `idempotency_keys` table exists, but nothing reads or writes it yet.
- Reversals of posted transactions
- Account balances and per-account ledger views
- Redis. The container is provisioned in Compose, but the backend does not use it yet.
- Cloud deployment. The target (GCP or AWS) is undecided.

---

## License

GNU General Public License v3. See [LICENSE](LICENSE).
