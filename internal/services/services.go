package services

import (
	"context"
	"errors"
	"strings"
	"time"

	"practice_1/internal/models"
	"practice_1/internal/repository"
	"practice_1/internal/utils"
)

var (
	ErrDuplicateEmail     = errors.New("user with this email already exists")
	ErrInvalidCredentials = errors.New("invalid email or password")
	ErrInvalidInput       = errors.New("invalid input")
	ErrNotFound           = repository.ErrNotFound
)

type UserService struct {
	repo Store
}

type OrderService struct {
	repo Store
}

type AuthService struct {
	repo      Store
	jwtSecret string
	tokenTTL  time.Duration
}

type CreateUserInput struct {
	Name     string
	Email    string
	Age      int
	Password string
}

type UpdateUserInput struct {
	Name  string
	Email string
	Age   int
}

type CreateOrderInput struct {
	Product  string
	Quantity int
	Price    float64
}

func NewUserService(repo Store) *UserService {
	return &UserService{repo: repo}
}

func NewOrderService(repo Store) *OrderService {
	return &OrderService{repo: repo}
}

func NewAuthService(repo Store, jwtSecret string, tokenTTL time.Duration) *AuthService {
	return &AuthService{repo: repo, jwtSecret: jwtSecret, tokenTTL: tokenTTL}
}

func (s *UserService) Create(ctx context.Context, input CreateUserInput) (*models.User, error) {
	input.Name = strings.TrimSpace(input.Name)
	input.Email = normalizeEmail(input.Email)
	if input.Name == "" || input.Email == "" || input.Password == "" || input.Age <= 0 {
		return nil, ErrInvalidInput
	}

	if _, err := s.repo.GetUserByEmail(ctx, input.Email); err == nil {
		return nil, ErrDuplicateEmail
	} else if !errors.Is(err, repository.ErrNotFound) {
		return nil, err
	}

	passwordHash, err := utils.HashPassword(input.Password)
	if err != nil {
		return nil, err
	}

	user := &models.User{
		Name:         input.Name,
		Email:        input.Email,
		Age:          input.Age,
		PasswordHash: passwordHash,
	}
	if err := s.repo.CreateUser(ctx, user); err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return nil, ErrDuplicateEmail
		}
		return nil, err
	}
	return user, nil
}

func (s *UserService) List(ctx context.Context, page, limit int, minAge, maxAge *int) ([]models.User, int64, error) {
	if page <= 0 {
		page = 1
	}
	if limit <= 0 {
		limit = 10
	}
	if limit > 100 {
		limit = 100
	}
	if minAge != nil && *minAge < 0 || maxAge != nil && *maxAge < 0 {
		return nil, 0, ErrInvalidInput
	}
	if minAge != nil && maxAge != nil && *minAge > *maxAge {
		return nil, 0, ErrInvalidInput
	}
	return s.repo.ListUsers(ctx, page, limit, minAge, maxAge)
}

func (s *UserService) Get(ctx context.Context, id uint) (*models.User, error) {
	return s.repo.GetUserByID(ctx, id)
}

func (s *UserService) Update(ctx context.Context, id uint, input UpdateUserInput) (*models.User, error) {
	input.Name = strings.TrimSpace(input.Name)
	input.Email = normalizeEmail(input.Email)
	if input.Name == "" || input.Email == "" || input.Age <= 0 {
		return nil, ErrInvalidInput
	}

	user, err := s.repo.GetUserByID(ctx, id)
	if err != nil {
		return nil, err
	}

	if user.Email != input.Email {
		existing, err := s.repo.GetUserByEmail(ctx, input.Email)
		if err == nil && existing.ID != user.ID {
			return nil, ErrDuplicateEmail
		}
		if err != nil && !errors.Is(err, repository.ErrNotFound) {
			return nil, err
		}
	}

	user.Name = input.Name
	user.Email = input.Email
	user.Age = input.Age
	if err := s.repo.UpdateUser(ctx, user); err != nil {
		return nil, err
	}
	return user, nil
}

func (s *UserService) Delete(ctx context.Context, id uint) error {
	return s.repo.DeleteUser(ctx, id)
}

func (s *OrderService) Create(ctx context.Context, userID uint, input CreateOrderInput) (*models.Order, error) {
	input.Product = strings.TrimSpace(input.Product)
	if input.Product == "" || input.Quantity <= 0 || input.Price <= 0 {
		return nil, ErrInvalidInput
	}
	if _, err := s.repo.GetUserByID(ctx, userID); err != nil {
		return nil, err
	}

	order := &models.Order{
		UserID:   userID,
		Product:  input.Product,
		Quantity: input.Quantity,
		Price:    input.Price,
	}
	if err := s.repo.CreateOrder(ctx, order); err != nil {
		return nil, err
	}
	return order, nil
}

func (s *OrderService) ListByUserID(ctx context.Context, userID uint) ([]models.Order, error) {
	if _, err := s.repo.GetUserByID(ctx, userID); err != nil {
		return nil, err
	}
	return s.repo.ListOrdersByUserID(ctx, userID)
}

func (s *AuthService) Login(ctx context.Context, email, password string) (string, error) {
	user, err := s.repo.GetUserByEmail(ctx, normalizeEmail(email))
	if errors.Is(err, repository.ErrNotFound) {
		return "", ErrInvalidCredentials
	}
	if err != nil {
		return "", err
	}
	if !utils.CheckPassword(password, user.PasswordHash) {
		return "", ErrInvalidCredentials
	}
	return utils.GenerateToken(user.ID, user.Email, s.jwtSecret, s.tokenTTL)
}

func normalizeEmail(email string) string {
	return strings.ToLower(strings.TrimSpace(email))
}
