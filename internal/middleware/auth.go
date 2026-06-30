package middleware

import (
	"context"
	"net/http"
	"strings"

	"practice_1/internal/utils"
)

type contextKey string

const (
	UserIDKey contextKey = "user_id"
	EmailKey  contextKey = "email"
)

func JWTAuth(secret string) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			header := r.Header.Get("Authorization")
			if header == "" {
				utils.WriteError(w, http.StatusUnauthorized, "missing Authorization header")
				return
			}

			parts := strings.SplitN(header, " ", 2)
			if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
				utils.WriteError(w, http.StatusUnauthorized, "invalid Authorization header format")
				return
			}

			claims, err := utils.ParseToken(parts[1], secret)
			if err != nil {
				utils.WriteError(w, http.StatusUnauthorized, "invalid or expired token")
				return
			}

			ctx := context.WithValue(r.Context(), EmailKey, claims.Email)
			ctx = context.WithValue(ctx, UserIDKey, claims.Subject)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}
