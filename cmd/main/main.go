package main

import (
	"fmt"
	"os"

	"github.com/archdemon-developer/settled/pkg/config"

	"github.com/archdemon-developer/settled/pkg/handler"
	"github.com/gin-gonic/gin"
	_ "github.com/golang-migrate/migrate/v4"
	_ "github.com/google/uuid"
	_ "github.com/jackc/pgx/v5"
	_ "github.com/shopspring/decimal"
)

func main() {

	cfg, err := config.Load()

	if err != nil {
		fmt.Printf("Failed to load configuration: %v\n", err)
		os.Exit(1)
	}

	fmt.Printf("✅ Config loaded (DB: %s:%d, Redis: %s:%d, Port: %s)\n",
		cfg.PostgresHost, cfg.PostgresPort,
		cfg.RedisHost, cfg.RedisPort,
		cfg.Port)

	app := handler.NewApp(cfg)

	router := gin.Default()

	router.GET("/health", app.GetHealth)

	fmt.Printf("Starting server on port %s\n", cfg.Port)

	if err := router.Run(":" + cfg.Port); err != nil {
		fmt.Printf("Failed to start server: %v\n", err)
		os.Exit(1)
	}
}
