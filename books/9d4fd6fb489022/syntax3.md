---
title: "Cadenceの基礎[関数]"
---

このチャプターではCadenceにおける関数の使用方法について紹介します。

## 関数の定義

関数を作成するには```fun```キーワードを使用して以下のようにします。

```ts
fun add(x: Int, y: Int): Int {
  return x + y
}

fun hello() {
  log("hello")
}
```

関数の引数がある場合は引数名とその型を記述する必要があります。また、関数の戻り値がある場合は戻り値の型を記述する必要があります。

## 引数のラベル名

定義した関数の呼びだしは以下のようにします。

```ts
let num = add(x: 1, y: 1)
hello()
```

Cadenceでは引数がある場合、そのラベル名を指定することが必須となっています。ラベル名の指定を省略したい場合、関数を定義するときに以下のように```_```をラベル名の前に指定します。

```ts
fun add(_ x: Int, _ y: Int): Int {
  return x + y
}

let num = add(1, 1)
```

また、Cadenceではデフォルト引数や可変長引数などはサポートしていません。

## 関数式

Cadenceでは関数は式としても扱うことができます。式として扱う場合は無名関数として宣言します。

```ts
let double =
    fun (_ x: Int): Int {
        return x * 2
    }
```

## View関数

読み取りのみの関数に```view```を識別子として付けることで更新などが行われない読み取りのみの関数であることが保証される。View関数のコンテキストで許可されない操作の例は以下の通りです。

- view関数以外の関数の呼び出し。
- リソースへの書き込みや修正
- リファレンスへの書き込みや修正

:::message

ドキュメントに明記されていないですが、おそらくコントラクト内でのみ定義が可能です。scriptやtransactionでview関数を定義しようとしてもコンパイラに認識されないです。(コントラクトの外でview関数を定義してもあまり意味がないので当然といえばそう)

また、view関数から非view関数の呼び出しができないとありますが以下のようにview関数の中で非view関数を呼び出してもエラーにならず正常に動作しました。もし、View関数の使用方法が誤っているなどあればコメントください。

```ts
pub contract User {
  pub var name: String

  view pub fun getName(): String {
    return self.name
  }

  pub fun updateName(name: String) {
    self.name = name
  }

  view pub fun updateAndGetName(name: String): String {
    self.updateName(name: name)
    return self.name
  }

  init() {
    self.name = "user"
  }
}
```

:::

### 事前条件と事後条件

Cadenceでは関数に事前条件と事後条件を設定することができる。事前条件は```pre```キーワードのあとにBool値を返す処理ブロックを記述し、事後条件は```post```キーワードのあとに同じくBool値を返す処理ブロックを記述します。postブロックでは```result```という特殊定数が使用することができ、これは関数の戻り値を使用することができます。また、```:```の後に文字列でエラー時のメッセージを指定することができます。

```ts
pub fun add(_ x: Int, _ y: Int): Int {
  pre {
    x > 0; y > 0:
      "xとyは正数で指定してください"
  }

  post {
    result <= 100:
      "計算結果が100を超えないように指定してください"
  }

  return x + y
}
```

postブロックでは特殊関数```before```が使用でき、これは関数が呼ばれる直前の式の値を取得することができます。

```ts
pub var n = 0

pub fun incrementN() {
    post {
        n == before(n) + 1:
            "n must be incremented by 1"
    }

    n = n + 1
}
```

## 組み込み関数

Cadenceに標準で組み込まれている関数はimportなしに使用することができます。

### panic

プログラムを無条件で終了させることができます。

```ts
fun panic(_ message: String): Never
```

```ts
let optionalAccount: AuthAccount? = // ...
let account = optionalAccount ?? panic("missing account")
```

### assert

与えられた条件がfalseの場合に、プログラムを終了させることができます。

```ts
fun assert(_ condition: Bool, message: String)
```

```ts
  let result = 1 + 1
  assert(result == 2, message: "計算結果は2である必要があります。")
```

### unsafeRandom

疑似乱数を生成します。乱数の使用にはセキュリティ上のリスクが伴う可能性がありますので公式ドキュメントのベストプラティクスなどに従ってください。