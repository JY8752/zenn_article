package controller

import (
	"context"

	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/JY8752/go-unittest-architecture/infrastructure/api"
	"github.com/JY8752/go-unittest-architecture/infrastructure/repository"
)

type Gacha struct {
	gachaRep *repository.Gacha
	itemRep  *repository.Item
	sg       domain.SeedGenerator
	p        api.Payment
}

func NewGacha(gachaRep *repository.Gacha, itemRep *repository.Item, sg domain.SeedGenerator, p api.Payment) *Gacha {
	return &Gacha{gachaRep, itemRep, sg, p}
}

func (g *Gacha) Draw(ctx context.Context, gachaId int) (*domain.Item, error) {
	// 指定ガチャの重み一覧を取得
	gachaItemWeights, err := g.gachaRep.GetGachaItemWeights(ctx, gachaId)
	if err != nil {
		return nil, err
	}

	// ドメインモデルを生成
	gacha := domain.NewGacha(gachaItemWeights)

	// 乱数生成のためのseedを取得
	seed := g.sg.New()

	// アイテムを抽選する
	itemId, err := gacha.Draw(seed)
	if err != nil {
		return nil, err
	}

	// 管理下にないプロセス外依存 決済する 手抜きでハードコード
	if err = g.p.Buy(100); err != nil {
		return nil, err
	}

	// アイテム情報を取得する
	return g.itemRep.FindById(ctx, itemId)
}
