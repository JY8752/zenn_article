---
"title": "依存関係の解決"
---

本章では依存関係の解決に[wire](https://github.com/google/wire)というライブラリを導入してみたいと思います。

```
go get github.com/google/wire
```

現状依存性の解決は全て```main.go```の以下の箇所で全て行なっています。

```go
	gachaRep := repository.NewGacha(db)
	itemRep := repository.NewItem(db)
	seedGenerator := domain.NewSeedGenerator()
	payment := api.NewPayment()

	controllerGacha := controller.NewGacha(gachaRep, itemRep, seedGenerator, payment)

	handlerGacha := handler.NewGacha(e, controllerGacha)

	handler.NewRoot(handlerGacha).RegisterAll()
```

このままでは機能が増えるたびにhandlerやcontroller,repositoryといった実装が増え依存関係の解決が煩雑になってしまいます。wireを使用することで作成したい構造体の依存関係の解決を自動で生成することができるようになります。コードを自動生成するには以下のような生成処理を作成します。

```go:wire.go
//go:build wireinject

package main

import (
	"database/sql"

	"github.com/JY8752/go-unittest-architecture/controller"
	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/JY8752/go-unittest-architecture/infrastructure/api"
	"github.com/JY8752/go-unittest-architecture/infrastructure/handler"
	"github.com/JY8752/go-unittest-architecture/infrastructure/repository"
	"github.com/google/wire"
	"github.com/labstack/echo/v4"
)

func InitializeRootHandler(db *sql.DB, e *echo.Echo) *handler.Root {
	wire.Build(
		repository.NewGacha,
		repository.NewItem,
		domain.NewSeedGenerator,
		api.NewPayment,
		controller.NewGacha,
		handler.NewGacha,
		handler.NewRoot,
	)
	return &handler.Root{}
}

```

最終的に```*handler.Root```を作成するように全ての依存関係のファクトリ関数を順に列挙していきます。```*sql.DB```や```*echo.Echo```のような外部依存は関数の引数を指定することで解決することができます。この処理はコードを自動生成するためだけの処理のため```//go:build wireinject```のようにビルドタグを記載しプロダクションのビルドに含まれないようにしましょう。

作成できたらwireのCLIツールを使用してコードを自動生成します。

```
% go install github.com/google/wire/cmd/wire@latest
% wire gen
```

問題なく生成されると```wire_gen.go```というファイルが作成され以下のような依存性を解決するコードが生成されているはずです。

```go:wire_gen.go
// Injectors from wire.go:

func InitializeRootHandler(db *sql.DB, e *echo.Echo) *handler.Root {
	gacha := repository.NewGacha(db)
	item := repository.NewItem(db)
	seedGenerator := domain.NewSeedGenerator()
	payment := api.NewPayment()
	controllerGacha := controller.NewGacha(gacha, item, seedGenerator, payment)
	handlerGacha := handler.NewGacha(e, controllerGacha)
	root := handler.NewRoot(handlerGacha)
	return root
}

```

この生成されたコードで```main.go```の処理を置き換えます。

```diff go:main.go
+	InitializeRootHandler(db, e).RegisterAll()

- gachaRep := repository.NewGacha(db)
- itemRep := repository.NewItem(db)
- seedGenerator := domain.NewSeedGenerator()
- payment := api.NewPayment()

- controllerGacha := controller.NewGacha(gachaRep, itemRep, seedGenerator, payment)

- handlerGacha := handler.NewGacha(e, controllerGacha)

- handler.NewRoot(handlerGacha).RegisterAll()
```

これで依存関係の解決がスッキリしました！