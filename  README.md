# Settled

Double-entry ledger as-a-service platform.

**Stack:** Spring Boot 4.1.1 | Vue 3.5 | TypeScript 7.0 | PostgreSQL | Redis | Docker

---

## Project Structure

```
settled/
├── settled-backend/          Spring Boot backend service
├── settled-ui/               Vue 3.5 + TypeScript frontend
├── docker-compose.yml        Local development orchestration
├── .github/workflows/        CI/CD pipelines
├── LICENSE                   GPL v3
└── README.md                 This file
```

---

## Quick Start

### Prerequisites
- Node.js 24+
- Docker & Docker Compose
- Java 25+ (for backend development)

### Local Development

```bash
docker compose up
```

Frontend: http://localhost:3000
Backend: http://localhost:8080
PostgreSQL: localhost:5432
Redis: localhost:6379

---

## License

GNU General Public License v3 — See LICENSE file for details.