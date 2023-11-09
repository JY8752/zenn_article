---
"title": "sqlboilerを使用したコードの自動生成"
---

本章では作成したDBをもとに[sqlboiler](https://github.com/volatiletech/sqlboiler)を使用してコードの自動生成を行います。

## sqlboilerのインストール

```
go install github.com/volatiletech/sqlboiler/v4@latest
go install github.com/volatiletech/sqlboiler/v4/drivers/sqlboiler-mysql@latest
```

上記コマンドを実行することでsqlboilerのCLIツールとMySQLのドライバーをインストールすることができます。

```
% sqlboiler --version
SQLBoiler v4.15.0
```

## 設定ファイルを作成する

sqlboilerでコードを生成する前にプロジェクトのルートパスに```sqlboiler.toml```という名前でファイルを作成し、以下のように記載します。

```toml:sqlboiler.toml
pkgname = "db"
output = "db"
wipe = true                 # 前回生成したコードを毎回削除
add-global-variants = false # グローバル構造体を使用する関数を生成するか
no-tests = true             # テストコードを作成するか

[mysql]
# dbname  = "dbname"
# host    = "localhost"
# port    = 3306
# user    = "dbusername"
# pass    = "dbpassword"
sslmode = "false"

[aliases.tables.gachas]
up_plural = "Gachas"
up_singular = "Gacha"
down_plural = "gachas"
down_singular = "gacha"
```

MySQLの接続設定値は秘匿情報のため直接記載することもできますが、環境変数を使用することにします。sqlboilerの設定ファイルは環境変数に対応しており上記記載のキーのprefixに```MYSQL_```をつけて設定することで利用することができます。事前に作成していた```.env```に記載した環境変数はsqlboilerで読み込めるように変数名をつけているので対応済みとなります。

また、```aliases.tables.gachas```の部分はコードの自動生成される構造体に関する設定で、例えば```users```のような複数形のテーブルがあったとき```User```というような単数形の名前で構造体が生成されます。この、生成がうまくいかないときがあるので明示的に命名方法を指定することができます。今回は```gachas```というテーブル名から生成される構造体が```Gachas```となってしまい不都合があるので明示的に```Gacha```となるよう設定しています。

## コードを自動生成する

設定ファイルが作成できたら以下のコマンドを実行してコードを自動生成します。

```
% sqlboiler mysql
```

エラーなく実行できると以下のようにファイルが生成されているはずです。

```
% tree db 
db
├── boil_queries.go
├── boil_table_names.go
├── boil_types.go
├── boil_view_names.go
├── gacha_items.go
├── gachas.go
├── items.go
├── mysql_upsert.go
└── schema_migrations.go
```

これでアプリケーションを作成するための準備は完了です。