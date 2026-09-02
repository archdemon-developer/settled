package main

import (
	"context"
	"fmt"
	"os"

<<<<<<< HEAD
	"github.com/archdemon-developer/settled/pkg/db"
=======
	"github.com/archdemon-developer/settled/pkg/config"
>>>>>>> 0263637f5b616dec1c502b78132b1c1a6e5812f4
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
