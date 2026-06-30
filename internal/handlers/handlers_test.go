package handlers

import (
	"bytes"
	"encoding/json"
	"io"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"testing"
	"time"

	"practice_1/internal/services"
	"practice_1/internal/testutil"
	"practice_1/internal/utils"
)

const testSecret = "test-secret"

func newTestHandlers(t *testing.T) (*Handlers, string) {
	t.Helper()
	store := testutil.NewMockStore()
	users := services.NewUserService(store)
	orders := services.NewOrderService(store)
	auth := services.NewAuthService(store, testSecret, time.Hour)
	return New(users, orders, auth), testSecret
}

func doRequest(t *testing.T, h http.Handler, method, target, body string, token string) *httptest.ResponseRecorder {
	t.Helper()
	var bodyReader io.Reader
	if body != "" {
		bodyReader = bytes.NewBufferString(body)
	}
	req := httptest.NewRequest(method, target, bodyReader)
	req.Header.Set("Content-Type", "application/json")
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, req)
	return rec
}

func TestCreateUser_Success(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)

	body := `{"name":"John","email":"john@example.com","age":30,"password":"secret"}`
	rec := doRequest(t, routes, http.MethodPost, "/users", body, "")

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d: %s", rec.Code, rec.Body.String())
	}
	var resp map[string]any
	json.Unmarshal(rec.Body.Bytes(), &resp)
	if resp["email"] != "john@example.com" {
		t.Fatalf("unexpected email: %v", resp["email"])
	}
	if _, ok := resp["password_hash"]; ok {
		t.Fatal("password_hash should not be in response")
	}
}

func TestCreateUser_DuplicateEmail(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)

	body := `{"name":"John","email":"john@example.com","age":30,"password":"secret"}`
	doRequest(t, routes, http.MethodPost, "/users", body, "")
	rec := doRequest(t, routes, http.MethodPost, "/users", body, "")

	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", rec.Code)
	}
}

func TestCreateUser_InvalidJSON(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)

	rec := doRequest(t, routes, http.MethodPost, "/users", "{bad json", "")
	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", rec.Code)
	}
}

func TestLogin_Success(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)

	doRequest(t, routes, http.MethodPost, "/users",
		`{"name":"John","email":"john@example.com","age":30,"password":"secret"}`, "")

	rec := doRequest(t, routes, http.MethodPost, "/auth/login",
		`{"email":"john@example.com","password":"secret"}`, "")

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d: %s", rec.Code, rec.Body.String())
	}
	var resp map[string]any
	json.Unmarshal(rec.Body.Bytes(), &resp)
	if resp["token"] == nil || resp["token"] == "" {
		t.Fatal("expected non-empty token")
	}
}

func TestLogin_WrongPassword(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)

	doRequest(t, routes, http.MethodPost, "/users",
		`{"name":"John","email":"john@example.com","age":30,"password":"secret"}`, "")

	rec := doRequest(t, routes, http.MethodPost, "/auth/login",
		`{"email":"john@example.com","password":"wrong"}`, "")

	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", rec.Code)
	}
}

func TestProtectedRoutes_RequireAuth(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)

	endpoints := []struct {
		method, path string
	}{
		{http.MethodGet, "/users"},
		{http.MethodGet, "/users/1"},
		{http.MethodPut, "/users/1"},
		{http.MethodDelete, "/users/1"},
		{http.MethodPost, "/users/1/orders"},
		{http.MethodGet, "/users/1/orders"},
	}
	for _, e := range endpoints {
		rec := doRequest(t, routes, e.method, e.path, "", "")
		if rec.Code != http.StatusUnauthorized {
			t.Fatalf("%s %s: expected 401, got %d", e.method, e.path, rec.Code)
		}
	}
}

func TestGetUser_NotFound(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)
	token := makeToken(t, 1, "a@b.com", secret)

	rec := doRequest(t, routes, http.MethodGet, "/users/999", "", token)
	if rec.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", rec.Code)
	}
}

func TestGetUser_InvalidID(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)
	token := makeToken(t, 1, "a@b.com", secret)

	rec := doRequest(t, routes, http.MethodGet, "/users/abc", "", token)
	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", rec.Code)
	}
}

func TestUpdateUser_Success(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)
	token := makeToken(t, 1, "john@example.com", secret)

	rec := doRequest(t, routes, http.MethodPost, "/users",
		`{"name":"John","email":"john@example.com","age":30,"password":"secret"}`, "")
	var created map[string]any
	json.Unmarshal(rec.Body.Bytes(), &created)
	id := created["id"]

	updateBody := `{"name":"John Updated","email":"john.updated@example.com","age":31}`
	rec = doRequest(t, routes, http.MethodPut, "/users/"+itoa(id), updateBody, token)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d: %s", rec.Code, rec.Body.String())
	}
}

func TestDeleteUser_Success(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)
	token := makeToken(t, 1, "john@example.com", secret)

	rec := doRequest(t, routes, http.MethodPost, "/users",
		`{"name":"John","email":"john@example.com","age":30,"password":"secret"}`, "")
	var created map[string]any
	json.Unmarshal(rec.Body.Bytes(), &created)
	id := created["id"]

	rec = doRequest(t, routes, http.MethodDelete, "/users/"+itoa(id), "", token)
	if rec.Code != http.StatusNoContent {
		t.Fatalf("expected 204, got %d", rec.Code)
	}
}

func TestCreateOrder_Success(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)
	token := makeToken(t, 1, "john@example.com", secret)

	rec := doRequest(t, routes, http.MethodPost, "/users",
		`{"name":"John","email":"john@example.com","age":30,"password":"secret"}`, "")
	var created map[string]any
	json.Unmarshal(rec.Body.Bytes(), &created)
	id := created["id"]

	orderBody := `{"product":"Laptop","quantity":1,"price":1200.50}`
	rec = doRequest(t, routes, http.MethodPost, "/users/"+itoa(id)+"/orders", orderBody, token)
	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d: %s", rec.Code, rec.Body.String())
	}
	var order map[string]any
	json.Unmarshal(rec.Body.Bytes(), &order)
	if order["product"] != "Laptop" {
		t.Fatalf("unexpected product: %v", order["product"])
	}
}

func TestListOrders_Success(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)
	token := makeToken(t, 1, "john@example.com", secret)

	rec := doRequest(t, routes, http.MethodPost, "/users",
		`{"name":"John","email":"john@example.com","age":30,"password":"secret"}`, "")
	var created map[string]any
	json.Unmarshal(rec.Body.Bytes(), &created)
	id := created["id"]

	doRequest(t, routes, http.MethodPost, "/users/"+itoa(id)+"/orders",
		`{"product":"A","quantity":1,"price":10}`, token)
	doRequest(t, routes, http.MethodPost, "/users/"+itoa(id)+"/orders",
		`{"product":"B","quantity":2,"price":20}`, token)

	rec = doRequest(t, routes, http.MethodGet, "/users/"+itoa(id)+"/orders", "", token)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", rec.Code)
	}
	body := strings.TrimSpace(rec.Body.String())
	if !strings.HasPrefix(body, "[") {
		t.Fatalf("expected JSON array, got: %s", body)
	}
}

func TestListUsers_Pagination(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)
	token := makeToken(t, 1, "a@b.com", secret)

	for i := 0; i < 12; i++ {
		doRequest(t, routes, http.MethodPost, "/users",
			`{"name":"User`+itoa(i)+`","email":"u`+itoa(i)+`@example.com","age":`+itoa(20+i)+`,"password":"p"}`, "")
	}

	rec := doRequest(t, routes, http.MethodGet, "/users?page=2&limit=5", "", token)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", rec.Code)
	}
	var resp map[string]any
	json.Unmarshal(rec.Body.Bytes(), &resp)
	if resp["total"].(float64) != 12 {
		t.Fatalf("expected total 12, got %v", resp["total"])
	}
	if resp["page"].(float64) != 2 {
		t.Fatalf("expected page 2, got %v", resp["page"])
	}
}

func TestHealth(t *testing.T) {
	h, secret := newTestHandlers(t)
	routes := h.Routes(secret)

	rec := doRequest(t, routes, http.MethodGet, "/health", "", "")
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", rec.Code)
	}
}

func makeToken(t *testing.T, userID uint, email, secret string) string {
	t.Helper()
	token, err := utils.GenerateToken(userID, email, secret, time.Hour)
	if err != nil {
		t.Fatalf("failed to generate token: %v", err)
	}
	return token
}

func itoa(v any) string {
	switch val := v.(type) {
	case float64:
		return strconv.Itoa(int(val))
	case int:
		return strconv.Itoa(val)
	}
	return ""
}
