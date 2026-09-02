package main

import (
	"context"
	"fmt"
	"os"

	"github.com/archdemon-developer/settled/pkg/db"
	"github.com/archdemon-developer/settled/pkg/handler"
	"github.com/gin-gonic/gin"
	_ "github.com/golang-migrate/migrate/v4"
	_ "github.com/google/uuid"
	_ "github.com/jackc/pgx/v5"
	_ "github.com/shopspring/decimal"
)

func main() {

	dbURL := os.Getenv("DATABASE_URL")

	if dbURL == "" {
		fmt.Println("DATABASE_URL environment variable is not set")
		os.Exit(1)
	}

	pool, err := db.OpenDB(context.Background(), dbURL)

	if err != nil {
		fmt.Printf("Failed to open database: %v\n", err)
		os.Exit(1)
	}

	defer pool.Close()

	fmt.Println("Database connection established successfully")

	app := handler.NewApp(pool)

	router := gin.Default()

	router.GET("/health", app.GetHealth)

	port := os.Getenv("PORT")

	if port == "" {
		port = "8080"
	}

	fmt.Printf("Starting server on port %s\n", port)

	if err := router.Run(":" + port); err != nil {
		fmt.Printf("Failed to start server: %v\n", err)
		os.Exit(1)
	}
}
