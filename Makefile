.PHONY: swagger tidy build run test docker-up docker-down

SWAG_CMD := $(shell go env GOPATH)/bin/swag

swagger: ## Generate OpenAPI docs from code annotations
	$(SWAG_CMD) init -g cmd/main.go -o docs --parseDependency --parseInternal

tidy: ## Download and tidy dependencies
	go mod tidy

build: ## Build the server binary
	go build -o bin/server ./cmd

run: ## Run the server locally
	go run ./cmd

test: ## Run tests with coverage
	go test ./... -cover

docker-up: ## Start all services via Docker Compose
	docker compose up --build -d

docker-down: ## Stop all Docker Compose services
	docker compose down

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'
