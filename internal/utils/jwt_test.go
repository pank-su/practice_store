package utils

import (
	"testing"
	"time"
)

func TestGenerateAndParseToken(t *testing.T) {
	secret := "test-secret"
	token, err := GenerateToken(42, "user@example.com", secret, time.Hour)
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if token == "" {
		t.Fatal("expected non-empty token")
	}

	claims, err := ParseToken(token, secret)
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if claims.Email != "user@example.com" {
		t.Fatalf("expected email user@example.com, got %s", claims.Email)
	}
	if claims.Subject != "42" {
		t.Fatalf("expected subject 42, got %s", claims.Subject)
	}
}

func TestParseToken_InvalidSecret(t *testing.T) {
	token, _ := GenerateToken(1, "a@b.com", "secret1", time.Hour)
	if _, err := ParseToken(token, "secret2"); err == nil {
		t.Fatal("expected error with wrong secret")
	}
}

func TestParseToken_ExpiredToken(t *testing.T) {
	token, _ := GenerateToken(1, "a@b.com", "secret", -time.Hour)
	if _, err := ParseToken(token, "secret"); err == nil {
		t.Fatal("expected error for expired token")
	}
}
