package handlers

import (
	"errors"
	"log"
	"net/http"
	"strconv"

	"practice_1/internal/middleware"
	"practice_1/internal/models"
	"practice_1/internal/services"
	"practice_1/internal/utils"

	httpSwagger "github.com/swaggo/http-swagger"
)

type Handlers struct {
	users  *services.UserService
	orders *services.OrderService
	auth   *services.AuthService
}

func New(users *services.UserService, orders *services.OrderService, auth *services.AuthService) *Handlers {
	return &Handlers{users: users, orders: orders, auth: auth}
}

type createUserRequest struct {
	Name     string `json:"name" example:"John Doe"`
	Email    string `json:"email" example:"john.doe@example.com"`
	Age      int    `json:"age" example:"30"`
	Password string `json:"password" example:"securepassword"`
}

type updateUserRequest struct {
	Name  string `json:"name" example:"John Updated"`
	Email string `json:"email" example:"john.updated@example.com"`
	Age   int    `json:"age" example:"31"`
}

type createOrderRequest struct {
	Product  string  `json:"product" example:"Laptop"`
	Quantity int     `json:"quantity" example:"1"`
	Price    float64 `json:"price" example:"1200.50"`
}

type loginRequest struct {
	Email    string `json:"email" example:"john.doe@example.com"`
	Password string `json:"password" example:"securepassword"`
}

type loginResponse struct {
	Token string `json:"token" example:"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."`
}

type listUsersResponse struct {
	Page  int           `json:"page" example:"2"`
	Limit int           `json:"limit" example:"5"`
	Total int64         `json:"total" example:"15"`
	Users []models.User `json:"users"`
}

func (h *Handlers) Routes(jwtSecret string) http.Handler {
	mux := http.NewServeMux()

	mux.HandleFunc("POST /auth/login", h.login)
	mux.HandleFunc("POST /users", h.createUser)

	mux.Handle("GET /users", middleware.JWTAuth(jwtSecret)(http.HandlerFunc(h.listUsers)))
	mux.Handle("GET /users/{id}", middleware.JWTAuth(jwtSecret)(http.HandlerFunc(h.getUser)))
	mux.Handle("PUT /users/{id}", middleware.JWTAuth(jwtSecret)(http.HandlerFunc(h.updateUser)))
	mux.Handle("DELETE /users/{id}", middleware.JWTAuth(jwtSecret)(http.HandlerFunc(h.deleteUser)))

	mux.Handle("POST /users/{user_id}/orders", middleware.JWTAuth(jwtSecret)(http.HandlerFunc(h.createOrder)))
	mux.Handle("GET /users/{user_id}/orders", middleware.JWTAuth(jwtSecret)(http.HandlerFunc(h.listOrders)))

	mux.HandleFunc("GET /health", h.health)
	mux.HandleFunc("GET /openapi.yaml", h.openapi)
	mux.HandleFunc("GET /swagger/", httpSwagger.WrapHandler)
	mux.HandleFunc("GET /swagger", httpSwagger.WrapHandler)

	return mux
}

// @Summary      Login
// @ID           login
// @Description  Authenticate user and receive a JWT token
// @Tags         auth
// @Accept       json
// @Produce      json
// @Param        request body loginRequest true "Credentials"
// @Success      200 {object} loginResponse
// @Failure      401 {object} utils.ErrorResponse
// @Failure      400 {object} utils.ErrorResponse
// @Router       /auth/login [post]
func (h *Handlers) login(w http.ResponseWriter, r *http.Request) {
	var req loginRequest
	if err := utils.DecodeJSON(r, &req); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	token, err := h.auth.Login(r.Context(), req.Email, req.Password)
	if err != nil {
		if errors.Is(err, services.ErrInvalidCredentials) {
			utils.WriteError(w, http.StatusUnauthorized, "invalid email or password")
			return
		}
		log.Printf("login error: %v", err)
		utils.WriteError(w, http.StatusInternalServerError, "internal server error")
		return
	}

	log.Printf("user logged in")
	utils.WriteJSON(w, http.StatusOK, loginResponse{Token: token})
}

// @Summary      Create user
// @ID           createUser
// @Description  Register a new user
// @Tags         users
// @Accept       json
// @Produce      json
// @Param        request body createUserRequest true "User data"
// @Success      201 {object} models.User
// @Failure      400 {object} utils.ErrorResponse
// @Router       /users [post]
func (h *Handlers) createUser(w http.ResponseWriter, r *http.Request) {
	var req createUserRequest
	if err := utils.DecodeJSON(r, &req); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	user, err := h.users.Create(r.Context(), services.CreateUserInput{
		Name:     req.Name,
		Email:    req.Email,
		Age:      req.Age,
		Password: req.Password,
	})
	if err != nil {
		h.writeServiceError(w, err)
		return
	}

	log.Printf("user created id=%d email=%s", user.ID, user.Email)
	utils.WriteJSON(w, http.StatusCreated, user)
}

// @Summary      List users
// @ID           listUsers
// @Description  Get a paginated and filtered list of users
// @Tags         users
// @Produce      json
// @Param        page     query int false "Page number" default(1)
// @Param        limit    query int false "Items per page" default(10)
// @Param        min_age  query int false "Minimum age"
// @Param        max_age  query int false "Maximum age"
// @Success      200 {object} listUsersResponse
// @Failure      400 {object} utils.ErrorResponse
// @Security     BearerAuth
// @Router       /users [get]
func (h *Handlers) listUsers(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	page := parseIntDefault(q.Get("page"), 1)
	limit := parseIntDefault(q.Get("limit"), 10)
	minAge := parseIntPtr(q.Get("min_age"))
	maxAge := parseIntPtr(q.Get("max_age"))

	users, total, err := h.users.List(r.Context(), page, limit, minAge, maxAge)
	if err != nil {
		h.writeServiceError(w, err)
		return
	}

	utils.WriteJSON(w, http.StatusOK, listUsersResponse{
		Page:  page,
		Limit: limit,
		Total: total,
		Users: users,
	})
}

// @Summary      Get user by ID
// @ID           getUser
// @Description  Retrieve a single user
// @Tags         users
// @Produce      json
// @Param        id path int true "User ID"
// @Success      200 {object} models.User
// @Failure      404 {object} utils.ErrorResponse
// @Failure      400 {object} utils.ErrorResponse
// @Security     BearerAuth
// @Router       /users/{id} [get]
func (h *Handlers) getUser(w http.ResponseWriter, r *http.Request) {
	id, ok := parseID(w, r)
	if !ok {
		return
	}

	user, err := h.users.Get(r.Context(), id)
	if err != nil {
		h.writeServiceError(w, err)
		return
	}
	utils.WriteJSON(w, http.StatusOK, user)
}

// @Summary      Update user
// @ID           updateUser
// @Description  Update an existing user
// @Tags         users
// @Accept       json
// @Produce      json
// @Param        id      path int true "User ID"
// @Param        request body updateUserRequest true "Updated user data"
// @Success      200 {object} models.User
// @Failure      404 {object} utils.ErrorResponse
// @Failure      400 {object} utils.ErrorResponse
// @Security     BearerAuth
// @Router       /users/{id} [put]
func (h *Handlers) updateUser(w http.ResponseWriter, r *http.Request) {
	id, ok := parseID(w, r)
	if !ok {
		return
	}

	var req updateUserRequest
	if err := utils.DecodeJSON(r, &req); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	user, err := h.users.Update(r.Context(), id, services.UpdateUserInput{
		Name:  req.Name,
		Email: req.Email,
		Age:   req.Age,
	})
	if err != nil {
		h.writeServiceError(w, err)
		return
	}

	log.Printf("user updated id=%d", user.ID)
	utils.WriteJSON(w, http.StatusOK, user)
}

// @Summary      Delete user
// @ID           deleteUser
// @Description  Delete a user by ID
// @Tags         users
// @Param        id path int true "User ID"
// @Success      204
// @Failure      404 {object} utils.ErrorResponse
// @Failure      400 {object} utils.ErrorResponse
// @Security     BearerAuth
// @Router       /users/{id} [delete]
func (h *Handlers) deleteUser(w http.ResponseWriter, r *http.Request) {
	id, ok := parseID(w, r)
	if !ok {
		return
	}

	if err := h.users.Delete(r.Context(), id); err != nil {
		h.writeServiceError(w, err)
		return
	}

	log.Printf("user deleted id=%d", id)
	w.WriteHeader(http.StatusNoContent)
}

// @Summary      Create order
// @ID           createOrder
// @Description  Create an order for a user
// @Tags         orders
// @Accept       json
// @Produce      json
// @Param        user_id path int true "User ID"
// @Param        request body createOrderRequest true "Order data"
// @Success      201 {object} models.Order
// @Failure      404 {object} utils.ErrorResponse
// @Failure      400 {object} utils.ErrorResponse
// @Security     BearerAuth
// @Router       /users/{user_id}/orders [post]
func (h *Handlers) createOrder(w http.ResponseWriter, r *http.Request) {
	userID, ok := parseUserID(w, r)
	if !ok {
		return
	}

	var req createOrderRequest
	if err := utils.DecodeJSON(r, &req); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	order, err := h.orders.Create(r.Context(), userID, services.CreateOrderInput{
		Product:  req.Product,
		Quantity: req.Quantity,
		Price:    req.Price,
	})
	if err != nil {
		h.writeServiceError(w, err)
		return
	}

	log.Printf("order created id=%d user_id=%d", order.ID, order.UserID)
	utils.WriteJSON(w, http.StatusCreated, order)
}

// @Summary      List orders
// @ID           listOrders
// @Description  Get all orders for a user
// @Tags         orders
// @Produce      json
// @Param        user_id path int true "User ID"
// @Success      200 {array} models.Order
// @Failure      404 {object} utils.ErrorResponse
// @Failure      400 {object} utils.ErrorResponse
// @Security     BearerAuth
// @Router       /users/{user_id}/orders [get]
func (h *Handlers) listOrders(w http.ResponseWriter, r *http.Request) {
	userID, ok := parseUserID(w, r)
	if !ok {
		return
	}

	orders, err := h.orders.ListByUserID(r.Context(), userID)
	if err != nil {
		h.writeServiceError(w, err)
		return
	}

	if orders == nil {
		orders = []models.Order{}
	}
	utils.WriteJSON(w, http.StatusOK, orders)
}

func (h *Handlers) health(w http.ResponseWriter, r *http.Request) {
	utils.WriteJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func (h *Handlers) openapi(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/yaml")
	http.ServeFile(w, r, "docs/swagger.yaml")
}

func (h *Handlers) writeServiceError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, services.ErrNotFound):
		utils.WriteError(w, http.StatusNotFound, "not found")
	case errors.Is(err, services.ErrDuplicateEmail):
		utils.WriteError(w, http.StatusBadRequest, "user with this email already exists")
	case errors.Is(err, services.ErrInvalidInput):
		utils.WriteError(w, http.StatusBadRequest, "invalid input")
	case errors.Is(err, services.ErrInvalidCredentials):
		utils.WriteError(w, http.StatusUnauthorized, "invalid email or password")
	default:
		log.Printf("internal error: %v", err)
		utils.WriteError(w, http.StatusInternalServerError, "internal server error")
	}
}

func parseID(w http.ResponseWriter, r *http.Request) (uint, bool) {
	idStr := r.PathValue("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil || id == 0 {
		utils.WriteError(w, http.StatusBadRequest, "invalid id")
		return 0, false
	}
	return uint(id), true
}

func parseUserID(w http.ResponseWriter, r *http.Request) (uint, bool) {
	idStr := r.PathValue("user_id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil || id == 0 {
		utils.WriteError(w, http.StatusBadRequest, "invalid user_id")
		return 0, false
	}
	return uint(id), true
}

func parseIntDefault(value string, fallback int) int {
	if value == "" {
		return fallback
	}
	parsed, err := strconv.Atoi(value)
	if err != nil || parsed <= 0 {
		return fallback
	}
	return parsed
}

func parseIntPtr(value string) *int {
	if value == "" {
		return nil
	}
	parsed, err := strconv.Atoi(value)
	if err != nil || parsed < 0 {
		return nil
	}
	return &parsed
}
