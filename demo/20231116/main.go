package main

import (
	"encoding/json"
	"log"
	"net/http"
	"test-value-object/domain"
)

func main() {
	ui := domain.NewUserId(1)
	name := domain.NewUserName("user")
	user := domain.User{Id: ui, Name: name}

	http.HandleFunc("/user", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(user)
	})
	log.Fatal(http.ListenAndServe(":8080", nil))
}
