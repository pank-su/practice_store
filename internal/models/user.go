package models

type User struct {
	ID           uint   `gorm:"primaryKey" json:"id"`
	Name         string `gorm:"type:varchar(255);not null" json:"name"`
	Email        string `gorm:"type:varchar(255);uniqueIndex;not null" json:"email"`
	Age          int    `gorm:"not null" json:"age"`
	PasswordHash string `gorm:"type:varchar(255);not null" json:"-"`
}
