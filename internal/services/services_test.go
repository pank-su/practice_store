package services

import (
	"context"
	"encoding/json"
	"strconv"
	"strings"
	"testing"

	"practice_1/internal/models"
	"practice_1/internal/testutil"
)

func TestUserService_Create(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	user, err := svc.Create(context.Background(), CreateUserInput{
		Name: "John Doe", Email: "john@example.com", Age: 30, Password: "secret",
	})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if user.ID == 0 {
		t.Fatal("expected non-zero ID")
	}
	if user.PasswordHash == "secret" {
		t.Fatal("password should be hashed")
	}

	_, err = svc.Create(context.Background(), CreateUserInput{
		Name: "Jane", Email: "john@example.com", Age: 25, Password: "secret",
	})
	if err != ErrDuplicateEmail {
		t.Fatalf("expected ErrDuplicateEmail, got %v", err)
	}
}

func TestUserService_Create_InvalidInput(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	cases := []CreateUserInput{
		{Name: "", Email: "a@b.com", Age: 30, Password: "secret"},
		{Name: "John", Email: "", Age: 30, Password: "secret"},
		{Name: "John", Email: "a@b.com", Age: 0, Password: "secret"},
		{Name: "John", Email: "a@b.com", Age: 30, Password: ""},
	}
	for _, c := range cases {
		if _, err := svc.Create(context.Background(), c); err != ErrInvalidInput {
			t.Fatalf("expected ErrInvalidInput for %+v, got %v", c, err)
		}
	}
}

func TestUserService_Get_NotFound(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	_, err := svc.Get(context.Background(), 999)
	if err != ErrNotFound {
		t.Fatalf("expected ErrNotFound, got %v", err)
	}
}

func TestUserService_Update(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	user, _ := svc.Create(context.Background(), CreateUserInput{
		Name: "John", Email: "john@example.com", Age: 30, Password: "secret",
	})

	updated, err := svc.Update(context.Background(), user.ID, UpdateUserInput{
		Name: "John Updated", Email: "john.updated@example.com", Age: 31,
	})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if updated.Name != "John Updated" || updated.Age != 31 {
		t.Fatalf("unexpected updated user: %+v", updated)
	}
}

func TestUserService_Update_DuplicateEmail(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	svc.Create(context.Background(), CreateUserInput{Name: "A", Email: "a@example.com", Age: 20, Password: "p"})
	user2, _ := svc.Create(context.Background(), CreateUserInput{Name: "B", Email: "b@example.com", Age: 20, Password: "p"})

	_, err := svc.Update(context.Background(), user2.ID, UpdateUserInput{
		Name: "B", Email: "a@example.com", Age: 20,
	})
	if err != ErrDuplicateEmail {
		t.Fatalf("expected ErrDuplicateEmail, got %v", err)
	}
}

func TestUserService_Delete(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	user, _ := svc.Create(context.Background(), CreateUserInput{
		Name: "John", Email: "john@example.com", Age: 30, Password: "secret",
	})

	if err := svc.Delete(context.Background(), user.ID); err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if err := svc.Delete(context.Background(), user.ID); err != ErrNotFound {
		t.Fatalf("expected ErrNotFound on second delete, got %v", err)
	}
}

func TestUserService_List_Pagination(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	for i := 0; i < 15; i++ {
		svc.Create(context.Background(), CreateUserInput{
			Name: "User", Email: "user" + strconv.Itoa(i) + "@example.com", Age: 20 + i, Password: "p",
		})
	}

	users, total, err := svc.List(context.Background(), 2, 5, nil, nil)
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if total != 15 {
		t.Fatalf("expected total 15, got %d", total)
	}
	if len(users) != 5 {
		t.Fatalf("expected 5 users on page 2, got %d", len(users))
	}
}

func TestUserService_List_AgeFilter(t *testing.T) {
	store := testutil.NewMockStore()
	svc := NewUserService(store)

	for i := 0; i < 10; i++ {
		svc.Create(context.Background(), CreateUserInput{
			Name: "User", Email: "user" + strconv.Itoa(i) + "@example.com", Age: 20 + i, Password: "p",
		})
	}

	minAge, maxAge := 22, 25
	users, total, err := svc.List(context.Background(), 1, 10, &minAge, &maxAge)
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if total != 4 {
		t.Fatalf("expected total 4, got %d", total)
	}
	for _, u := range users {
		if u.Age < minAge || u.Age > maxAge {
			t.Fatalf("user age %d out of range", u.Age)
		}
	}
}

func TestOrderService_Create(t *testing.T) {
	store := testutil.NewMockStore()
	userSvc := NewUserService(store)
	orderSvc := NewOrderService(store)

	user, _ := userSvc.Create(context.Background(), CreateUserInput{
		Name: "John", Email: "john@example.com", Age: 30, Password: "secret",
	})

	order, err := orderSvc.Create(context.Background(), user.ID, CreateOrderInput{
		Product: "Laptop", Quantity: 1, Price: 1200.50,
	})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if order.UserID != user.ID || order.Product != "Laptop" {
		t.Fatalf("unexpected order: %+v", order)
	}
}

func TestOrderService_Create_UserNotFound(t *testing.T) {
	store := testutil.NewMockStore()
	orderSvc := NewOrderService(store)

	_, err := orderSvc.Create(context.Background(), 999, CreateOrderInput{
		Product: "Laptop", Quantity: 1, Price: 1200.50,
	})
	if err != ErrNotFound {
		t.Fatalf("expected ErrNotFound, got %v", err)
	}
}

func TestOrderService_Create_InvalidInput(t *testing.T) {
	store := testutil.NewMockStore()
	userSvc := NewUserService(store)
	orderSvc := NewOrderService(store)

	user, _ := userSvc.Create(context.Background(), CreateUserInput{
		Name: "John", Email: "john@example.com", Age: 30, Password: "secret",
	})

	cases := []CreateOrderInput{
		{Product: "", Quantity: 1, Price: 10},
		{Product: "Laptop", Quantity: 0, Price: 10},
		{Product: "Laptop", Quantity: 1, Price: 0},
	}
	for _, c := range cases {
		if _, err := orderSvc.Create(context.Background(), user.ID, c); err != ErrInvalidInput {
			t.Fatalf("expected ErrInvalidInput for %+v, got %v", c, err)
		}
	}
}

func TestOrderService_ListByUserID(t *testing.T) {
	store := testutil.NewMockStore()
	userSvc := NewUserService(store)
	orderSvc := NewOrderService(store)

	user, _ := userSvc.Create(context.Background(), CreateUserInput{
		Name: "John", Email: "john@example.com", Age: 30, Password: "secret",
	})
	orderSvc.Create(context.Background(), user.ID, CreateOrderInput{Product: "A", Quantity: 1, Price: 10})
	orderSvc.Create(context.Background(), user.ID, CreateOrderInput{Product: "B", Quantity: 2, Price: 20})

	orders, err := orderSvc.ListByUserID(context.Background(), user.ID)
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if len(orders) != 2 {
		t.Fatalf("expected 2 orders, got %d", len(orders))
	}
}

func TestAuthService_Login_Success(t *testing.T) {
	store := testutil.NewMockStore()
	userSvc := NewUserService(store)
	authSvc := NewAuthService(store, "test-secret", 3600)

	userSvc.Create(context.Background(), CreateUserInput{
		Name: "John", Email: "john@example.com", Age: 30, Password: "secret",
	})

	token, err := authSvc.Login(context.Background(), "john@example.com", "secret")
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if token == "" {
		t.Fatal("expected non-empty token")
	}
}

func TestAuthService_Login_WrongPassword(t *testing.T) {
	store := testutil.NewMockStore()
	userSvc := NewUserService(store)
	authSvc := NewAuthService(store, "test-secret", 3600)

	userSvc.Create(context.Background(), CreateUserInput{
		Name: "John", Email: "john@example.com", Age: 30, Password: "secret",
	})

	_, err := authSvc.Login(context.Background(), "john@example.com", "wrong")
	if err != ErrInvalidCredentials {
		t.Fatalf("expected ErrInvalidCredentials, got %v", err)
	}
}

func TestAuthService_Login_UserNotFound(t *testing.T) {
	store := testutil.NewMockStore()
	authSvc := NewAuthService(store, "test-secret", 3600)

	_, err := authSvc.Login(context.Background(), "nobody@example.com", "secret")
	if err != ErrInvalidCredentials {
		t.Fatalf("expected ErrInvalidCredentials, got %v", err)
	}
}

func TestModels_User_JSONExcludesPassword(t *testing.T) {
	user := models.User{
		ID: 1, Name: "John", Email: "john@example.com", Age: 30, PasswordHash: "hash",
	}
	data, err := json.Marshal(user)
	if err != nil {
		t.Fatalf("marshal error: %v", err)
	}
	if strings.Contains(string(data), "hash") {
		t.Fatal("password_hash should not be serialized")
	}
	if !strings.Contains(string(data), `"id":1`) {
		t.Fatal("id should be present in JSON")
	}
}
