package db

import (
	"context"
	"fmt"
	"time"

	"github.com/archdemon-developer/settled/pkg/config"
	"github.com/jackc/pgx/v5/pgxpool"
)

func OpenDB(ctx context.Context, dbURL string, cfg *config.Config) (*pgxpool.Pool, error) {
	poolConfig, err := pgxpool.ParseConfig(dbURL)

	if err != nil {
		return nil, fmt.Errorf("failed to parse database URL: %w", err)
	}

	poolConfig.MaxConns = int32(cfg.DbMaxConns)
	poolConfig.MinConns = int32(cfg.DbMinIdle)
	poolConfig.MaxConnLifetime = time.Duration(cfg.DbMaxLifetime) * time.Second
	poolConfig.MaxConnIdleTime = time.Duration(cfg.DbMaxIdleTime) * time.Second

	dbPool, err := pgxpool.NewWithConfig(ctx, poolConfig)

	if err != nil {
		return nil, fmt.Errorf("failed to create database connection pool: %w", err)
	}

	return dbPool, nil
}
