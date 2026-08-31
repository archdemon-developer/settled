package main

import (
	"fmt"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
	_ "github.com/golang-migrate/migrate/v4"
	_ "github.com/google/uuid"
	_ "github.com/jackc/pgx/v5"
	_ "github.com/shopspring/decimal"
)

func main() {
	router := gin.Default()

	router.GET("/health", func(c *gin.Context) {
		fmt.Println("GET - /health - Checking application up status")
		c.JSON(http.StatusOK, gin.H{
			"status": "ok",
		})
	})

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
