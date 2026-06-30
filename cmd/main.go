package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	_ "practice_1/docs"

	"practice_1/internal/config"
	"practice_1/internal/handlers"
	"practice_1/internal/repository"
	"practice_1/internal/services"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	gormlogger "gorm.io/gorm/logger"
)

// @title           Users and Orders API
// @version         1.0
// @description     REST API for managing users and their orders with JWT authentication.
// @BasePath        /api
// @schemes         https
// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
func main() {
	cfg := config.Load()

	gormConfig := &gorm.Config{Logger: gormlogger.Default.LogMode(gormlogger.Warn)}
	db, err := gorm.Open(postgres.Open(cfg.DatabaseDSN), gormConfig)
	if err != nil {
		log.Fatalf("failed to connect to database: %v", err)
	}

	sqlDB, err := db.DB()
	if err != nil {
		log.Fatalf("failed to get underlying sql.DB: %v", err)
	}
	sqlDB.SetMaxOpenConns(25)
	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetConnMaxLifetime(5 * time.Minute)

	repo := repository.New(db)
	if err := repo.Migrate(context.Background()); err != nil {
		log.Fatalf("failed to run migrations: %v", err)
	}

	userService := services.NewUserService(repo)
	orderService := services.NewOrderService(repo)
	authService := services.NewAuthService(repo, cfg.JWTSecret, cfg.TokenTTL)

	h := handlers.New(userService, orderService, authService)
	server := &http.Server{
		Addr:              cfg.Addr,
		Handler:           h.Routes(cfg.JWTSecret),
		ReadHeaderTimeout: 5 * time.Second,
		ReadTimeout:       15 * time.Second,
		WriteTimeout:      15 * time.Second,
		IdleTimeout:       60 * time.Second,
	}

	go func() {
		log.Printf("server starting on %s", cfg.Addr)
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("server error: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("server shutting down...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		log.Fatalf("server forced to shutdown: %v", err)
	}

	_ = sqlDB.Close()
	log.Println("server stopped")
}
