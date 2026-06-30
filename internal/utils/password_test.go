package utils

import "testing"

func TestHashPasswordAndCheck(t *testing.T) {
	hash, err := HashPassword("mypassword")
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if hash == "" || hash == "mypassword" {
		t.Fatal("expected a non-empty hash different from password")
	}
	if !CheckPassword("mypassword", hash) {
		t.Fatal("expected password to match hash")
	}
}

func TestCheckPassword_WrongPassword(t *testing.T) {
	hash, _ := HashPassword("correct")
	if CheckPassword("wrong", hash) {
		t.Fatal("expected password mismatch")
	}
}
