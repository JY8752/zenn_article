---
title: "Atlas + Prismaで実現するTypeScript環境における安全で堅牢な宣言的マイグレーション"
emoji: "🔭"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["Prisma", "Atlas", "ts", "db", "マイグレーション"]
published: true
---

## はじめに

以前、Go製のマイグレーションツールである[Atlas](https://atlasgo.io/docs)についての紹介記事を書きました。

https://zenn.dev/jy8752/articles/f9fda2379b57f5

Atlasは従来のupとdownのマイグレーションスクリプトを開発者が書く**バージョン管理型のマイグレーション**ではなく、DBの理想状態を宣言的に記述することでスキーマを反映することができる**宣言的マイグレーション**を実現しています。宣言的なプログラミングは昨今の技術トレンドのように個人的には感じており、k8sやTerraformといった技術も理想の状態を宣言的に記述しその状態に近づけるといったアプローチを取っていると思います。Atlasの公式ドキュメントにも以下のようにAtlasは**DB版のTerraform**と呼ばれるということが記載されています。

> Atlas - is a database schema-as-code tool that applies modern DevOps principles to the ancient art of database schema management. Many of its users call it a "Terraform for Databases" because of its schema-first, declarative approach.

ちなみに、Atlasは宣言的マイグレーションだけでなくバージョン管理型のマイグレーションもサポートしており、その場合はマイグレーションファイルが作成されますがスキーマからマイグレーションファイルは自動で作成されるため開発者がマイグレーションのためのDDLを考える必要はなく、さらに`atlas.sum`というファイルによりマイグレーションファイルの競合を事前に察知することができるためマイグレーションが失敗する前にマイグレーションファイルを修正することが可能となっています。

:::message
Atlasは従来型のマイグレーションと同じようにマイグレーションファイルを生成するバージョン管理型のマイグレーションをサポートしています。しかし、Atlasのバージョン管理型のマイグレーションは従来型の理想のスキーマの状態に近づけるためのDDLを開発者が自ら書く命令的なマイグレーションではなく、宣言的マイグレーション同様理想のスキーマから自動でマイグレーションファイルが生成されます。これはAtlasの宣言的マイグレーションは人間のレビューを必要とする場面も多く、バージョンファイルを生成してレビューを可能としたほうが良いケースも多いため提供している従来型の課題を解決しつついいとこどりしたようなマイグレーション方法です。
:::

もし、Atlasについてもう少し詳しく知りたい方は上記記事もご参照ください。

このように、高機能で宣言的マイグレーションを実現してくれるAtlasですがGo製ではあるもののプログラミング言語を問わず利用することが可能であることと、スキーマの管理にはhcl、sqlファイルに加え各言語の主要なORMスキーマをサポートしていることから**TypeScript環境においてPrismaとAtlasを組み合わせて使えないか**という検証をしてみたのでその備忘録的な記事となっております。主に以下のスクラップの内容をまとめています。

https://zenn.dev/jy8752/scraps/2c7dfe6054a01f

:::message
先に言うと思っていたよりもPrismaのマイグレーション機能が優秀なうえにAtlasの機能の多くがFreeプランでは使えないこともあり、AtlasをPrismaに組み込むメリットは想定していたよりもあまりなかったです。とはいえ、Atlasを組み込むメリットは十分あると個人的には思っています。
:::

## 対象読者

- Atlasのような宣言的マイグレーションについて興味がある方
- Prismaのマイグレーションについて知りたい方
- TypeScript環境におけるDBマイグレーションについて興味がある方

## Atlasとは

Go製の次世代マイグレーションツールです。宣言的マイグレーションと従来のバージョン管理型のマイグレーションの両方をサポートしており、かなり高機能なツールです。詳しくは前述の紹介記事も参照ください。

前述したようにAtlasは主要なORMをサポートしており、PrismaやDrizzleもサポートしています。詳しくは以下を参照ください。

https://atlasgo.io/orms/why-atlas-for-your-orm

:::message
余談ですが本記事執筆のために公式ドキュメントを読んでいて知ったのですが、Atlasの開発チームはGo製のORMであるentのメンテナンスもしているらしくORM制作者的な視点も持っており、信頼感があります。
:::

## Prismaとは

TypeScript環境におけるORMです。独自のDSLを用いてスキーマを定義することができ、マイグレーション機能も提供しています。最近、TypedSQLが発表されて個人的にも熱いなと思ってます。詳しくは公式ドキュメントを参照ください。

https://www.prisma.io/docs/orm

## Prismaのマイグレーションの仕組み

PrismaにAtlasを取り入れていく前に一旦Prismaのマイグレーション機能について見ていきましょう。

### Prismaのマイグレーションの流れ

Prismaでマイグレーションの機能を使用する場合、以下のようなスキーマファイルをまず作成しておく必要があります。(以下は公式ドキュメントから引用)

```ts:schema.prisma
datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

model User {
  id    Int    @id @default(autoincrement())
  name  String
  posts Post[]
}

model Post {
  id        Int     @id @default(autoincrement())
  title     String
  published Boolean @default(true)
  authorId  Int
  author    User    @relation(fields: [authorId], references: [id])
}
```

このスキーマファイルをDB環境に反映するためには以下のようなコマンドを実行します。

```bash
prisma migrate dev --name init
```

`prisma migrate dev`コマンドはスキーマファイルと実際のDB環境との差分からマイグレーションファイルを作成し、かつ、DBへの差分反映まで実行してくれるコマンドです。実際にコマンドを実行した場合、以下のようなマイグレーションファイルが作成されます。

```bash
migrations/
  └─ 20210313140442_init/
    └─ migration.sql
```

実際には`prisma migrate dev`コマンドはマイグレーション履歴テーブルの更新やPrisma Clientの生成なども実行します。このコマンドは開発中専用のコマンドとなっており、スキーマの更新を行った際にこのコマンドを実行することでローカルのDB環境の更新、マイグレーションファイルの生成、Prisma Client生成のトリガーなどをまとめて実行してくれる便利なコマンドです。

スキーマの変更をローカルのDBに反映するもう一つの方法として`prisma db push`コマンドを実行する方法もあります。このコマンドはスキーマファイルを作成せずにDBにスキーマを反映するコマンドです。この説明だけ聞くとAtlasが実現している宣言的マイグレーションのように感じてしまいますが`prisma db push`コマンドを本番環境に対して実行するのは**非推奨**です。

なぜならば、`prisma db push`コマンドはDB上のデータ損失の可能性があるためプロトタイプやローカル環境においてなど試行錯誤を有するケースにおいて使用することを推奨しています。例えば、`prisma db push`コマンドはカラム名の変更を自動的に検知する仕組みがないためDBのリセットが必要になります。`prisma db push`コマンドはスキーマとDBの状態を同期させることのみを目的としその過程本番環境には使えませんが手元でガチャガチャやるときには非常に便利な便利なコマンドでしょう。

本番環境への適用においては作成済みのマイグレーションファイルを適用する以下のコマンドを実行します。

```bash
prisma migrate deploy
```

### Prismaのマイグレーション履歴の管理

Prismaのマイグレーション履歴は以下の2つによって管理されています。

- `prisma/migrations`配下のマイグレーションファイル
- データベース内の`_prisma_migrations`テーブル

`prisma/migrations`配下のマイグレーションファイルはデータベース変更の**正式な情報源**です。そのため、これらのマイグレーションファイルを**削除・編集**することは基本的にはしてはいけません。

これらのマイグレーションファイルの適用状態を格納しているのが`_prisma_migrations`テーブルです。以下は公式ドキュメントから引用してきた基本的なPrismaのマイグレーションのフローです。

![prisma migrate dev flow](https://storage.googleapis.com/zenn-user-upload/6ad122304094-20250916.png)

上記2つに加え、Prismaのマイグレーションにおいて**shadow database**(シャドウデータベース)という仕組みが重要な働きをしています。

`prisma migrate dev`コマンドを実行するたびに、Prismaは毎回shadow databaseと呼ばれるデータベースを作成します。これはマイグレーションファイルの内容を`_prisma_migrations`テーブルを元にshadow databaseに対し反映することで開発用のデータベースと比較し予期せぬ差分が発生していないかを検知することが可能となっています。これは**スキーマドリフトの検出**などと言われているものです。

また、未適用のマイグレーションをshadow databaseに対して事前に適用することでデータ損失の可能性がないかなどを事前に検知することができるようになっています。この仕組みによってPrismaでは安全にデータベーススキーマを変更することができるようになっています。(データ損失をせずにすべてのマイグレーションを実行できるわけではありません。)

:::message
SupabaseやPlanetScalseのようなクラウドベースのDBを使っている場合、パーミッションの関係でシャドウデータベースの作成に失敗してしまうことがあります。その場合、手動でシャドウデータベースを作成するなどの対応が必要となってきます。
:::

:::message
ちなみに、AtlasではDockerコンテナを一時的に起動してPrismaのshadow databaseと同様のことをしています。 [参考](https://atlasgo.io/concepts/dev-database)
:::

## Atlasのマイグレーションの仕組み

Prismaのマイグレーションの基本的な仕組みを理解したところでAtlasについてもみていきましょう。

### Atlasを使ったマイグレーションの利点

Atlasには大きく以下のような特徴があります。

- バージョン管理型のマイグレーションと宣言的なマイグレーションの2種類のサポート
- `schema as code`の実現
- 言語非依存
- 自動マイグレーション
- トリガーやRLSといった高度なDB機能のサポート
- GitHub ActionsなどのCI/CD環境、Kubernetes Operator, TerraformといったDevOps環境との高度な統合
- plan, lint, testといった機能による事前チェックで安全性の高いマイグレーション環境の実現
- Atlas Cloudの提供
- 豊富な種類のデータベースをサポート

宣言的マイグレーションによりアプリケーション開発者はスキーマの理想状態を考えるだけで良くなります。理想状態のスキーマを反映するのはインフラエンジニアの責務とすることができます。Atlasは高度で現代的なDevOps環境へDBマイグレーションを組み込むことを目的としているように思え、この宣言的マイグレーションによりアプリケーション開発者とインフラエンジニアの境界を明確にすることが可能です。

宣言的マイグレーションもバージョン管理型のマイグレーションも**理想状態のスキーマ**から自動でマイグレーションを実行するため開発者が手動でマイグレーションファイルを編集する必要がありません。理想状態のスキーマはSQL, HCL, ORMをサポートしており、Atlas自体はGoで作られていますが言語非依存なためどんな開発環境でも利用することが可能となっています。今回のテーマとなっているPrismaのスキーマファイルをAtlasで使用することができるのもAtlasが主要ORMをサポートしているからです。

また、Atlasは[Atlas Cloud](https://atlasgo.io/cloud/getting-started)というSaasサービスを提供していたり、GitHub ActionsのようなCI/CD環境で実行できるplanningやlint, testといった機能を数多く提供してくれており、DevOpsの運用への組み込みがかなり手厚くなっているように感じます。Prismaもそうですし、Atlasもそうですが、マイグレーションが失敗するケースやデータ損失につながるような安全でないマイグレーションを0にはできないので、こういった事前チェックする仕組みは重要で特に大規模なチーム開発やエンプラ系の開発においては重要となってくるのではないでしょうか。

さらに、Atlasは数多くのDBをサポートしておりGoogle CloudのSpannerなどもサポートしています。

### AtlasのORMサポートについて

通常、ORMはマイグレーションツールを内包していることが多いですが、これはORMを正常に機能させるために必要であって、かつ、異なるデータベース間でも動作するようにするため特定のDB固有の機能はあまりサポートされず最低限の実装がされていることが多いそうです。

AtlasをORMに対して使うということは基本的にはORM内臓のマイグレーション機能をAtlasに置き換えるということになります。公式ドキュメントによるとAtlasをORMと連携して使用するユースケースとしては以下のようなものがあるそうです。

- **自動マイグレーション** 従来のマイグレーションツールは開発者が手動でDDLなどを書く必要があったがAtlasを使うことで自動でマイグレーションを実行することができる。Atlasには従来型のバージョン型のマイグレーションと宣言的なマイグレーションの2種類がありますがどちらも自動マイグレーションをサポートしています。
- **高度なDB機能のサポート** 多くのマイグレーションツールではトリガーやストアドプロシージャ、RLSといった高度なDB機能をサポートしていないことがほとんどです。Atlasはこういった高度なDB機能をサポートしています。
- **プラットフォームの構築** 組織内に既にプラットフォームチームが存在している、もしくは、これから新規で立ち上げていくといった場合、Atlasはk8sやTerraform, GitHub ActionsなどのCI/CD環境との統合をサポートしています。

以下の公式ドキュメントにあるORMサポートについてのページが非常にわかりやすい & 面白かったのでよければご覧ください。

https://atlasgo.io/orms/why-atlas-for-your-orm

### `prisma migrate` とAtlasのマイグレーションの比較

上記でORMにAtlasを組み込むユースケースを紹介しましたが、Prismaのマイグレーション機能をAtlasに置き換えることを考えていきましょう。

自動マイグレーションに関しては前述のとおりPrismaもサポートしています。そのため、Atlasを導入せずとも開発者が手動でマイグレーションを書く必要はありません。また、PrismaはAtlasのバージョン型マイグレーション同様マイグレーションファイルを生成します。Atlasの宣言的マイグレーションを導入したい場合はAtlasの導入が必要ですがバージョン型のマイグレーションであればPrisma内臓のマイグレーション機能でも実現可能です。

RLSなどの高度なDB機能に関してはPrismaはサポートしていないので、それらもマイグレーション管理したい場合、Atlasの導入が必要ですが**Proプラン以上の契約**が必要です。Prismaはマイグレーションファイルを生成しますが、Prismaのスキーマを正としてその状態を目標にマイグレーションを実行するため**宣言的マイグレーション**と言えます。そして、Prismaは生成したマイグレーションファイルに対して自由に記述できるため、従来のマイグレーション機能のように手動でサポートしていない高度なDB機能を記述してマイグレーション管理することが可能なため、Atlasを導入しなくても高度なDB機能をマイグレーション管理することは可能です。Prismaのマイグレーションはこのように宣言的なマイグレーションと従来の命令型のマイグレーションのハイブリッドなマイグレーション機能といえます。

所属している開発組織が大規模な組織だったりプラットフォームチームやインフラチームがアプリケーション開発チームと別で存在するような場合はAtlasの導入は検討の余地があるでしょう。逆に、小規模な開発チームだったりする場合はアプリケーション側とマイグレーションの機能は密になっていたほうがやりやすかったりするでしょう。(prisma migrate devやprisma db pushは手元の開発時にはやはり便利)

ここでAtlasの導入メリットをPrismaの機能と比較して見てみましょう。

| 機能 | Atlas | Prisma |
|------|-------|--------|
| **マイグレーション方式** | バージョン管理型・宣言的マイグレーションの両方をサポート | バージョン管理型マイグレーション（宣言的マイグレーションは`db push`で可能だが本番非推奨） |
| **Schema as Code** | SQL、HCL、ORMスキーマをサポート | Prismaスキーマで実現 |
| **言語対応** | 言語非依存（Go製だが全言語で利用可能） | TypeScript/JavaScript中心 |
| **自動マイグレーション** | 完全自動（手動編集も可能） | 自動マイグレーション（手動編集も可能） |
| **高度なDB機能** | トリガー、RLS、ストアドプロシージャ等をサポート | 手動でマイグレーションファイルに記載することで対応 |
| **DevOps統合** | GitHub Actions、Kubernetes Operator、Terraform等との高度な統合 | 基本的なCI/CD統合のみ |
| **事前チェック機能** | plan、lint、test等の豊富な事前チェック機能 | `prisma format`、`prisma validate`等のスキーマ検証のみ |
| **SaaSサービス** | Atlas Cloudを提供 | なし |
| **データベースサポート** | 豊富（Spanner等も含む） | 主要DB + Supabase、PlanetScale等（Spannerは非対応） |

このように、Prismaの内臓マイグレーションの機能は高水準でありAtlasを導入せずとも対応している特徴が多くありますが、Atlasを導入することで得られるメリットも多くあります。

## AtlasをPrismaに組み込む

前置きが長くなりましたがPrismaにAtlasを組み込んでみましょう。

atlasのCLIコマンドがインストールされていない方は以下でインストール

```bash
curl -sSf https://atlasgo.sh | sh
// or Macなら以下
brew install ariga/tap/atlas

atlas version
> atlas version v0.36.3-c755e2a-canary
> https://github.com/ariga/atlas/releases/latest
```

```bash
pnpm init
pnpm add -D prisma
pnpm approve-builds
```

### PrismaのスキーマをAtlasに渡す

以下のコマンドで`schema.prisma`を生成します。

```bash
pnpm prisma init --datasource-provider postgresql 
```

AtlasでPrismaのスキーマを使用するにはスキーマをSQL形式にエクスポートする必要があります。プロジェクトのルートディレクトリに`atlas.hcl`というファイルを作成し、以下のように`external_schema`の設定を記述します。

```hcl:atlas.hcl
data "external_schema" "prisma" {
    program = [ 
      "npx",
      "prisma",
      "migrate",
      "diff",
      "--from-empty",
      "--to-schema-datamodel",
      "prisma/schema.prisma",
      "--script"
    ]
}

env "local" {
  dev = "docker://postgres/16/dev?search_path=public"
  schema {
    src = data.external_schema.prisma.url
  }
  migration {
    dir = "file://atlas/migrations"
  }
}
```

`external_schema`はスキーマを外部からAtlasに渡すことができるリソースです。Atlasに渡すスキーマはPrismaとAtlasの両方でサポートされているSQL形式である必要があるため、prismaのコマンドを実行してSQL形式のスキーマ情報を出力しています。スキーマは空の状態と現在のスキーマを比較し完全なDDLを出力します。

ここまでできたら一旦、スキーマにモデルを追加してみましょう。

```prisma:schema.prisma
model User {
  id    Int    @id @default(autoincrement())
  name  String
  email String @unique
}
```

スキーマにモデルを追加できたら以下のコマンドで試しにPrismaのスキーマをAtlasに渡せているかを確認してみましょう、

```bash
atlas schema inspect --env local --url env://schema.src --format "{{ sql . }}"

-- Create "User" table
CREATE TABLE "User" (
  "id" serial NOT NULL,
  "name" text NOT NULL,
  "email" text NOT NULL,
  PRIMARY KEY ("id")
);
-- Create index "User_email_key" to table: "User"
CREATE UNIQUE INDEX "User_email_key" ON "User" ("email");
```

上記のようにDDLが出力されればokです！Prismaのスキーマから正しくAtlasに渡せています。

### PrismaのスキーマをAtlas経由で反映する

先にローカル環境にDBを準備しましょう。今回は以下のようなdocker composeを用意します。

```yaml:compose.yaml
name: postgres-db

services:
  db:
    image: postgres:17-alpine
    restart: unless-stopped
    environment:
      POSTGRES_USER: ${POSTGRES_USER:-postgres}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-password}
      POSTGRES_DB: ${POSTGRES_DB:-myapp}
    ports:
      - ${POSTGRES_PORT:-5432}:5432

```

```bash
docker compose up -d
```

DBの準備ができたら以下のコマンドを実行してスキーマを反映してみましょう。

```bash
atlas schema apply --env local --url "postgresql://postgres:password@:5432/myapp?search_path=public&sslmode=disable"
Planning migration statements (2 in total):

  -- create "user" table:
    -> CREATE TABLE "User" (
         "id" serial NOT NULL,
         "name" text NOT NULL,
         "email" text NOT NULL,
         PRIMARY KEY ("id")
       );
  -- create index "user_email_key" to table: "user":
    -> CREATE UNIQUE INDEX "User_email_key" ON "User" ("email");

-------------------------------------------

Analyzing planned statements (2 in total):

  -- no diagnostics found

  -------------------------
  -- 43.302125ms
  -- 2 schema changes

-------------------------------------------

? Approve or abort the plan:
  ▸ Approve and apply
    Abort
```

上記のようにクエリの実行計画と承認を求める出力がされると思いますので、問題なければ承認することでクエリが実行されます。
以下のようにUserテーブルが作成されていることが確認できました！

```bash
docker exec -it postgres-db-db-1 psql -U postgres -d myapp
psql (17.6)
Type "help" for help.

myapp=# \d "User"
                             Table "public.User"
 Column |  Type   | Collation | Nullable |              Default
--------+---------+-----------+----------+------------------------------------
 id     | integer |           | not null | nextval('"User_id_seq"'::regclass)
 name   | text    |           | not null |
 email  | text    |           | not null |
Indexes:
    "User_pkey" PRIMARY KEY, btree (id)
    "User_email_key" UNIQUE, btree (email)
```

もう一度`atlas schema apply`を実行するとスキーマとDBが完全に同期されているため何も実行されないことも確認できます。

```bash
atlas schema apply --env local --url "postgresql://postgres:password@:5432/myapp?search_path=public&sslmode=disable"
Schema is synced, no changes to be made
```

このマイグレーション方法はAtlasの宣言的マイグレーションという方式です。バージョン管理型マイグレーションのアプローチを取る場合、以下のコマンドを実行してマイグレーションファイルを生成します。

```bash
atlas migrate diff --env local
```

上記のコマンドを実行すると`atlas/migrations`ディレクトリ配下にマイグレーションファイルと`atlas.sum`ファイルが生成されます。

```bash
atlas
└── migrations
    ├── 20250917155657.sql
    └── atlas.sum
```

## Atlas + Prismaのメリットを考える

前述したようにPrismaは高水準のマイグレーション機能を有しているので、Atlasを導入するメリットを既に多く持っていますが、Atlasを導入することで得られるメリットはまだ多くあるのでそれらについてみていきましょう。

### マイグレーションファイルの競合解決

従来型のマイグレーションでしばしば問題に上がるのがチーム開発をしていてマイグレーションファイルが同じタイミングで作成されてしまうときがあることです。機能Aと機能Bをチーム内で別々の開発者が実装しているときに、どちらもマイグレーションが伴う場合、先にマージした方は問題ないですが後からマージした方はマイグレーションに失敗することがあります。従来型のマイグレーションはタイムスタンプで管理されているものが多く、適用済みのマイグレーションより古いタイムスタンプのマイグレーションを実行することができません。

Prismaではタイムスタンプ付きのマイグレーションファイルが生成されますが、実際は履歴テーブルを見て、未適用のマイグレーションを実行するため、従来型のマイグレーションに比べればマイグレーションが失敗することは少ないです。が、例えば同じテーブルに同じインデックスを貼る変更を2つのブランチで行ってしまった場合、あとからマージするブランチはスキーマのほうでコンフリクトが発生するとは思いますが、マイグレーションファイルの方は競合しません。スキーマのコンフリクトでマイグレーションファイルの重複に気づければいいですが気づかないでマージしてしまうとマイグレーションが失敗してしまいます。

Atlasではマイグレーションファイルとともに`atlas.sum`というファイルが生成されます。これは以下のような各マイグレーションファイルのハッシュ値とその合計が記載されています。

```bash
h1:KRFsSi68ZOarsQAJZ1mfSiMSkIOZlMq4RzyF//Pwf8A=
20220318104614_team_A.sql h1:EGknG5Y6GQYrc4W8e/r3S61Aqx2p+NmQyVz/2m8ZNwA=
```

マイグレーションファイルが新たに生成されると合計値が変わるので、ほとんどの場合マイグレーションファイルが同時に生成されるとコンフリクトが発生することになります。もしコンフリクトした場合は最新の内容を取り込んだあとに自分たちのマイグレーションが最新でなければ`atlas migrate rebase`コマンドを実行してリベースすることができます。スキーマレベルで競合が発生した場合はコンフリクトを解消した後、`atlas migrate hash`コマンドを実行してハッシュ値を再計算できます。

詳しくは以下のリンクを参照ください

https://atlasgo.io/concepts/migration-directory-integrity

Prismaでも従来型のマイグレーションに比べればマイグレーションエラーは起こりづらくなっていると思いますがAtlasを用いた方がより安全で堅牢なマイグレーション管理が可能となるでしょう。

### 高度なDB機能のサポート

(AtlasのProプラン以上の契約が必要)

トリガーやRLS、Postgresの拡張機能など高度なDB機能も含めてマイグレーション管理したい場合、Atlasを用いることで可能となりますが、AtlasのProプラン以上の契約が必要となります。

一応ですが、空のマイグレーションファイルを生成して手動で記載することでマイグレーション管理をすることは可能です。以下はPostgresの部分インデックスを作成する例です。

```bash
atlas migrate new create-partial-index --env local
```

```sql:20250917160000_create-partial-index.sql
CREATE INDEX idx_verified_users ON "User"("email") WHERE "isVerified" = true;

CREATE INDEX idx_verified_premium_users ON "User"("email", "createdAt") 
WHERE "isVerified" = true AND "email" LIKE '%@premium.com';
```

```bash
atlas migrate apply --env local --url "postgresql://postgres:password@:5432/myapp?search_path=public&sslmode=disable"
```

### DevOps環境と統合したマイグレーションパイプラインの構築

(AtlasのProプラン以上の契約が必要)

これはインフラやプラットフォームチームが開発チームと別で存在するような大規模組織や堅牢なマイグレーションの仕組みが必要な場合にAtlasの各種統合機能やlintやテスト、マイグレーション事前チェックの機能などは非常に有用でしょう。

Atlas Cloudを用いたDBマイグレーションパイプラインの構築やk8s環境、Terraform環境との統合がAtlasの機能を用いることで可能となるでしょう。しかし、これは前述の通り大規模組織向けなのとこれらほとんどの機能がProプラン以上の契約が必要となります。

### Spannerのサポート

(AtlasのProプラン以上の契約が必要)

PrismaでサポートされていないDBもAtlasではサポートされています。Google CloudのSpannerもAtlasではサポートしています。

### lintやテスト機能による堅牢なマイグレーション事前チェック

前述していますがAtlasではlintやテスト機能、事前チェック機能などマイグレーションを安全に実施するための機能が豊富に用意されています。これらの機能は多くがProプラン以上の契約が必要となりますが、lint機能に関しては基本機能であればFreeプランでも利用可能です。

Atlasでlintを実行するには`atlas migrate lint`コマンドを実行します。このコマンドを実行することで破壊的変更やテーブルロックといった危険な変更を事前に検知することが可能です。lintコマンドはどのバージョンに対して実行するかを指定する必要があります。具体的には以下です。

- Atlas Registryにpushされた最新のマイグレーションとローカルのマイグレーションディレクトリを比較
- 最新のN個のマイグレーションファイルを指定
- 特定のブランチとの比較

例えば、ローカルにある最新のマイグレーションファイルに対して実行する場合は以下のように実行します。

```bash
atlas migrate lint --env local --latest 1
```

以下にlintルールがまとまっているので興味がある方はご参照ください。

https://atlasgo.io/lint/analyzers#checks

また、PrismaにおけるAtlasのlint機能の使いどころとして**命名規則の強制**があります。PrismaではPascalケースもしくはCamelケースでモデルを定義しますが、テーブル名も同様になるため`User`というモデルを定義すると`User`というテーブルが生成されます。しかし、データベースのテーブル名として小文字のスネークケースで統一したいと思う方もいるでしょう。そのような場合、Prismaでは以下のように`@@map`ディレクティブを使用することができます。

```prisma:schema.prisma
model Comment {
  id      Int    @id @default(autoincrement())
  content String @map("comment_text")
  email   String @map("commenter_email")

  @@map("comments")
}
```

Atlasでは`atlas.hcl`にlintルールを記述することで命名規則を強制することができます。

```diff hcl:atlas.hcl
env "local" {
  dev = "docker://postgres/16/dev?search_path=public"
  schema {
    src = data.external_schema.prisma.url
  }
  migration {
    dir = "file://atlas/migrations"
  }
+   lint {
+     review = WARNING
+     naming {
+       error   = true
+       match   = "^[a-z_]+$"                 # regex to match lowercase letters
+       message = "must be lowercase letters" # message to return if a violation is found
+     }
+   }
}
```

この状態で`atlas migrate lint`コマンドもしくは`atlas schema apply`コマンドを実行することで命名規則のチェックが実行されます。

```bash
atlas migrate lint --env local --latest 1
Analyzing changes until version 20250917155657 (1 migration in total):

  -- analyzing version 20250917155657
    -- naming violations detected:
      -- L2: Table named "User" violates the naming policy: must be lowercase letters
         https://atlasgo.io/lint/analyzers#NM102
      -- L9: Index named "User_email_key" violates the naming policy: must be
         lowercase letters
         https://atlasgo.io/lint/analyzers#NM104
  -- ok (307.167µs)

  -------------------------
  -- 119.561209ms
  -- 1 version with errors
  -- 2 schema changes
  -- 2 diagnostics
```

余談ですがムーザルちゃんねるでPrismaの命名の話が出ていたので紹介しておきます😇

https://youtu.be/oMAa1RJljlo?si=Z22t7QKOOEFSxobt

また、公式から提供されているGitHub Actionsを使うことでCI上でlintを実行することができます。

https://atlasgo.io/integrations/github-actions#arigaatlas-actionmigratelint

## まとめ

本記事では以下のことを紹介しました！

- Prismaのマイグレーションの仕組み
- Atlasのマイグレーションの仕組み
- AtlasをPrismaに組み込む方法
- AtlasをPrismaに組み込むメリット

冒頭にも書きましたが、AtlasをPrismaに組み込むことへの期待値をかなり高く持ってしまっていたのですが、Prismaのマイグレーション機能が思っていたよりもかなり優秀だったことと思いのほかAtlasの機能を使うには課金が必要なものが多かったため、AtlasをPrismaに組み込むメリットは想定よりも多くはなかったです。

特にAtlasが実現するスキーマを絶対的な正とした宣言的マイグレーションはPrismaは既に実現しています。加えて、スキーマとDBの状態からDDLを生成するマイグレーションエンジンはAtlasはかなり高度に解析して、DDLを生成してくれるようですがPrismaも同じ程度の水準で生成してくれるように思います。

:::message
Atlasの公式ドキュメントに記載されていますが、sqlite3のデフォルト値付きのカラムの変更はできないため、テーブルを作り直す必要があるそうです。AtlasとPrismaの自動マイグレーションはスキーマのこの変更から以下のようなDDLを生成してくれます。

```sql
PRAGMA defer_foreign_keys=ON;
PRAGMA foreign_keys=OFF;
CREATE TABLE "new_User" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "greeting" TEXT NOT NULL DEFAULT 'Hello, World!'
);
INSERT INTO "new_User" ("greeting", "id") SELECT "greeting", "id" FROM "User";
DROP TABLE "User";
ALTER TABLE "new_User" RENAME TO "User";
PRAGMA foreign_keys=ON;
PRAGMA defer_foreign_keys=OFF;
```

このDDLを開発者が手動で書くのはけっこう難しいでしょう。AtlasやPrismaはこういったデータベースによる特徴を考慮して開発者の手を煩わせずにDDLを生成してくれるのです。すごい！
:::

しかし、Atlasは宣言的マイグレーションだけでなくDevOps環境へDBマイグレーションを組み込むための機能が豊富に提供されているためこの部分はAtlasを導入するメリットになるでしょう。

無料の範囲だとそこまでメリットは多くありませんがPrismaのlint機能とマイグレーションの整合性を保つ仕組み、GitHub ActionsなどのCI環境への統合は特にチーム開発にとっては非常に重要な仕組みです。

Spannerを使っていたり、堅牢で安全なマイグレーションを実施したかったり、開発組織内にDevOpsやPlatformに精通するチームがあり、マイグレーションの仕組みを安全に自動化したいといった課題がある場合はAtlasのProプラン以上になることでAtlasのすべての機能を使うことができるためAtlasを導入するメリットは非常に大きいでしょう。

個人で使う分にはAtlas + Prismaはやりすぎ感があるかもしれませんがチーム開発においては無料プランの範囲だとしても十分にメリットはあると思いますので興味がある方はぜひ使ってみてください。

今回は以上です🐼

(Atlasを推す記事を書きたかったのにPrismaを推す記事になってしまった)
