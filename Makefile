.PHONY: up down migrate-status migrate-up migrate-down migrate-rollback test clean help logs build

include .env
export

DB_URL := postgres://$(POSTGRES_USER):$(POSTGRES_PASSWORD)@postgres:$(POSTGRES_PORT)/$(POSTGRES_DB)?sslmode=disable
MIGRATE := docker compose run --rm -T migrate -path=/migrations -database "postgres://$(POSTGRES_USER):$(POSTGRES_PASSWORD)@postgres:$(POSTGRES_PORT)/$(POSTGRES_DB)?sslmode=disable" 
STRIP_LOGS := version 2>&1 | grep -v "Container\|^\[" || true

help:
	@echo "Settled: Local Development"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  up                  - Start all services (postgres, redis, app)"
	@echo "  down                - Stop all services (removes volumes for clean restart)"
	@echo "  test                - Run tests + coverage"
	@echo "  migrate-up [ver]    - Apply migrations (optional: ver=002)"
	@echo "  migrate-down [ver]  - Rollback migrations (optional: ver=001)"
	@echo "  migrate-rollback    - Rollback N steps (optional: steps=3, default=1)"
	@echo "  migrate-status      - Show current migration version"
	@echo "  build               - Compile Go binary to bin/"
	@echo "  logs                - Tail Docker Compose logs"
	@echo "  clean               - Remove containers, volumes, images"
	@echo ""

up:
	@echo "🚀 Starting services..."
	docker compose up -d
	@echo "✅ Services running. View logs with: make logs"

down:
	@echo "🛑 Stopping services and removing volumes..."
	docker compose down -v
	@echo "✅ Services stopped (clean state)"

test:
	@echo "🧪 Running tests..."
	go test -v -cover ./...
	@echo "✅ Tests completed"

migrate-up: 
	@if [ -z "$(ver)" ]; then \
		echo "Applying all pending migrations..."; \
		$(MIGRATE) up $(STRIP_LOGS); \
		echo "✅ Migrations applied successfully"; \
	else \
		echo "Applying migrations up to version $(ver)..."; \
		$(MIGRATE) goto $$(printf "%d" $(ver)) $(STRIP_LOGS); \
		echo "✅ Migrations applied up to version $$(printf "%d" $(ver))"; \
	fi

migrate-down: 
	@if [ -z "$(ver)" ]; then \
		echo "Rolling back ALL migrations..."; \
		$(MIGRATE) down $(STRIP_LOGS); \
		echo "✅ Rollback complete"; \
	else \
		echo "Rolling back to version $(ver)..."; \
		$(MIGRATE) goto $$(printf "%d" $(ver)) $(STRIP_LOGS); \
		echo "✅ Rollback to version $$(printf "%d" $(ver)) complete"; \
	fi

migrate-rollback: 
	@STEPS=$${steps:-1}; \
	echo "Rolling back $$STEPS step(s)..."; \
	$(MIGRATE) down $$STEPS $(STRIP_LOGS)
	echo "✅ Rollback complete"

migrate-status:
	@echo "📍 Current Database Migration Status:"
	@echo "======================================"
	@VERSION=$$($(MIGRATE) version $(STRIP_LOGS)); echo "Version: $$VERSION"; echo "Status: Applied"
	@echo "======================================"
	@echo "✅ Status check complete"

build:
	@echo "🔨 Building Go binary..."
	go build -o bin/settled ./main.go
	@echo "✅ Build complete. Binary located at bin/settled"

logs:
	@echo "📜 Tailing logs..."
	docker compose logs -f

clean:
	@echo "🧹 Cleaning up..."
	docker compose down -v --rmi all --remove-orphans
	rm -rf bin/
	@echo "✅ Clean"