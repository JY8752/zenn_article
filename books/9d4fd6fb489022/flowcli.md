---
title: "Flow CLIの利用とCadenceプログラミングの実行"
---

Cadenceで作成したプログラムを一番手軽に動かすにはFlow Playgroundを利用することですが実際の開発ではローカル環境で開発できたほうがGitHubなどでコードの管理ができたりと利点が多いため本チャプターではローカル環境でCadenceプログラムを実行する方法について紹介いたします。

## Flowプロジェクトのセットアップ

Flow CLIの```setup```コマンドを利用することでFlowプロジェクトの雛形を作成することができます。

```ts
flow setup <プロジェクト名>

flow setup hello
```

成功すると以下のようなディレクトリ構造が作成されます。

```
.
├── README.md
├── cadence
│   ├── contracts // コントラクトの実装ファイルを配置する
│   ├── scripts // scriptファイルを配置する
│   ├── tests // testファイルを配置する
│   └── transactions // transactionファイルを配置する
└── flow.json // 設定ファイル
```

scriptsとtransactionsディレクトリに関しては別途チャプターにて説明いたしますので、一旦下記のHelloコントラクトの実装をcontracts配下に作成します。

```ts:Hello.cdc
pub contract Hello {
  pub fun hello(): String {
    return "Hello World!!"
  }
}
```

## Flowエミュレーターの起動

flow.jsonがある場所で以下のコマンドを実行し、エミュレーターを起動します。エミュレーターを起動する場合は、vオプションを指定することでlogが出力されるので指定することをおすすめします。

:::message
Flow CLIを用いた開発をする場合、エミュレーターとflow developコマンドをフォワードで実行しておく必要があり、コードの修正とflowコマンドの実行を繰り返すことになるためターミナルを最低３分割くらいにしておくと開発がやりやすいかもしれません。
:::

```
flow emulator -v
```

## Flow devの起動

エミュレーターが起動できたら別のターミナルを開き以下のコマンドでflow developコマンドを起動します。

```
flow dev
```

このコマンドを実行することでファイルの変更を検知し、コントラクトを自動でデプロイしてくれます。

## scriptの実行

scriptsディレクトリ配下にHelloコントラクトのhello関数を実行するscriptファイルを作成し配置します。

```ts:hello.cdc
import Hello from "../contracts/Hello.cdc"

pub fun main(): String {
  log("start script!!")
  return Hello.hello()
}
```

作成できたら以下のコマンドを実行し、scriptファイルを実行します。

```
flow scripts execute cadence/scripts/hello.cdc

> Result: "Hello World!!"
```

このように、Cadenceプログラムの動作を実行して確認したい場合、flowエミュレーターを起動し、scriptファイルを実行することで手軽にローカルで確認することができます。