package config

import (
	"fmt"
	"os"
	"strconv"
	"time"
)

type Config struct {
	Addr                       string
	DatabaseDSN                string
	DatabasePreferSimpleProto  bool
	DatabaseMaxOpenConns       int
	DatabaseMaxIdleConns       int
	DatabaseConnMaxLifetime    time.Duration
	DatabaseConnMaxIdleTime    time.Duration
	DatabaseConnectMaxAttempts int
	DatabaseConnectRetryDelay  time.Duration
	JWTSecret                  string
	TokenTTL                   time.Duration
}

func Load() Config {
	return Config{
		Addr:                       env("HTTP_ADDR", ":8080"),
		DatabaseDSN:                databaseDSN(),
		DatabasePreferSimpleProto:  envBool("DB_PREFER_SIMPLE_PROTOCOL", false),
		DatabaseMaxOpenConns:       envInt("DB_MAX_OPEN_CONNS", 10),
		DatabaseMaxIdleConns:       envInt("DB_MAX_IDLE_CONNS", 5),
		DatabaseConnMaxLifetime:    time.Duration(envInt("DB_CONN_MAX_LIFETIME_SECONDS", 300)) * time.Second,
		DatabaseConnMaxIdleTime:    time.Duration(envInt("DB_CONN_MAX_IDLE_TIME_SECONDS", 60)) * time.Second,
		DatabaseConnectMaxAttempts: envInt("DB_CONNECT_MAX_ATTEMPTS", 10),
		DatabaseConnectRetryDelay:  time.Duration(envInt("DB_CONNECT_RETRY_DELAY_SECONDS", 2)) * time.Second,
		JWTSecret:                  env("JWT_SECRET", "change-me-in-production"),
		TokenTTL:                   time.Duration(envInt("JWT_TTL_HOURS", 24)) * time.Hour,
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

func envBool(key string, fallback bool) bool {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := strconv.ParseBool(value)
	if err != nil {
		return fallback
	}
	return parsed
}
