package repository

import (
	"context"
	"errors"

	"practice_1/internal/models"

	"gorm.io/gorm"
)

var ErrNotFound = errors.New("not found")

type Repository struct {
	db *gorm.DB
}

func New(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

func (r *Repository) Migrate(ctx context.Context) error {
	return r.db.WithContext(ctx).AutoMigrate(&models.User{}, &models.Order{})
}

func (r *Repository) CreateUser(ctx context.Context, user *models.User) error {
	return r.db.WithContext(ctx).Create(user).Error
}

func (r *Repository) GetUserByID(ctx context.Context, id uint) (*models.User, error) {
	var user models.User
	err := r.db.WithContext(ctx).First(&user, id).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &user, nil
}

func (r *Repository) GetUserByEmail(ctx context.Context, email string) (*models.User, error) {
	var user models.User
	err := r.db.WithContext(ctx).Where("email = ?", email).First(&user).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &user, nil
}

func (r *Repository) ListUsers(ctx context.Context, page, limit int, minAge, maxAge *int) ([]models.User, int64, error) {
	query := r.db.WithContext(ctx).Model(&models.User{})
	if minAge != nil {
		query = query.Where("age >= ?", *minAge)
	}
	if maxAge != nil {
		query = query.Where("age <= ?", *maxAge)
	}

	var total int64
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	var users []models.User
	offset := (page - 1) * limit
	if err := query.Order("id ASC").Limit(limit).Offset(offset).Find(&users).Error; err != nil {
		return nil, 0, err
	}
	return users, total, nil
}

func (r *Repository) UpdateUser(ctx context.Context, user *models.User) error {
	return r.db.WithContext(ctx).Save(user).Error
}

func (r *Repository) DeleteUser(ctx context.Context, id uint) error {
	result := r.db.WithContext(ctx).Delete(&models.User{}, id)
	if result.Error != nil {
		return result.Error
	}
	if result.RowsAffected == 0 {
		return ErrNotFound
	}
	return nil
}

func (r *Repository) CreateOrder(ctx context.Context, order *models.Order) error {
	return r.db.WithContext(ctx).Create(order).Error
}

func (r *Repository) ListOrdersByUserID(ctx context.Context, userID uint) ([]models.Order, error) {
	var orders []models.Order
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Order("created_at DESC, id DESC").Find(&orders).Error
	if err != nil {
		return nil, err
	}
	return orders, nil
}
