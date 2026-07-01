package models

type User struct {
	ID           uint   `gorm:"primaryKey" json:"id"`
	Name         string `gorm:"type:varchar(255);not null;check:users_name_not_blank,length(trim(name)) > 0" json:"name"`
	Email        string `gorm:"type:varchar(255);uniqueIndex;not null;check:users_email_not_blank,length(trim(email)) > 0" json:"email"`
	Age          int    `gorm:"not null;check:users_age_positive,age > 0" json:"age"`
	PasswordHash string `gorm:"type:varchar(255);not null" json:"-"`
}
