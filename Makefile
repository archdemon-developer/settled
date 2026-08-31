.PHONY: help up down test migrate clean logs build

help:
	@echo "Settled: Local Development"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  up       - Start all services (postgres, redis, app)"
	@echo "  down     - Stop all services"
	@echo "  test     - Run tests + coverage"
	@echo "  migrate  - Run database migrations"
	@echo "  clean    - Remove containers, volumes, build artifacts"
	@echo "  logs     - Tail Docker Compose logs"
	@echo "  build    - Compile Go binary to bin/"
	@echo ""

up:
	@echo "🚀 Starting services..."
	docker compose up -d
	@echo "✅ Services running. View logs with: make logs"

down:
	@echo "🛑 Stopping services..."
	docker compose down -v
	@echo "✅ Services stopped"

test:
	@echo "🧪 Running tests..."
	go test -v -cover ./...

migrate:
	@echo "🗄️ Running migrations..."
	migrate -path migrations -database "postgres://postgres:postgres@localhost:5432/settled?sslmode=disable" up

clean:
	@echo "🧹 Cleaning up..."
	docker compose down -v --rmi all --remove-orphans

logs:
	@echo "📜 Tailing logs..."
	docker compose logs -f

build:
	@echo "🔨 Building Go binary..."
	go build -o bin/settled ./cmd/main/main.go