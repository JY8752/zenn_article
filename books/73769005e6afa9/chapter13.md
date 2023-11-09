---
"title": "APIハンドラーの登録"
---

前回まででドメイン層とDBの処理、およびその接続部分であるcontrollerの実装とテストまで作成できました。本章では実際に外部からこのガチャの処理を呼び出せるようにAPIハンドラーの実装を行なっていきます。APIハンドラーの登録にはGoの軽量webフレームワークである[Echo](https://github.com/labstack/echo)を使用して作成していきたいと思います。

```
% go get github.com/labstack/echo/v4
```

## handlerの作成

echoインスタンスとcontrollerを依存関係に持つhandlerを以下のように作成します。

```go:infrastructure/handler/gacha.go
package handler

import (
	"context"
	"net/http"
	"strconv"

	"github.com/JY8752/go-unittest-architecture/controller"
	"github.com/labstack/echo/v4"
)

type Gacha struct {
	e  *echo.Echo
	gc *controller.Gacha
}

func NewGacha(e *echo.Echo, gc *controller.Gacha) *Gacha {
	return &Gacha{e, gc}
}

func (g *Gacha) Register() {
	g.draw()
}

type ErrorResponse struct {
	Message string `json:"message"`
	Err     error  `json:"error"`
}

func (g *Gacha) draw() {
	g.e.POST("/gacha/:gachaId/draw", func(c echo.Context) error {
		gachaId, err := strconv.Atoi(c.Param("gachaId"))
		if err != nil {
			return c.JSON(http.StatusBadRequest, ErrorResponse{
				Message: "gachaId parameter is invalid",
				Err:     err,
			})
		}

		item, err := g.gc.Draw(context.Background(), gachaId)
		if err != nil {
			return c.JSON(http.StatusBadRequest, ErrorResponse{
				Message: "failed to draw gacha",
				Err:     err,
			})
		}

		return c.JSON(http.StatusOK, item)
	})
}

```

上記の```Register()```を呼び出すことでAPIエンドポイントが登録されるような流れです。もし、エンドポイントが増えるようであれば同じようにhandlerを登録するプライベートな関数を作成し```Register()```の処理に追加する想定です。

## Root Handlerを作成する

今回は１つのハンドラーしか作成しませんが、実際のプロダクトでは複数のハンドラーを扱うことになると思いますので**Root**のハンドラーを作成しておきます。

```go:infrastructure/handler/root.go
package handler

type Handler interface {
	Register()
}

type Root struct {
	handlers []Handler
}

func NewRoot(g *Gacha) *Root {
	return &Root{[]Handler{g}}
}

func (r *Root) RegisterAll() {
	for _, h := range r.handlers {
		h.Register()
	}
}
```

ポイントは```RegisterAll()```で全てのハンドラーの登録処理を行なっていることです。これを実現するためには各ハンドラーに```Register()```を用意し、実行できるようにすればよいため**インターフェースを作成**しましょう。インターフェースは**作成するものではなく発見するもの**とここまでに述べてきましたが、今回の例はインターフェースを作成してよい良い例と言えます。

## ハンドラーを登録する

上記で作成したハンドラーの登録を以下のようなmain.goを作成して実行します。

```go:main.go
package main

import (
	"database/sql"
	"fmt"
	"log"
	"os"
	"strconv"

	_ "github.com/go-sql-driver/mysql"
)

type dbConfig struct {
	User     string
	Pass     string
	Host     string
	Port     int
	Database string
}

func newDBConfig() (*dbConfig, error) {
	user := os.Getenv("MYSQL_USER")
	pass := os.Getenv("MYSQL_PASS")
	host := os.Getenv("MYSQL_HOST")
	port, err := strconv.Atoi(os.Getenv("MYSQL_PORT"))
	if err != nil {
		return nil, err
	}
	database := os.Getenv("MYSQL_DBNAME")

	return &dbConfig{
		user,
		pass,
		host,
		port,
		database,
	}, nil
}

func initializeDB(config *dbConfig) (*sql.DB, error) {
	connectionString := fmt.Sprintf(
		"%s:%s@tcp(%s:%d)/%s",
		config.User,
		config.Pass,
		config.Host,
		config.Port,
		config.Database,
	)

	return sql.Open("mysql", connectionString)
}

func main() {
	config, err := newDBConfig()
	if err != nil {
		log.Fatal(err)
	}

	db, err := initializeDB(config)
	if err != nil {
		log.Fatal(err)
	}

	defer func() {
		if err = db.Close(); err != nil {
			fmt.Println(err.Error())
		}
	}()

	e := echo.New()

	gachaRep := repository.NewGacha(db)
	itemRep := repository.NewItem(db)
	seedGenerator := domain.NewSeedGenerator()
	payment := api.NewPayment()

	controllerGacha := controller.NewGacha(gachaRep, itemRep, seedGenerator, payment)

	handlerGacha := handler.NewGacha(e, controllerGacha)

	handler.NewRoot(handlerGacha).RegisterAll()

	e.Logger.Fatal(e.Start(fmt.Sprintf(":%d", ServerPort)))
}
```

作成できたらローカルで実行してみましょう。

```
% go run main.go

% curl -XPOST localhost:8080/gacha/1/draw | jq
{
  "item_id": 9,
  "name": "item9",
  "rarity": "R"
}
```

次章では作成したhandlerのテストをE2Eテストとして作成していきたいと思います。