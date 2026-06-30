package services

import (
	"context"

	"practice_1/internal/models"
)

type Store interface {
	CreateUser(ctx context.Context, user *models.User) error
	GetUserByID(ctx context.Context, id uint) (*models.User, error)
	GetUserByEmail(ctx context.Context, email string) (*models.User, error)
	ListUsers(ctx context.Context, page, limit int, minAge, maxAge *int) ([]models.User, int64, error)
	UpdateUser(ctx context.Context, user *models.User) error
	DeleteUser(ctx context.Context, id uint) error
	CreateOrder(ctx context.Context, order *models.Order) error
	ListOrdersByUserID(ctx context.Context, userID uint) ([]models.Order, error)
}
