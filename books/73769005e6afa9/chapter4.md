---
"title": "マイグレーション"
---

前回まででDockerコンテナを起動しアプリケーションで使用するDBの準備ができたので、本章ではアプリケーションで使用するガチャデータを準備します。データの準備には[go-migrate](https://github.com/golang-migrate/migrate)を使用したいと思います。

## go-migrate

go-migrateはGoで作成されたマイグレーションツールでCLIでの実行とGoプロジェクトからのインポートによるコードからの実行の両方をサポートしています。サポートしているDBも多く、GitHubなどに配置されたマイグレーションファイルを直接インターネット経由で使用しマイグレーションを実行できることが特徴です。

### install

MacであればHomebrewでCLIツールをインストールすることが可能です。

```
% brew install golang-migrate

% migrate --version
v4.16.2
```

また、後述しますがDBを使用したテストを実行する際にテストコードからマイグレーションを実行したいためプロジェクト側にもライブラリとしてインストールしておきます。

```
go get -u github.com/golang-migrate/migrate/v4
```

## マイグレーションファイルを作成する

マイグレーションファイルは以下のようなコマンドを実行することで作成することができます。

```
% migrate create -ext sql -dir migrations -seq create_table

% tree migrations
migrations
├── 000001_create_table.down.sql
└── 000001_create_table.up.sql
```

- ```-ext sql``` 作成するファイルの拡張子に```sql```を指定。
- ```-dir migrations``` ファイルを作成するディレクトリを指定する。今回は```migrations```というディレクトリ配下に作成。ディレクトリは自動で作成される。
- ```-seq``` ファイル名に連番の数字を含める。デフォルトはタイムスタンプ。
- ```create_table``` マイグレーション名。ファイル名に含まれる。

ファイルは```up```と```down```の2種類が作成され、マイグレーションバージョンを上げる時のSQLと下げるときのSQLを記述する。

:::details 000001_create_table.up.sql
```sql
-- -----------------------------------------------------
-- Table `gachas`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gachas` (
  `id` INT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `items`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `items` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `rarity` VARCHAR(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `gacha_items`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gacha_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `gacha_id` INT NOT NULL,
  `item_id` BIGINT NOT NULL,
  `weight` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_gacha_items_items_idx` (`item_id` ASC) VISIBLE,
  INDEX `fk_gacha_items_gachas1_idx` (`gacha_id` ASC) VISIBLE,
  CONSTRAINT `fk_gacha_items_items`
    FOREIGN KEY (`item_id`)
    REFERENCES `items` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_gacha_items_gachas1`
    FOREIGN KEY (`gacha_id`)
    REFERENCES `gachas` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
```
:::

::: details 000001_create_table.down.sql
```sql
DROP TABLE IF NOT EXISTS gacha_items;
DROP TABLE IF NOT EXISTS gachas;
DROP TABLE IF NOT EXISTS items;
```
:::

## マイグレーションを実行する

マイグレーションを実行するには以下のようなコマンドで実行することができます。

```
migrate --path migrations \
  --database "mysql://$MYSQL_USER:$MYSQL_PASS@tcp($MYSQL_HOST:$MYSQL_PORT)/$MYSQL_DBNAME" \
  -verbose up
```

- ```--path migrations``` マイグレーションファイルが配置されているパスを指定します。
- ```--database``` マイグレーションの実行先のDBを指定します。

一応、DBに接続してテーブルが作成されていることを確認します。前回作成しておいたタスクを実行することでDBに接続することが可能です。

```
% task db:connect
```

```
mysql> show tables;
+----------------------+
| Tables_in_gacha-demo |
+----------------------+
| gacha_items          |
| gachas               |
| items                |
| schema_migrations    |
+----------------------+
4 rows in set (0.01 sec)
```

テーブルの作成が確認できたら同じような手順でデータのインサートするマイグレーションも実行しておきます。ガチャの抽選には重み付け抽選による抽選をするため、登録する各アイテムにはそれぞれ```weight```を設定しています。

:::details 000002_insert_gacha_data.up.sql
```sql
insert into gachas values(1,"gacha1");

insert into items values(1,"item1","N");
insert into items values(2,"item2","N");
insert into items values(3,"item3","N");
insert into items values(4,"item4","N");
insert into items values(5,"item5","N");
insert into items values(6,"item6","R");
insert into items values(7,"item7","R");
insert into items values(8,"item8","R");
insert into items values(9,"item9","R");
insert into items values(10,"item10","SR");

insert into gacha_items values(null,1,1,10);
insert into gacha_items values(null,1,2,10);
insert into gacha_items values(null,1,3,10);
insert into gacha_items values(null,1,4,10);
insert into gacha_items values(null,1,5,10);
insert into gacha_items values(null,1,6,5);
insert into gacha_items values(null,1,7,5);
insert into gacha_items values(null,1,8,5);
insert into gacha_items values(null,1,9,5);
insert into gacha_items values(null,1,10,1);
```
:::

:::details 000002_insert_gacha_data.down.sql
```sql
delete from gacha_items;
delete from gachas;
delete from items;
```
:::

## タスク化する

Dockerコマンド同様、マイグレーションのコマンドも実行するには少し長いのでタスクとして登録しておきます。

```yaml
vars:
  CONTAINER_NAME: go-unittest-architecture-db
  MIGRATIONS_PATH: migrations
```

```yaml
  migrate:create:
    cmds:
      - migrate create -ext sql -dir {{.MIGRATIONS_PATH}} -seq {{.CLI_ARGS}}
    desc: 'Create migration file.Migration name must be specified as an argument.ex) task migrate:create -- create_user_table'
  migrate:up:
    cmds:
      - migrate --path {{.MIGRATIONS_PATH}}
        --database "mysql://$MYSQL_USER:$MYSQL_PASS@tcp($MYSQL_HOST:$MYSQL_PORT)/$MYSQL_DBNAME"
        -verbose up
    desc: 'Execution migration up.'
```

```{{.CLI_ARGS}}```はtaskコマンドの引数を指定した場合にその値が格納されます。taskコマンドに引数を指定するには```--```のあとに指定したい引数を指定します。

```
% task migrate:create -- create_tables
```