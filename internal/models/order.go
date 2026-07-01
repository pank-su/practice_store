package models

import "time"

type Order struct {
	ID        uint      `gorm:"primaryKey" json:"id"`
	UserID    uint      `gorm:"not null;index" json:"user_id"`
	Product   string    `gorm:"type:varchar(255);not null;check:orders_product_not_blank,length(trim(product)) > 0" json:"product"`
	Quantity  int       `gorm:"not null;check:orders_quantity_positive,quantity > 0" json:"quantity"`
	Price     float64   `gorm:"type:decimal(10,2);not null;check:orders_price_positive,price > 0" json:"price"`
	CreatedAt time.Time `gorm:"autoCreateTime" json:"created_at"`
	User      User      `gorm:"constraint:OnDelete:CASCADE" json:"-"`
}
