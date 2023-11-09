---
"title": "ゴールデンファイルテストを使用したE2Eテスト"
---

本章では前章で作成したhandlerに対して**E2Eテスト**を作成していきたいと思います。テストはGo製の[goldie](https://github.com/sebdah/goldie)というライブラリを使用して**ゴールデンファイルテスト**として作成していきます。

## E2Eテストについて

E2Eテストは一種の結合テストとみることができ、共有依存やプロセスが依存などが統合された状態で想定通りに機能することを検証するテストです。結合テストとの違いはE2Eテストの方が**プロセス外依存を多く含む**ということです。結合テストが１、２個のプロセス外依存を扱うのに比べて、E2Eテストはほぼ全ての機能を動作させることになります。

しかし、ほぼ全ての機能を動作させるE2Eテストでも外部の決済APIなどを使用する場合は**モックを使用します**。モックを使用しないでテストをする場合、**非常にコストがかかる**からです。

## ゴールデンファイルテストについて

ゴールデンファイルテストとは過去に実行したテストの結果をファイル(ゴールデンファイル)に保存し、テスト結果と比較することで期待通りの結果が返ってくることを検証するテスト手法です。JSONのような大きな文字列ファイルなどの検証がしたい場合などに有効なテスト手法です。

## ハンドラーのテスト

前回までで作成したハンドラーのテストを上述のゴールデンファイルテストで作成してみます。ゴールデンファイルテストの作成には**goldie**というライブラリを使用します。

:::message
ゴールデンファイルテストを作成するためのライブラリはgoldieの他に[gotest.tools](https://pkg.go.dev/gotest.tools/v3/golden)などがあります。goldieは2020年あたりからメンテナンスされていなさそうなので気になる方は別のライブラリを使用してください。
:::

```
go get github.com/sebdah/goldie/v2
```

```go:infrastructure/handler/gacha_test.go
package handler_test

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/JY8752/go-unittest-architecture/controller"
	"github.com/JY8752/go-unittest-architecture/infrastructure/handler"
	"github.com/JY8752/go-unittest-architecture/infrastructure/repository"
	mock_api "github.com/JY8752/go-unittest-architecture/mocks/api"
	mock_domain "github.com/JY8752/go-unittest-architecture/mocks/domain"
	"github.com/JY8752/go-unittest-architecture/test"
	"github.com/labstack/echo/v4"
	"github.com/sebdah/goldie/v2"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

const (
	goldenDir      = "../../testdata/golden/"
	migrationsPath = "../../migrations"
)

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

func TestDraw(t *testing.T) {
	// Arrange
	gachaRep := repository.NewGacha(db)
	itemRep := repository.NewItem(db)

	ctrl := gomock.NewController(t)
	t.Cleanup(func() {
		ctrl.Finish()
	})

	sg := mock_domain.NewMockSeedGenerator(ctrl)
	sg.EXPECT().New().Return(int64(1000))

	p := mock_api.NewMockPayment(ctrl)
	p.EXPECT().Buy(100).Return(nil).Times(1)

	controller := controller.NewGacha(gachaRep, itemRep, sg, p)

	e := echo.New()

	handler.NewGacha(e, controller).Register()

	gachaId := 1
	w := httptest.NewRecorder()
	r := httptest.NewRequest(echo.POST, fmt.Sprintf("/gacha/%d/draw", gachaId), nil)

	g := goldie.New(t, goldie.WithFixtureDir(goldenDir))

	// Act
	response := handleAndIndentResponse(t, e, w, r)

	// Assertion
	assert.Equal(t, http.StatusOK, w.Code)
	g.Assert(t, t.Name(), response)
}

func handleAndIndentResponse(t *testing.T, e *echo.Echo, w *httptest.ResponseRecorder, r *http.Request) []byte {
	t.Helper()
	e.ServeHTTP(w, r)
	var buf bytes.Buffer
	err := json.Indent(&buf, w.Body.Bytes(), "", "  ")
	if err != nil {
		t.Fatal(err)
	}
	return buf.Bytes()
}

```

結合テストの時同様、seedの生成に関してはモックを使用しないと検証できないためモックを使用します。また、外部の決済APIの呼び出しはコストが高いことが考えられるためこちらもモックに置き換えます。

また、webサーバーの起動とその検証には```httptest```モジュールを使用します。

ゴールデンファイルの作成は```g := goldie.New(t, goldie.WithFixtureDir(goldenDir))```この部分で行われ、テスト実行時に```-update```オプションを付けることで```testdata/golden```ディレクトリ配下にゴールデンファイルが作成されます。

```json:testdata/golden/TestDraw.golden
{
  "item_id": 9,
  "name": "item9",
  "rarity": "R"
}
```

初回実行時にはゴールデンファイルを作成する必要があるため以下のコマンドのように```-update```オプションをつけて実行してください。

```
% go test ./infrastructure/handler/gacha_test.go -update
```

このようなゴールデンファイルテストをE2Eテストとして作成する以外に、特定のユーザー操作からどのような結果が得られるかをシナリオとして作成する**シナリオテスト**のようなものもあります。これにはAPIクライアントである[Postman](https://learning.postman.com/docs/writing-scripts/intro-to-scripts/)を使用してシナリオを作成したり、[runn](https://github.com/k1LoW/runn)のようなOSSを使用することで実現できます。

E2Eテストを書く場合、プロダクトやチームでどのような形式でテストを作成するのかは事前によく検討する必要があるかもしれません。

アプリケーションの実装とテストは本書まででだいたい完了しました。次章では依存関係の解決をwireというライブラリを使用して簡潔にしたいと思います。