package domain

type Rarity string

const (
	N  = Rarity("N")
	R  = Rarity("R")
	SR = Rarity("SR")
)

type Item struct {
	Id     int64  `json:"item_id"`
	Name   string `json:"name"`
	Rarity Rarity `json:"rarity"`
}

func NewItem(id int64, name string, rarity Rarity) *Item {
	return &Item{id, name, rarity}
}
