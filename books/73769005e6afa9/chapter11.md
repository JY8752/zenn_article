---
"title": "controllerの結合テスト"
---

本章では作成したcontrollerの結合テストを書いていきたいと思います。

## なぜ結合テストを書くのか？

単体テストでビジネスロジックはテストすることができますが外部サービスとドメイン層との接続部分をテストすることはできていません。結合テストは**単体テストで確認できなかった全ての異常系とコントローラーで行なっている接続部分を確認する**ために書かれます。結合テストは接続部分を検証する意味合いが強く**1件のハッピーパス**がテストできればよいとされています。ハッピーパスとはいわゆる正常系のことで期待する正常な振る舞いのことです。

## テスト用のDB環境を用意する

controllerのテストはプロセス外依存であるDBを扱い、この依存に関してはモックを使用しないためテストを実行する前に準備する必要があります。方法としてテスト実行者のローカルマシンにDBを起動する方法とDockerのようなコンテナ環境を用意する方法が考えられますが今回は**Docker**を使用してDBを用意したいと思います。

:::message
「単体テストの考え方/使い方」では結合テストに使用するDBにDockerを使用することはあまり**推奨していません**。その理由としてはDockerを使用した場合に、データベースのこと以外に以下のようなことにも注意を払う必要があり保守コストがかなり上がってしまうからです。

- Dockerイメージの保守
- 各テスト・ケースが個別のコンテナ・インスタンスを得られることの保証
- 使い終わった後のコンテナの破棄

しかし、今回はdockertestのようなライブラリを使用することでそのような保守コストを下げられるためDcokerを採用しましたが、実際に**Docker起因のエラー**で詰まることも多かったです。加えて、テスト前にDockerを起動する起動時間のことも考えると**Dockerを採用しない**という決断も間違いではないと筆者は思います。
:::

### dockertestの導入

Dockerを手動で起動して準備する方法も考えられますが今回は[dockertest](https://github.com/ory/dockertest)というGo製のライブラリを使用したいと思います。

dockertestとはテストコード上でコンテナの起動・停止を管理することができるテストライブラリです。類似のライブラリで[testcontainers](https://github.com/testcontainers/testcontainers-go)というライブラリも存在します。このようなツールを使用するメリットとしてテストコード上でコンテナ操作を実施することができるので外部環境に左右されずテストを実行することができます。Dockerを使用するのでテスト実行者の環境でdcoker-engineが起動している必要はありますがコンテナの起動や停止を意識せずテストを実行することができるということです。

ただし、デメリットとしてテスト実行前にコンテナを都度起動するため**テストの実行時間が伸びます**。外部環境を気にせず結合テストを実行できるトレードオフになっているためこのようなライブラリを採用するかは慎重に検討する必要があるかもしれません。

また、dockertestとtestcontainersを以前に比較した記事を書いたためもし興味があればこちらもご参照ください。

https://zenn.dev/jy8752/articles/419ab77b2b6a61

dockertestを使用したMySQLコンテナの起動の処理は以下のようになります。

```
go get -u github.com/ory/dockertest/v3
```

```go:test/container.go
package test

import (
	"database/sql"
	"fmt"
	"io"
	"log"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"github.com/ory/dockertest/v3"
	"github.com/ory/dockertest/v3/docker"
)

type MySQLContainer struct {
	Pool     *dockertest.Pool
	Resource *dockertest.Resource
	DB       *sql.DB
}

func RunMySQLContainer() (*MySQLContainer, error) {
	pool, err := dockertest.NewPool("")
	pool.MaxWait = time.Minute * 1
	if err != nil {
		return nil, fmt.Errorf("could not construct pool: %w", err)
	}

	log.Println("connecting to Docker...")

	err = pool.Client.Ping()
	if err != nil {
		return nil, fmt.Errorf("could not connect to Docker: %w", err)
	}

	log.Println("success connecting to Docker🚀")

	resource, err := pool.RunWithOptions(&dockertest.RunOptions{
		Repository: "mysql",
		Tag:        "latest",
		Env: []string{
			"MYSQL_ROOT_PASSWORD=secret",
		},
	}, func(hc *docker.HostConfig) {
		hc.AutoRemove = true
		hc.RestartPolicy = docker.RestartPolicy{
			Name: "no",
		}
	})

	if err != nil {
		return nil, fmt.Errorf("could not start resource: %w", err)
	}

	if err = resource.Expire(600); err != nil {
		return nil, fmt.Errorf("could not set container expire: %w", err)
	}

	var db *sql.DB

	log.Println("connecting to DB...")

	if err = pool.Retry(func() error {
		var err error
		db, err = sql.Open("mysql", fmt.Sprintf("root:secret@(localhost:%s)/mysql?parseTime=true&multiStatements=true", resource.GetPort("3306/tcp")))
		if err != nil {
			return err
		}
		return db.Ping()
	}); err != nil {
		return nil, fmt.Errorf("could not connect to docker: %w", err)
	}

	log.Println("success connecting to DB🚀")
	log.Println("mysql container start🐳")

	return &MySQLContainer{
		Pool:     pool,
		Resource: resource,
		DB:       db,
	}, nil
}

func (mc *MySQLContainer) Close() {
	if err := mc.DB.Close(); err != nil {
		log.Printf("could not close db: %s\n", err)
	}
	// When you're done, kill and remove the container
	if err := mc.Pool.Purge(mc.Resource); err != nil {
		log.Printf("could not purge resource: %s\n", err)
	}
	log.Println("mysql container end🐳")
}
```

実際にテストで使用するには以下のように```TestMain()```で呼び出すことでテストケースを実行する前にコンテナの起動をすることができます。

```go
func TestMain(m *testing.M) {
	container, err := test.RunMySQLContainer()
	if err != nil {
		container.Close()
		log.Fatal(err)
	}

	code := m.Run()

	container.Close()
	os.Exit(code)
}
```

コンテナは```AutoRemove```の設定を設定しているのでコンテナは自動で破棄されますが念のため```Close()```を用意して明示的に破棄しています。さらに```resource.Expire(600)```の部分でコンテナのExpireを設定して万が一コンテナが破棄されなかったとしても削除されるようにしています。(さすがにここまではいらないかもですが明示的な```Close()```は入れておいたほうが確実かなと思います。)Close関数の呼び出しを```TestMain()```内で呼び出す場合、```os.Exit()```や```log.Fatal()```を使用すると```defer```で```Close()```を仕込んでも実行されないため注意してください。

また、後述する```go-migrate```をMySQLで使用する場合、```multiStatements=true```のパラメーターを設定する必要があるためパラメーターを指定したうえでコンテナを起動しています。

さらに、テストコードからコンテナを起動すると以下のようなログが複数出力されてしまいます。
```
[mysql] 2023/11/07 15:46:08 /Users/yamanakajunichi/work/zenn/demo/go-test/controller/packets.go:37: unexpected EOF
```

これはコンテナ起動の完了を待つために```Ping()```を打ち続けるために出力されるログですが、テストの実行にはノイズになるので以下のようにログを抑制することも可能です。

```go
func init() {
	// コンテナが起動するまでPingを打ち続けるので接続できるまでエラーが出続けるので抑止
	mysql.SetLogger(mysql.Logger(log.New(io.Discard, "", 0)))
}
```

### テスト実行前にマイグレーションを実行する

開発中にDBの変更があった場合、その変更を本番環境に反映させる必要があります。反映方法にはさまざまな方法が考えられますがプロダクションコードと合わせてバージョン管理することができるためマイグレーションツールを採用するプロダクトも多いでしょう。この時、スキーマ情報と合わせて管理するものに**参照データ**というものがあります。これはマスターデータのようなもので、**そのデータがないとアプリケーションが適切に動作しない**ようなデータのことです。このようなデータはアプリケーションの起動に必要不可欠なためスキーマ情報と合わせてコード管理されるべきものです。

今回作成しているアプリケーションはガチャとアイテムデータは全て参照データのためテスト起動時にも存在している必要があります。そのため、コンテナ起動と合わせてマイグレーションの実行もテスト起動前に実施する必要があります。```go-migrate```にはCLIツールだけでなくGoプロダクトから利用することもできるため以下のようにマイグレーション実行処理を作成します。

```go:test/migrate.go
package test

import (
	"database/sql"

	"github.com/golang-migrate/migrate/v4"
	"github.com/golang-migrate/migrate/v4/database/mysql"
	_ "github.com/golang-migrate/migrate/v4/source/file"
)

func Migrate(db *sql.DB, migrationsPath string) error {
	driver, err := mysql.WithInstance(db, &mysql.Config{})
	if err != nil {
		return err
	}

	migrate, err := migrate.NewWithDatabaseInstance(
		"file://"+migrationsPath,
		"mysql",
		driver,
	)
	if err != nil {
		return err
	}

	if err = migrate.Up(); err != nil {
		return err
	}

	return nil
}

```

マイグレーションインスタンスの作成の仕方はいくつかありますが```*sql.DB```とマイグレーションファイルが配置されているディレクトリパスを指定する方法でインスタンスを作成し、```migrate up```をコード上から実行しています。これをテスト実行前に使用する例は以下のようになります。

```go
const (
	migrationPath = "../migrations"
)

func TestMain(m *testing.M) {
	container, err := test.RunMySQLContainer()
	if err != nil {
		container.Close()
		log.Fatal(err)
	}

	if err = test.Migrate(container.DB, migrationPath); err != nil {
		container.Close()
		log.Fatal(err)
	}

	code := m.Run()

	container.Close()
	os.Exit(code)
}
```

## seedの生成を外部依存として切り出す

ここまででDBの準備はできましたが今の実装のままではseed値をcontrollerで生成しているため、戻り値が予測不可能なためテストすることができません。ここでテストしたいことはDBとドメイン層の接続がうまくいっているかということで正直seedの生成には興味がありません。そこでseedの生成を切り出し、インターフェースにすることで**モック**に置き換えることにします。

### seed生成インターフェースを作成する

以下のようなファイルを作成します。

```go:domain/random.go
package domain

import "time"

type SeedGenerator interface {
	New() int64
}

type seedGenerator struct{}

func NewSeedGenerator() SeedGenerator {
	return &seedGenerator{}
}

func (s *seedGenerator) New() int64 {
	return time.Now().UnixNano()
}
```

controllerで行っていた処理を切り出しただけです。呼び出しは以下のように修正します。

```go:controller/gacha.go
package controller

import (
	"context"

	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/JY8752/go-unittest-architecture/infrastructure/repository"
)

type Gacha struct {
	gachaRep *repository.Gacha
	itemRep  *repository.Item
	sg       domain.SeedGenerator // <-- この依存を追加
}

func NewGacha(gachaRep *repository.Gacha, itemRep *repository.Item, sg domain.SeedGenerator) *Gacha {
	return &Gacha{gachaRep, itemRep, sg}
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
	seed := g.sg.New() // <-- 外部依存を呼び出すように変更

	// アイテムを抽選する
	itemId, err := gacha.Draw(seed)
	if err != nil {
		return nil, err
	}

	// アイテム情報を取得する
	return g.itemRep.FindById(ctx, itemId)
}

```

### モックを作成する

次にモックを作成します。(正確に言うとモックはテスト対象のシステムから外部へ向かう処理の模倣のため今回はスタブとなりますがわかりやすいようにモックとします。)古典学派的にモックを使用するのは**管理下にないプロセス外依存**のみです。今回のseedの生成は管理することができない依存と考えられるのでモックに置き換えることにします。

Goにおけるモックの作成には自作する方法と```gomock```のようなモックライブラリを使用する方法がありますが、モックの検証まで簡単に実装できることを考えて今回は[gomock](https://github.com/uber-go/mock)を使用することとします。

```
go install go.uber.org/mock/mockgen@latest

% mockgen -version
v0.3.0
```

:::message
gomockはgolangのリポジトリでしたが2023年6月を持ってuber社のリポジトリに移行しているので注意してください。
:::

モックの作成は```go generate```で行います。そのため、モックを作成したいファイルに以下のように記載しておくことで```go generate```を実施すれば全てのモックが生成されるようにすることができます。

```diff go:domain/random.go
package domain

+ //go:generate mockgen -source=$GOFILE -destination=../mocks/domain/mock_$GOFILE -package=mock_domain

import "time"

type SeedGenerator interface {
	New() int64
}

type seedGenerator struct{}

func NewSeedGenerator() SeedGenerator {
	return &seedGenerator{}
}

func (s *seedGenerator) New() int64 {
	return time.Now().UnixNano()
}

```

## cotrollerの結合テスト

ここまででDBとモックの準備ができたので結合テストを以下のように作成します。

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

	sut := controller.NewGacha(gachaRep, itemRep, sg)

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

これで**1件のハッピーパスを結合テストで検証**することができました。

## (おまけ)結合テストの並列実行について

結合テストではDBを共有依存としてテストケース間で共有することになるため、**並列で実行する難易度はかなり上がります**。今回作成しているアプリケーションでは参照データを読み取るだけでデータの挿入を行なっていませんが、テストケースごとにデータの追加が行われるようであれば並列実行は難しくなります。

複雑な制御をすれば可能かもしれませんが保守コストが格段に上がるため結合テストにおいては**１件ずつ逐次実行にしたほうが無難**でしょう。

## (おまけ)結合テストにおけるデータの後始末

今回のアプリケーションではデータの追加操作は行われませんが、もしデータの追加が行われる場合、テストケースごとに**後始末として追加したデータを削除する**必要があります。なぜなら、古典学派のテストはテストケース間が隔離されるべきで追加されたデータが別のテストケースに影響を与えることを避けなければならないからです。

データの後始末のタイミングですがテスト後に後始末するようにすると**後始末漏れでテストが失敗してしまう**ことがあるため、**Arrangeブロックでテスト実行前に後始末する**のがいいでしょう。

## まとめ

- 結合テストは**単体テストで確認できなかった全ての異常系とコントローラーで行なっている接続部分の検証**を行う。
- 結合テストは**1件のハッピーパス**を検証できればよい。
- 結合テストに使用するDBには**Docker**を使用し、その管理にはdockertestを使用しました。しかし、**Dockerを採用しないという決断も間違いではない**。
- **参照データ**はアプリケーションの起動に不可欠なため、テスト実行前にマイグレーションを実行し用意する。マイグレーションの実行にはgo-migrateを使用しました。
- seedのような**制御できない値は外部依存としてモック化できるようにする**。モックの作成にはgomockを使用しました。

次章ではcontrollerに管理下にない外部依存処理を追加してモックのテストをもう少しみていきたいと思います。