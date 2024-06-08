package main

import (
	"fmt"
	hellov1 "protovalidate-demo/gen/example/hello/v1"

	"github.com/bufbuild/protovalidate-go"
)

func main() {
	msg := &hellov1.Hello{
		Hello: "",
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
