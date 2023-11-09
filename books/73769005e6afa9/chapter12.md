---
"title": "管理下にないプロセス外依存処理を追加しテストする"
---

本章では前章で行ったモックを使用したテストをもう少し掘り下げるために外部APIとして決済APIの呼び出し処理を追加してテストしていきたいと思います。

## 決済APIの追加

決済APIは手抜きでダミー実装とします。

```go:infrastructure/api/payment.go
package api

//go:generate mockgen -source=$GOFILE -destination=../../mocks/api/mock_$GOFILE -package=mock_api

import "errors"

type Payment interface {
	Buy(int) error
}

type payment struct{}

func NewPayment() Payment {
	return &payment{}
}

// dummy実装 本当は外部の決済APIとかを叩く想定
func (p *payment) Buy(price int) error {
	if price == 100 {
		return nil
	}
	return errors.New("invalid price")
}
```

ダミー実装ですがこの処理はこのアプリケーションの管理下にない外部依存のためテスト内ではモックに置き換えてテストをします。そのため、gomockの生成コマンドを記載しておきます。

controllerに呼び出しを追加します。

```go:controller/gacha.go
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
	p        api.Payment // <--　依存を追加
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

	// 管理下にないプロセス外依存 決済する 手抜きでハードコード <-- ここを追加
	if err = g.p.Buy(100); err != nil {
		return nil, err
	}

	// アイテム情報を取得する
	return g.itemRep.FindById(ctx, itemId)
}


```

## 結合テストを修正する

決済APIの呼び出し処理を追加したので結合テストを修正します。

```go:controller/gacha_test.go
package controller_test

import (
	"context"
	"database/sql"
	"log"
	"os"
	"testing"

	"github.com/JY8752/go-unittest-architecture/controller"
	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/JY8752/go-unittest-architecture/infrastructure/repository"
	mock_api "github.com/JY8752/go-unittest-architecture/mocks/api"
	mock_domain "github.com/JY8752/go-unittest-architecture/mocks/domain"
	"github.com/JY8752/go-unittest-architecture/test"
	"go.uber.org/mock/gomock"

	"github.com/stretchr/testify/assert"
)

const migrationsPath = "../migrations"

var db *sql.DB

func TestMain(m *testing.M) {
	container, err := test.RunMySQLContainer()
	if err != nil {
		container.Close()
		log.Fatal(err)
	}

	db = container.DB

	if err := test.Migrate(db, migrationsPath); err != nil {
		container.Close()
		log.Fatal(err)
	}

	code := m.Run()

	container.Close()
	os.Exit(code)
}

// seed 1000 total weight 71 -> random 67 なのでitem9が抽選されるはず
func TestDraw(t *testing.T) {
	// Arange
	gachaRep := repository.NewGacha(db)
	itemRep := repository.NewItem(db)

	ctrl := gomock.NewController(t)
	t.Cleanup(func() {
		ctrl.Finish()
	})

	sg := mock_domain.NewMockSeedGenerator(ctrl)
	sg.EXPECT().New().Return(int64(1000))

  // <-- ここを追加
	p := mock_api.NewMockPayment(ctrl)
	p.EXPECT().Buy(100).Return(nil).Times(1)

	sut := controller.NewGacha(gachaRep, itemRep, sg, p)

	expected := domain.NewItem(
		9,
		"item9",
		domain.R,
	)

	// Act
	act, err := sut.Draw(context.Background(), 1)
	if err != nil {
		t.Fatal(err)
	}

	// Assertion
	assert.Equal(t, expected, act)
}


```

## モックの検証について

ここまででcontrollerのテスト内ではseedの生成と決済APIの呼び出しをモックにしていますが厳密にはこの２つは異なります。seedの生成に関しては依存からテスト対象へのコミュニケーションを模倣しているためこれは**スタブ**です。決済APIに関してはテスト対象のクラスから依存へのコミュニケーションを模倣したものなので**モック**となります。

「単体テストの考え方/使い方」では**スタブの検証をすることはアンチパターン**とされており、逆にモックは確実に外部に向けてのコミュニケーションが実行されたことが大事なので**検証されなければならない**とされています。スタブの検証がなぜアンチパターンかというと古典学派的に**結果の振る舞いを検証すべきで、その過程を検証すべきでない**としているからです。スタブの振る舞いまで検証してしまうとこれは**過剰検証**と呼ばれることになります。

```go
p.EXPECT().Buy(100).Return(nil).Times(1)
```

上記の決済APIのモックの```Times(1)```に注目してください。これで決済APIの呼び出しが1回実行されたということを検証しています。

```go
sg.EXPECT().New().Return(int64(1000))
```

逆に上記のseed生成のモックは厳密にはスタブなので検証までは実施していません。

次章ではAPIハンドラーの登録処理を実装していきたいと思います。