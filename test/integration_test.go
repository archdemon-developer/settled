package test

import (
	"context"
	"database/sql"
	"net/http"
	"testing"
	"time"

	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestIntegration_Services(t *testing.T) {
	t.Log("Testing against running services...")

	t.Run("PostgreSQL", func(t *testing.T) {
		db, err := sql.Open("pgx", "postgres://settled_usr:settled_dev@localhost:5432/settled?sslmode=disable")
		require.NoError(t, err, "Failed to connect to PostgreSQL")
		defer db.Close()

		err = db.Ping()
		require.NoError(t, err, "PostgreSQL ping failed")
		t.Log("✅ PostgreSQL connected")
	})

	t.Run("Redis", func(t *testing.T) {
		rdb := redis.NewClient(&redis.Options{
			Addr: "localhost:6379",
		})
		defer rdb.Close()

		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()

		_, err := rdb.Ping(ctx).Result()
		require.NoError(t, err, "Redis ping failed")
		t.Log("✅ Redis connected")
	})

	t.Run("Database Schema", func(t *testing.T) {
		db, err := sql.Open("pgx", "postgres://settled_usr:settled_dev@localhost:5432/settled?sslmode=disable")
		require.NoError(t, err)
		defer db.Close()

		var schemaExists bool
		err = db.QueryRow(`
			SELECT EXISTS(
				SELECT 1 FROM information_schema.schemata 
				WHERE schema_name = 'settled'
			)
		`).Scan(&schemaExists)
		require.NoError(t, err)
		assert.True(t, schemaExists, "settled schema should exist")

		var accountsTableExists bool
		err = db.QueryRow(`
			SELECT EXISTS(
				SELECT 1 FROM information_schema.tables 
				WHERE table_schema = 'settled' AND table_name = 'accounts'
			)
		`).Scan(&accountsTableExists)
		require.NoError(t, err)
		assert.True(t, accountsTableExists, "accounts table should exist")

		var ledgerTableExists bool
		err = db.QueryRow(`
			SELECT EXISTS(
				SELECT 1 FROM information_schema.tables 
				WHERE table_schema = 'settled' AND table_name = 'ledger'
			)
		`).Scan(&ledgerTableExists)
		require.NoError(t, err)
		assert.True(t, ledgerTableExists, "ledger table should exist")

		t.Log("✅ Database schema verified")
	})

	t.Run("Health Endpoint", func(t *testing.T) {
		resp, err := http.Get("http://localhost:8080/health")
		require.NoError(t, err, "Failed to reach health endpoint")
		defer resp.Body.Close()

		assert.Equal(t, http.StatusOK, resp.StatusCode, "Health endpoint should return 200")
		t.Log("✅ Health endpoint healthy")
	})

	t.Log("✅ All integration tests passed")
}
