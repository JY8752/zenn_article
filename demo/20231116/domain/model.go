package domain

import (
	"encoding/json"
	"fmt"
)

type primitive interface {
	~int | ~int8 | ~int16 | ~int32 | ~int64 |
		~uint | ~uint8 | ~uint16 | ~uint32 | ~uint64 |
		~float32 | ~float64 |
		~bool |
		~string
}

type ValueObject[T primitive] struct {
	value T
}

func (v ValueObject[T]) Value() T {
	return v.value
}

func (v ValueObject[T]) String() string {
	return fmt.Sprintf("%v", v.value)
}

func (v ValueObject[T]) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

type UserId struct {
	ValueObject[int64]
}

func NewUserId(value int64) UserId {
	return UserId{ValueObject[int64]{value}}
}

type UserName struct {
	ValueObject[string]
}

func NewUserName(value string) UserName {
	return UserName{ValueObject[string]{value}}
}

type User struct {
	Id   UserId   `json:"user_id"`
	Name UserName `json:"name"`
}

// type TestStruct struct {
// 	Field1 string
// 	Field2 string
// }

// type TestValue struct {
// 	ValueObject[TestStruct]
// }

// func NewTestValue(value TestStruct) TestValue {
// 	return TestValue{ValueObject[TestStruct]{value}}
// }
