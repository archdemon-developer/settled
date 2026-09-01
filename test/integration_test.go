package test

import (
	"context"
	"database/sql"
	"net/http"
	"os/exec"
	"testing"
	"time"

	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestIntegration_DockerCompose(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	t.Log("Starting services with make up...")
	cmd := exec.Command("make", "up")
	cmd.Dir = "../"
	err := cmd.Run()

	require.NoError(t, err, "Failed to start services with make up")
	t.Log("Starting services with make up...")
	//DEL BELOW
	cmd1 := exec.Command("make", "logs")
	cmd1.Dir = "../"
	output1, err1 := cmd1.CombinedOutput()
	if err1 != nil {
		t.Logf("Failed to fetch logs: %v", err1)
	}
	t.Logf("Docker logs:\n%s", string(output1))
	// DEL ABOVE
	t.Log("Giving services time to start...")
	time.Sleep(5 * time.Second)

	defer func() {
		t.Log("Stopping services with make down...")
		cmd := exec.Command("make", "down")
		cmd.Dir = "../"
		err := cmd.Run()
		require.NoError(t, err, "Failed to stop services with make down")
	}()

	t.Log("Waiting for services to be ready...")
	require.Eventually(t, func() bool { return waitForHealthy(t) }, 60*time.Second, 1*time.Second, "Services did not become ready in time")

	t.Log("Services are ready. Running integration tests...")

	t.Run("HealthEndpoint", func(t *testing.T) {
		t.Log("Testing GET /health endpoint...")
		resp, err := http.Get("http://localhost:8080/health")
		require.NoError(t, err)
		defer resp.Body.Close()
		t.Logf("Health endpoint returned status: %d", resp.StatusCode)
		assert.Equal(t, http.StatusOK, resp.StatusCode)
	})
}

func waitForHealthy(t *testing.T) bool {
	t.Log("Checking PostgreSQL...")
	db, err := sql.Open("pgx", "postgres://settled_usr:settled_dev@localhost:5432/settled?sslmode=disable")
	if err != nil {
		t.Logf("PostgreSQL Open failed: %v", err)
		return false
	}
	defer db.Close()

	err = db.Ping()
	if err != nil {
		t.Logf("PostgreSQL Ping failed: %v", err)
		return false
	}
	t.Log("PostgreSQL OK ✅")

	t.Log("Checking Redis...")
	rdb := redis.NewClient(&redis.Options{
		Addr: "localhost:6379",
	})
	defer rdb.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 1*time.Second)
	defer cancel()

	_, err = rdb.Ping(ctx).Result()
	if err != nil {
		t.Logf("Redis Ping failed: %v", err)
		return false
	}
	t.Log("Redis OK ✅")

	t.Log("Checking application health endpoint...")
	resp, err := http.Get("http://localhost:8080/health")
	if err != nil {
		t.Logf("Health endpoint unreachable: %v", err)
		return false
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Logf("Health endpoint returned %d, expected 200", resp.StatusCode)
		return false
	}
	t.Log("Application health endpoint OK ✅")

	return true
}
