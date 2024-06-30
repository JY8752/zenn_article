package main

import (
	"fmt"
	hellov1 "protovalidate-demo/gen/example/hello/v1"

	"github.com/bufbuild/protovalidate-go"
)

func main() {
	msg := &hellov1.Hello{
		Hello:      "hello",
		NumList:    []int32{5, 10, 15},
		StringList: []string{"banana", "peach", "apple"},
		UserList: []*hellov1.Hello_User{
			{
				Id:      1,
				Name:    "user1",
				Age:     18,
				Country: "Japan",
			},
			{
				Id:      2,
				Name:    "user2",
				Age:     20,
				Country: "America",
			},
			{
				Id:      3,
				Name:    "user3",
				Age:     20,
				Country: "Japan",
			},
		},
	}

	v, err := protovalidate.New()
	if err != nil {
		panic(err)
	}

	if err = v.Validate(msg); err != nil {
		fmt.Println("validation failed:", err)
	} else {
		fmt.Println("validation succeeded")
	}
}
