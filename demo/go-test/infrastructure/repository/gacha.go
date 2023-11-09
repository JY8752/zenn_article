package repository

import (
	"context"
	"database/sql"

	"github.com/JY8752/go-unittest-architecture/db"
	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/volatiletech/sqlboiler/v4/boil"
	"github.com/volatiletech/sqlboiler/v4/queries/qm"
)

type Gacha struct {
	exec boil.ContextExecutor
}

func NewGacha(db *sql.DB) *Gacha {
	return &Gacha{db}
}

func (g *Gacha) GetGachaItemWeights(ctx context.Context, gid int) (domain.GachaItemWeights, error) {
	gachaItems, err := db.GachaItems(
		qm.Select(db.GachaItemColumns.ItemID, db.GachaItemColumns.Weight),
		db.GachaItemWhere.GachaID.EQ(gid),
	).All(ctx, g.exec)

	if err != nil {
		return nil, err
	}

	return convertGachaItemWeights(gachaItems), nil
}

func convertGachaItemWeights(gachaItems db.GachaItemSlice) domain.GachaItemWeights {
	gachaItemWeights := make(domain.GachaItemWeights, len(gachaItems))
	for i, item := range gachaItems {
		gachaItemWeights[i] = struct {
			ItemId int64
			Weight int
		}{
			ItemId: item.ItemID,
			Weight: item.Weight,
		}
	}
	return gachaItemWeights
}
