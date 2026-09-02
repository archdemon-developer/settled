package config

import (
	"fmt"
	"os"
	"strconv"

	"github.com/joho/godotenv"
)

type Config struct {
	PostgresUser   string
	PostgresPass   string
	PostgresDBName string
	PostgresHost   string
	PostgresPort   int
	DbMaxConns     int
	DbMinIdle      int
	DbMaxLifetime  int
	DbMaxIdleTime  int

	RedisHost     string
	RedisPort     int
	RedisMaxConns int
	RedisMinIdle  int

	Port string
}

func Load() (*Config, error) {
	err := godotenv.Load()

	if err != nil {
		fmt.Println("No .env file found, using environment variables")
	}

	fg := &fieldGetter{}
	cfg := &Config{
		PostgresUser:   fg.requiredString("POSTGRES_USER"),
		PostgresPass:   fg.requiredString("POSTGRES_PASSWORD"),
		PostgresDBName: fg.requiredString("POSTGRES_DB"),
		PostgresHost:   fg.requiredString("POSTGRES_HOST"),
		PostgresPort:   fg.optionalInt("POSTGRES_PORT", 5432),
		DbMaxConns:     fg.optionalInt("DB_MAX_CONNS", 10),
		DbMinIdle:      fg.optionalInt("DB_MIN_IDLE", 2),
		DbMaxLifetime:  fg.optionalInt("DB_MAX_LIFETIME", 3600),
		DbMaxIdleTime:  fg.optionalInt("DB_MAX_IDLE_TIME", 900),

		RedisHost:     fg.requiredString("REDIS_HOST"),
		RedisPort:     fg.optionalInt("REDIS_PORT", 6379),
		RedisMaxConns: fg.optionalInt("REDIS_MAX_CONNS", 10),
		RedisMinIdle:  fg.optionalInt("REDIS_MIN_IDLE", 2),

		Port: getEnvOrDefault("PORT", "8080"),
	}

	if err := fg.Err(); err != nil {
		return nil, err
	}

	return cfg, nil
}

func (c *Config) BuildDatabaseURL() string {
	return fmt.Sprintf(
		"postgres://%s:%s@%s:%d/%s?sslmode=disable",
		c.PostgresUser,
		c.PostgresPass,
		c.PostgresHost,
		c.PostgresPort,
		c.PostgresDBName,
	)
}

type fieldGetter struct {
	errs []error
}

func (fg *fieldGetter) requiredString(key string) string {
	val := os.Getenv(key)
	if val == "" {
		fg.errs = append(fg.errs, fmt.Errorf("required environment variable %s is not set", key))
	}
	return val
}

func (fg *fieldGetter) optionalInt(key string, defaultVal int) int {
	return getEnvInt(key, defaultVal)
}

func getEnvInt(key string, defaultVal int) int {
	val := os.Getenv(key)
	if val == "" {
		return defaultVal
	}
	i, err := strconv.Atoi(val)

	if err != nil {
		fmt.Printf("Invalid integer value for environment variable %s: %s. Using default value: %d\n", key, val, defaultVal)
		return defaultVal
	}

	return i
}

func (fg *fieldGetter) Err() error {
	if len(fg.errs) > 0 {
		return fmt.Errorf("configuration errors: %v", fg.errs)
	}
	return nil
}

func getEnvOrDefault(key, defaultVal string) string {
	val := os.Getenv(key)

	if val == "" {
		return defaultVal
	}

	return val
}
