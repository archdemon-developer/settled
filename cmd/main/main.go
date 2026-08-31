package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	_ "github.com/golang-migrate/migrate/v4"
	_ "github.com/google/uuid"
	_ "github.com/jackc/pgx/v5"
	_ "github.com/shopspring/decimal"
	_ "github.com/stretchr/testify/assert"
)

func main() {
	router := gin.Default()

	router.GET("/health", func(c *gin.Context) {
		fmt.Println("GET - /health - Checking application up status")
		c.JSON(http.StatusOK, gin.H{
			"status": "ok",
		})
	})

	router.Run()
}
