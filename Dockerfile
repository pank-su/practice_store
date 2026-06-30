FROM golang:1.22-alpine AS builder

RUN apk add --no-cache git

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN go install github.com/swaggo/swag/cmd/swag@latest
RUN $(go env GOPATH)/bin/swag init -g cmd/main.go -o docs --parseDependency --parseInternal

RUN CGO_ENABLED=0 GOOS=linux go build -o /app/bin/server ./cmd

FROM alpine:3.20

RUN apk add --no-cache ca-certificates

WORKDIR /app

COPY --from=builder /app/bin/server /app/server
COPY --from=builder /app/docs /app/docs
COPY --from=builder /app/migrations /app/migrations

EXPOSE 8080

CMD ["/app/server"]
