# Settled

A double-entry ledger system built in Go.

**Current Phase:** P0 - Local Development Setup

---

## Quick Start

### Prerequisites

- Go 1.27+
- Docker 29.0+
- Docker Compose 5.0+
- Make 4.3+

### Setup

```bash
git clone <repo>
cd settled
make build
make up
make test
```

Verify: `curl http://localhost:8080/health` returns `{"status":"ok"}`

---

## Project Structure

```
settled/
├── cmd/main/main.go           # Application entry point
├── pkg/                       # Package stubs (P1+)
│   ├── db/db.go
│   ├── handler/handler.go
│   └── model/model.go
├── test/                      # Tests
│   ├── app_test.go           # Unit test: /health endpoint
│   └── integration_test.go    # Integration test: Docker Compose
├── Dockerfile                 # Multi-stage build
├── docker-compose.yml         # Postgres, Redis, App services
├── Makefile                   # Build commands
├── go.mod / go.sum            # Dependencies
└── README.md                  # This file
```

---

## Services

- **Postgres** (port 5432): postgres:18-alpine
- **Redis** (port 6379): redis:8.10.1-alpine
- **App** (port 8080): Go application with GET /health endpoint

---

## Make Commands

```bash
make build      # Compile binary
make up         # Start services
make down       # Stop services
make test       # Run tests
make clean      # Clean build artifacts
```

---

## Testing

```bash
# Unit test
go test ./test -run TestHealthCheckEndpoint -v

# Integration test (requires make up)
go test ./test -run TestIntegration -v

# All tests
make test
```

---

## Environment Variables

Set in docker-compose.yml:

```
POSTGRES_USER: settled_usr
POSTGRES_PASSWORD: settled_dev
POSTGRES_DB: settled
PORT: 8080
```

---

## Troubleshooting

### Container exits on make up
```bash
docker system prune -a
docker volume prune -f
make up
```

### Port 8080 in use
```bash
PORT=9000 make up
```

### Postgres auth fails
Update credentials in docker-compose.yml and test/integration_test.go to match.

---

## What's Next

Phase 1: Core Ledger

---

## License

MIT
