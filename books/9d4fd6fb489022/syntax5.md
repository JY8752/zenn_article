---
title: "Cadenceの基礎[StructとResource]"
---

このチャプターではCadenceにおいて重要な役割を果たすStructとResourceについて紹介いたします。

## Struct(構造体)

Cadenceにはクラスの概念はありませんが複数のデータ型を保持するデータの塊を表現したいときには```Struct(構造体)```を定義して使用することができます。Structの定義には```struct```キーワードを使用し、続いて構造体の名前を指定します。慣習的に構造体の命名は大文字始まりのキャメルケースで宣言します。構造体の中にはフィールドを定義することができ、フィールドの初期化には```init()```で必ず初期化する必要があります。構造体内で自身のフィールドにアクセスするには```self```を使用することでアクセスすることができます。

```ts
pub struct Token {
    pub let id: Int
    pub var balance: Int

    init(id: Int, balance: Int) {
        self.id = id
        self.balance = balance
    }
}
```

定義した構造体のインスタンスの作成とフィールドへのアクセスは以下のようにして行います。

```ts
// インスタンス化
let token = Token(id: 42, balance: 1_000_00)

token.id  // is `42`
token.balance  // is `1_000_000`

token.balance = 1

// NG idは定数のため書き換えできない
token.id = 23
```

また、構造体はフィールドだけでなく関数を定義することもできる。

```ts
pub struct Rectangle {
    pub var width: Int
    pub var height: Int

    init(width: Int, height: Int) {
        self.width = width
        self.height = height
    }

    // Declare a function named "scale", which scales
    // the rectangle by the given factor.
    //
    pub fun scale(factor: Int) {
        self.width = self.width * factor
        self.height = self.height * factor
    }
}

let rectangle = Rectangle(width: 2, height: 3)
rectangle.scale(factor: 4)
// `rectangle.width` is `8`
// `rectangle.height` is `12`
```

ちなみに、Cadenceでは継承や抽象型のようなものはサポートされていません。

## Resource

Cadenceはリソース指向プログラミング言語として開発されており、それを実現するための重要な機能がResourceです。Resourceは一度に一つの場所にしか存在できず、Resourceを作成・取得した場合はそのResourceを適切な場所に移動もしくは破棄する必要が言語仕様として定められており、適切にResourceが処理されていない場合、コンパイラによりエラーとなります。Resourceの定義は構造体と同じように宣言できますがキーワードに```resource```を使用し、Resourceを新たに作成(インスタンス化)するときには```create```というキーワードを使用し、変数への代入には代入演算子は使用することはできず、代わりに移動演算子である```<-```を使用します。また、Resourceを破棄する場合には```destroy```というキーワードを使用します。Resource型の型注釈を付ける場合には先頭に```@```を付ける必要があります。Resourceも構造体と同様慣習的に大文字始まりのキャメルケースで宣言します。

```ts
pub resource User {
  pub let name: String
  init(name: String) {
    self.name = name
  }
}

pub fun returnResource(user: @User): @User {
  return <- user
}

// リソースのインスタンス化
let user <- create User(name: "user")
// 別の変数にリソースを移動
let user2 <- user
// NG 既に移動しているのでアクセス不可
// user.name

// アクセス可能
user2.name

// 関数の引数にリソースを指定
let user3 <- returnResource(user: <- user2)

// リソースを破棄する
destroy user3
```

Resourceは定義内にデストラクタを１つだけ宣言することができ、リソースが破棄されるときに実行される処理を記述することができます。

```ts
pub resource User {
  pub let name: String
  init(name: String) {
    self.name = name
  }
  destroy() {
    log(self.uuid.toString().concat("が破棄されました。"))
  }
}
```

Resourceは暗黙な一意な識別子を持ち、```uuid```でアクセスすることができます。この識別子はリソースの作成時に自動的に設定され、Resourceが破棄された後もユニークであり、同じ識別子を持つResourceは存在しません。