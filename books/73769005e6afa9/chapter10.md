---
"title": "DB処理を抽出する"
---

本章ではcontrollerからDB処理を抽出します。前述したようにDB処理はプロセス外依存ですが外部APIのように管理下になり依存とは違い**管理下にある**プロセス外依存です。古典学派の考えではモックにしてよいのは**管理下にないプロセス外依存のみ**のためDB処理は共有依存ですがモックにせず、そのままプロダクションのコードを使用します。そして、古典学派的には**実装が１つしかないのであればインターフェースを作成すべきでない**としているためインターフェースを定義せずRepository構造体を作成するように修正していきます。

## Repositoryの作成

以下のようなRepositoryを作成します。

```go:infrastructure/repository/gacha.go
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

```

```go:infrastructure/repository/item.go
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
```

ここでアイテム情報をsqlboilerが自動生成したデータの構造体をそのまま扱っていたので、ドメインモデルに変換する処理も追加しました。

```go:domain/item.go
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
```

今回のようにORMを使用しているのであればデータオブジェクトをドメインモデルに変換する処理はRepositoryでやってしまって問題ないでしょう。**controllerはビジネスロジックと外部依存との接続のみを責務とすべき**なので変換処理を行うべきではないです。

ここまでできたら、cotroller側も以下のように修正します。

```go:controller/gacha.go
func (g *Gacha) Draw(ctx context.Context, gachaId int) (*domain.Item, error) {
	// 指定ガチャの重み一覧を取得
	gachaRep := repository.NewGacha(g.DB) // <-- 作成したRepositoryに置き換え
	gachaItemWeights, err := gachaRep.GetGachaItemWeights(ctx, gachaId)
	if err != nil {
		return nil, err
	}

	// ドメインモデルを生成
	gacha := domain.NewGacha(gachaItemWeights)

	// 乱数生成のためのseedを取得
	seed := time.Now().UnixNano()

	// アイテムを抽選する
	itemId, err := gacha.Draw(seed)
	if err != nil {
		return nil, err
	}

	// アイテム情報を取得する
	itemRep := repository.NewItem(g.DB) // <-- 作成したRepositoryに置き換え
	return itemRep.FindById(ctx, itemId)
}
```

また、Repository構造体を作成する処理は外部依存であり、controllerですべきではないのでcontrollerに依存性として注入します。

```go:controller/gacha.go
type Gacha struct {
  // *sql.DB
	gachaRep *repository.Gacha // <-- repositoryを依存性に追加
	itemRep  *repository.Item // <-- repositoryを依存性に追加
}

func NewGacha(gachaRep *repository.Gacha, itemRep *repository.Item) *Gacha {
	return &Gacha{gachaRep, itemRep}
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
	seed := time.Now().UnixNano()

	// アイテムを抽選する
	itemId, err := gacha.Draw(seed)
	if err != nil {
		return nil, err
	}

	// アイテム情報を取得する
	return g.itemRep.FindById(ctx, itemId)
}
```

## Repositoryのテストを書くべきか？

まず最初にRepositoryのテストは単体テストになるでしょうか？それとも結合テストになるでしょうか？Repositoryはプロセス外依存であるDBを扱うため**結合テスト**に分類されます。結合テストになるとテスト対象であるRepository単体ではテストすることはできず、テスト用にDBを準備する必要があります。DBの準備にはテスト実行者のローカルマシンにDBを準備する方法とDockerなどのコンテナ環境を準備する方法がありますがいずれにせよ**単体テストと比べてテストの実行時間が長くなります**。

また、Repositoryのテストに関しては**controllerの結合テストで検証することができるはずです**。なぜなら、古典学派的なテストを書く場合ほぼ全ての依存関係はモックを使わず実際のプロダクションコードを使用するからです。

必要なテストであれば書くべきですがただでさえ実行時間が長い結合テストを**効果が薄いのに増やすべきではありません**。

複雑なクエリを発行する際などは特にRepositoryのテストを書いてサクッと動作を確認したくなってしまうことがあるかもしれませんが、大抵の場合controllerのテストを書くことでその検証は可能なはずです。

絶対に書いてはいけないわけではなく、例外などもあるでしょうが**極力Repositoryのテストは書かないでcontrollerで検証**すべきだと筆者は考えます。

## まとめ

- DBとやりとりする処理をRepositoryに切り出しました。
- 今のところ実装は１つであり、テストではモックを使用しないため**インターフェースを作成しません**でした。
- データオブジェクトからドメインモデルへの変換処理は**Repositoryに追加**しました。
- **Repositoryのテストは作成せず、controllerで検証**するようにしました。

次章ではcontrollerの結合テストを作成していきたいと思います。