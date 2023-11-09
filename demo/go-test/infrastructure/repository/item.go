package repository

import (
	"context"
	"database/sql"

	"github.com/JY8752/go-unittest-architecture/db"
	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/volatiletech/sqlboiler/v4/boil"
)

type Item struct {
	exec boil.ContextExecutor
}

func NewItem(db *sql.DB) *Item {
	return &Item{db}
}

func (i *Item) FindById(ctx context.Context, itemId int64) (*domain.Item, error) {
	item, err := db.FindItem(ctx, i.exec, itemId)
	if err != nil {
		return nil, err
	}

	return domain.NewItem(
		itemId,
		item.Name,
		domain.Rarity(item.Rarity),
	), nil
}
