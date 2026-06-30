package testutil

import (
	"context"
	"sync"

	"practice_1/internal/models"
	"practice_1/internal/repository"
)

type MockStore struct {
	mu     sync.Mutex
	users  map[uint]*models.User
	orders map[uint]*models.Order
	nextID uint
}

func NewMockStore() *MockStore {
	return &MockStore{
		users:  make(map[uint]*models.User),
		orders: make(map[uint]*models.Order),
		nextID: 1,
	}
}

func (m *MockStore) CreateUser(ctx context.Context, user *models.User) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	for _, u := range m.users {
		if u.Email == user.Email {
			return repository.ErrNotFound
		}
	}
	user.ID = m.nextID
	m.nextID++
	m.users[user.ID] = user
	return nil
}

func (m *MockStore) GetUserByID(ctx context.Context, id uint) (*models.User, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	user, ok := m.users[id]
	if !ok {
		return nil, repository.ErrNotFound
	}
	cp := *user
	return &cp, nil
}

func (m *MockStore) GetUserByEmail(ctx context.Context, email string) (*models.User, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	for _, u := range m.users {
		if u.Email == email {
			cp := *u
			return &cp, nil
		}
	}
	return nil, repository.ErrNotFound
}

func (m *MockStore) ListUsers(ctx context.Context, page, limit int, minAge, maxAge *int) ([]models.User, int64, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	var filtered []models.User
	for _, u := range m.users {
		if minAge != nil && u.Age < *minAge {
			continue
		}
		if maxAge != nil && u.Age > *maxAge {
			continue
		}
		filtered = append(filtered, *u)
	}
	total := int64(len(filtered))
	offset := (page - 1) * limit
	if offset >= len(filtered) {
		return []models.User{}, total, nil
	}
	end := offset + limit
	if end > len(filtered) {
		end = len(filtered)
	}
	return filtered[offset:end], total, nil
}

func (m *MockStore) UpdateUser(ctx context.Context, user *models.User) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if _, ok := m.users[user.ID]; !ok {
		return repository.ErrNotFound
	}
	m.users[user.ID] = user
	return nil
}

func (m *MockStore) DeleteUser(ctx context.Context, id uint) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if _, ok := m.users[id]; !ok {
		return repository.ErrNotFound
	}
	delete(m.users, id)
	for oid, o := range m.orders {
		if o.UserID == id {
			delete(m.orders, oid)
		}
	}
	return nil
}

func (m *MockStore) CreateOrder(ctx context.Context, order *models.Order) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if _, ok := m.users[order.UserID]; !ok {
		return repository.ErrNotFound
	}
	order.ID = m.nextID
	m.nextID++
	m.orders[order.ID] = order
	return nil
}

func (m *MockStore) ListOrdersByUserID(ctx context.Context, userID uint) ([]models.Order, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	var orders []models.Order
	for _, o := range m.orders {
		if o.UserID == userID {
			orders = append(orders, *o)
		}
	}
	return orders, nil
}
