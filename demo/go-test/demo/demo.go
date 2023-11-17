package demo

import "database/sql"

type UserController struct {
	us UserService
}

type UserService interface {}

type userService struct {
	ur UserRepository
}

type UserRepository interface {}

type userRepository struct {
	db *sql.DB
}