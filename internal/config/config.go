package config

import (
	"fmt"
	"os"
	"strconv"
	"time"
)

type Config struct {
	Addr       string
	DatabaseDSN string
	JWTSecret  string
	TokenTTL   time.Duration
}

func Load() Config {
	return Config{
		Addr:        env("HTTP_ADDR", ":8080"),
		DatabaseDSN: databaseDSN(),
		JWTSecret:   env("JWT_SECRET", "change-me-in-production"),
		TokenTTL:    time.Duration(envInt("JWT_TTL_HOURS", 24)) * time.Hour,
	}
}

func databaseDSN() string {
	if dsn := os.Getenv("DATABASE_URL"); dsn != "" {
		return dsn
	}

	host := env("DB_HOST", "localhost")
	port := env("DB_PORT", "5432")
	user := env("DB_USER", "postgres")
	password := env("DB_PASSWORD", "postgres")
	database := env("DB_NAME", "users_orders")
	sslMode := env("DB_SSLMODE", "disable")

	return fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=%s", host, port, user, password, database, sslMode)
}

func env(key, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}

func envInt(key string, fallback int) int {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := strconv.Atoi(value)
	if err != nil {
		return fallback
	}
	return parsed
}
