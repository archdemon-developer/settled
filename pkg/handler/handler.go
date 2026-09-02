package handler

import (
	"net/http"

	"github.com/archdemon-developer/settled/pkg/config"
	"github.com/gin-gonic/gin"
)

type App struct {
	cfg *config.Config
}

func NewApp(cfg *config.Config) *App {
	return &App{cfg: cfg}
}

func (a *App) GetHealth(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"status": "ok",
	})
}
