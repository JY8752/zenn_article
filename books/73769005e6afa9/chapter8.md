---
"title": "ガチャ抽選のビジネスロジックを抽出する"
---

前回まででDBの準備と古典学派のテストについての説明をさせていただきました。本章からはいよいよ実装に進んでいきたいと思います。

## ガチャ抽選の実装

指定のガチャIDを引数にとりガチャの抽選をし、抽選したアイテム情報を返す処理をcontrollerに実装したものが以下です。DBへのクエリの発行は自動生成済みのsqlboilerのコードを使用します。

```go:controller/gacha.go
package controller

import (
	"context"
	"database/sql"
	"errors"
	"math/rand"
	"time"

	"github.com/JY8752/go-unittest-architecture/db"
	"github.com/volatiletech/sqlboiler/v4/queries/qm"
)

type Gacha struct {
	*sql.DB
}

func NewGacha(db *sql.DB) *Gacha {
	return &Gacha{}
}

func (g *Gacha) Draw(ctx context.Context, gachaId int) (*db.Item, error) {
	// 指定ガチャの重み一覧を取得
	gachaItems, err := db.GachaItems(
		qm.Select(db.GachaItemColumns.ItemID, db.GachaItemColumns.Weight),
		db.GachaItemWhere.GachaID.EQ(gachaId),
	).All(ctx, g.DB)

	if err != nil {
		return nil, err
	}

	// 重みの一覧を抽出
	weights := make([]int, len(gachaItems))
	for i, item := range gachaItems {
		weights[i] = item.Weight
	}

	// 乱数生成のためのseedを取得
	seed := time.Now().UnixNano()

	// アイテムを抽選する
	index, err := linearSearchLottery(weights, seed)
	if err != nil {
		return nil, err
	}

	// アイテム情報を取得する
	item, err := db.FindItem(ctx, g.DB, gachaItems[index].ItemID)
	if err != nil {
		return nil, err
	}

	return item, nil
}

/*
線形探索で重み付抽選する
@return 当選した要素のインデックス
*/
func linearSearchLottery(weights []int, seed int64) (int, error) {
	//  重みの総和を取得する
	var total int
	for _, weight := range weights {
		total += weight
	}

	// 乱数取得
	r := rand.New(rand.NewSource(seed))
	rnd := r.Intn(total)

	var currentWeight int
	for i, w := range weights {
		// 現在要素までの重みの総和
		currentWeight += w

		if rnd < currentWeight {
			return i, nil
		}
	}

	// たぶんありえない
	return 0, errors.New("the lottery failed")
}

```

見てわかる通りこの```Draw()```をテストするのは容易ではありません。抽選という複雑なビジネスロジックとプロセス外依存であるDBとのやり取りが混在しているためです。まずはこの関数からビジネスロジックを全て切り出してみましょう。

## ビジネスロジックの抽出

まずはガチャの抽選ロジック部分を以下のように切り出してみます。アイテムIDと重みを対応付けた構造体をスライスで持つ```GachaItemWeights```と```Gacha```というドメインモデルを新たに作成しています。

```go:domain/gacha.go
package domain

import (
	"errors"
	"math/rand"
)

type GachaItemWeights []struct {
	ItemId int64
	Weight int
}

type Gacha struct {
	Weights GachaItemWeights
}

func NewGacha(weights GachaItemWeights) *Gacha {
	return &Gacha{weights}
}

func (g *Gacha) Draw(seed int64) (int64, error) {
	weights := make([]int, len(g.Weights))
	for i, w := range g.Weights {
		weights[i] = w.Weight
	}

	index, err := linearSearchLottery(weights, seed)
	if err != nil {
		return 0, err
	}

	return g.Weights[index].ItemId, nil
}

/*
線形探索で重み付抽選する
@return 当選した要素のインデックス
*/
func linearSearchLottery(weights []int, seed int64) (int, error) {
	//  重みの総和を取得する
	var total int
	for _, weight := range weights {
		total += weight
	}

	// 乱数取得
	r := rand.New(rand.NewSource(seed))
	rnd := r.Intn(total)

	var currentWeight int
	for i, w := range weights {
		// 現在要素までの重みの総和
		currentWeight += w

		if rnd < currentWeight {
			return i, nil
		}
	}

	// たぶんありえない
	return 0, errors.New("the lotterya failed")
}
```

controllerからは作成したドメインモデルを呼び出すように以下のように修正します。

```go:controller/gacha.go
package controller

import (
	"context"
	"database/sql"
	"time"

	"github.com/JY8752/go-unittest-architecture/db"
	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/volatiletech/sqlboiler/v4/queries/qm"
)

type Gacha struct {
	*sql.DB
}

func NewGacha(db *sql.DB) *Gacha {
	return &Gacha{}
}

func (g *Gacha) Draw(ctx context.Context, gachaId int) (*db.Item, error) {
	// 指定ガチャの重み一覧を取得
	gachaItems, err := db.GachaItems(
		qm.Select(db.GachaItemColumns.ItemID, db.GachaItemColumns.Weight),
		db.GachaItemWhere.GachaID.EQ(gachaId),
	).All(ctx, g.DB)

	if err != nil {
		return nil, err
	}

  // ドメイン層に渡すのにデータオブジェクトの変換をする <--　これを追加
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

	// ドメインモデルを生成 <-- これを追加
	gacha := domain.NewGacha(gachaItemWeights)

	// 乱数生成のためのseedを取得
	seed := time.Now().UnixNano()

	// アイテムを抽選する
	itemId, err := gacha.Draw(seed)
	if err != nil {
		return nil, err
	}

	// アイテム情報を取得する
	item, err := db.FindItem(ctx, g.DB, itemId)
	if err != nil {
		return nil, err
	}

	return item, nil
}

```

少しcontrollerがスッキリしました。controllerのテストが容易ではない時、controllerが仕事をしすぎていることがほとんどのためビジネスロジックが含まれていないか注意して見るとよいでしょう。

## まとめ

- controllerは**ビジネスロジックを扱うドメイン層と外部依存との接続に専念すべき**。
- ビジネスロジックを扱う関数は**副作用のない純粋関数を目指す**と質の良いテストになりやすい。

![](https://storage.googleapis.com/zenn-user-upload/adb72f6088b3-20231104.png)

今回のリファクタリングでは上記の図にあるcontrollerから漏れ出てしまったビジネスロジックを分離し、domain層に抽出しました。ビジネスロジックを実行する関数はテストしやすいよう入力と出力がわかりやすい副作用のない作りを目指しました。次章で作成したドメイン層の単体テストを作成していきたいと思います。

