---
title: "scriptファイルの作成と実行"
---

Flow上にdeployされたスマートコントラクトから情報を取得するためにはcadenceでscriptを記述し実行することでスマートコントラクトと対話することができます。

## scriptの実行

scriptは以下のように```pub fun main()```の中に処理を記述することで作成します。

```ts:Hello.cdc
pub contract Hello {
  pub var count: UInt64
  pub var id: UInt64

  pub let helloStoragePath: StoragePath
  pub let helloPublicPath: PublicPath

  pub fun hello(): String {
    self.count = self.count + 1
    return "Hello World!!"
  }

  view pub fun getCount(): UInt64 {
    return self.count
  }

  pub resource Token {
    pub let id: UInt64
    init(id: UInt64) {
      self.id = id
    }
  }

  pub fun mintHelloToken(): @Token {
    let token <- create Token(id: self.id)
    self.id = self.id + 1
    return <- token
  }

  init() {
    self.count = 0
    self.id = 0

    self.helloStoragePath = /storage/hello
    self.helloPublicPath = /public/hello

    self.account.save(<- self.mintHelloToken(), to: self.helloStoragePath)
    self.account.link<&Token>(self.helloPublicPath, target: self.helloStoragePath)
  }
}
```

```ts:get_hello_count.cdc
import Hello from "../contracts/Hello.cdc"

pub fun main(): UInt64 {
  Hello.hello()
  return Hello.getCount()
}
```

Helloコントラクトをimportしてhello()の呼び出しとgetCount()でcountを取得しています。scriptの戻り値がある場合はmain()の戻り値の型を指定することで戻り値を返すことができます。scriptの実行は```flow scripts execute <scriptファイルのパス> ```のようにFlow CLIのコマンドで実行します。

```
flow scripts execute cadence/scripts/get_hello_count.cdc

> Result: 1
```

scriptの実行は**データの参照のみで状態の変更はできません**。仮にデータの変更を行ったとしてもプログラム完了時に元に戻ります。

```
flow scripts execute cadence/scripts/get_hello_count.cdc

> Result: 1

// 2回実行してもcountが増えていないことがわかる
flow scripts execute cadence/scripts/get_hello_count.cdc

> Result: 1
```

## scriptに引数を指定する

scriptに引数を指定したい場合はmain()の引数に指定することで実行可能です。

```diff ts:Hello.cdc
+  pub fun helloName(name: String): String {
+    return "Hello, ".concat(name).concat("!!")
+  }
```

```ts:hello_name.cdc
import Hello from "../contracts/Hello.cdc"

pub fun main(name: String): String {
  return Hello.helloName(name: name)
}
```

```
flow scripts execute cadence/scripts/hello_name.cdc cadence 

> Result: "Hello, cadence!!"

```

## script内でアカウントを取得する

script内でアカウントのインスタンスを直接操作したい場合、組み込みの関数である```getAccount()```と```getAuthAccount()```を使用することができる。関数の引数には取得したいアカウントのアドレスをAddress型で渡す必要があります。取得したアカウントのパブリックに公開しているフィールドや機能を参照したり、AuthAccountであれば直接ストレージのオブジェクトの参照を取得したりすることが可能です。

```ts:get_token_id.cdc
import Hello from "../contracts/Hello.cdc"

pub fun main(addr: Address) {
  // PublicAccount
  let acc = getAccount(addr)
  let ref = acc.getCapability(Hello.helloPublicPath)
    .borrow<&Hello.Token>()
    ?? panic("Tokenの参照が取得できませんでした")
  log("token id")
  log(ref.id)

  // AuthAccount
  let authAcc = getAuthAccount(addr)
  let authRef = authAcc.borrow<&Hello.Token>(from: Hello.helloStoragePath)
    ?? panic("Tokenの参照が取得できませんでした")
  log("token id")
  log(authRef.id)
}
```