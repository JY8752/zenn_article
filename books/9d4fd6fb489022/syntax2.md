---
title: "Cadenceの基礎[演算子]"
---

このチャプターではCadenceで使用される主な演算子について紹介します。

## 四則演算

他のプログラミング言語同様以下のようにCadenceでも四則演算ができます。

```ts
  // 足し算
  let a = 1 + 1
  // 引き算
  let b = 2 - 1
  // 掛け算
  let c = 1 * 2
  // 割り算
  let d = 10 / 2
  // 余り
  let e = 6 % 5
```

## 移動演算子

Cadence特有のResource型の変数にResourceを代入するとき、代入演算子である```=```は使用することはできず、代わりに移動演算子(move operator)である```<-```を使用します。

```ts
  // NG
  let user = User(name: "user")

  // OK
  let user <- create User(name: "user")
```

また、強制代入演算子として```<-!```があり、Optional型の変数がnilである場合にリソースを代入することができる。Optional型の変数がnilでない場合はruntim errorとなる。

```ts
pub resource User {
  pub let name: String
  init(name: String) {
    self.name = name
  }
}

pub fun getUserResource(exist: Bool): @User? {
  if exist {
    return <- create User(name: "user")
  } else {
    return nil
  }
}

pub fun main() {
  var user <- getUserResource(exist: false)
  // userはnilのため強制代入演算子が実行される
  user <-! create User(name: "force user")
  destroy user

  var user2 <- getUserResource(exist: true)
  // userはリソースが既に格納されているのでruntime error
  user2 <-! create User(name: "force user")
  destroy user2
}
```

## スワッピング演算子

```<->```を使用することで左辺と右辺の値を交換することができる。２つの変数の値を交換する場合、２つの変数が共にvarで宣言されている必要がある。

```ts
  var x = 1
  var y = 2
  var z = 3

  x <-> y

  log("x = ".concat(x.toString())) // x = 2
  log("y = ".concat(y.toString())) // y = 1
  log("z = ".concat(z.toString())) // z = 3

  // これはNG
  // x <-> y <-> z

  // 代わりにこのように書ける
  x <-> y
  y <-> z

  log("x = ".concat(x.toString())) // x = 1
  log("y = ".concat(y.toString())) // y = 3
  log("z = ".concat(z.toString())) // z = 2
```

## 論理演算子

Cadenceでは比較演算子として```&&```, ```||```, ```==```, ```!=```などが使用することができる。また真偽値の頭に```!```をつけることで真偽値を反転させることができる。

```ts
// AND かつ
true && false // false

// OR または
true || false // true

// 等しい
1 == 1 // true

// 等しくない
1 != 2 // true

!true // false
```

## 比較演算子

Cadenceでも他の言語同様値の比較に```<```, ```>```, ```<=```, ```>=```が使用できます。

```ts
2 > 1 // true
1 < 2 // true

2 <= 3 // true
3 >= 2 // true
```

## ビット演算子

本記事では説明を省略いたします。詳しく知りたい方は公式ドキュメントに記載がありますので公式ドキュメントを参照してください。

## 三項演算子

Cadenceでは三項演算子を使用することができるため```a ? b : c```のようにしてaに真偽値を返す条件式を記載するとtrueの時にbの値、falseの時にcの値が採用されます。三項演算子はif文のようですが式です。

```ts
let x = 1 > 2 ? true : false // x = false
```

## キャスト演算子

キャスト演算子```as```を使用すると演算子の後に与えられた型のサブタイプであれば与えられた型にキャストすることができます。キャストはプログラムの型チェックがされるときに実行されるため、値の実行時の型は考慮されません。つまり、この演算子を使用した**ダウンキャストは不可能**です。代わりにダウンキャスト演算子である```as?```の使用を検討してください。

```ts
let integer: Int = 1

// OK Number型はInt型のスーパークラスなのでキャストは成功する
let number = integer as Number

let something: AnyStruct = 1

// NG somethingの型チェック時の型はAnyStructです。Int型のサブクラスではないのでこのキャストは失敗する。
let result = something as Int
```
## ダウンキャスト演算子

前述したようにダウンキャストをしたいときには```as?```を使用します。この演算子によるキャストは型チェック時では無く実行時に行われます。型変更することができない場合、この演算子はnilを返します。

```ts
let something: AnyStruct = 1

// OK 変数の値はInt型なのでキャストは成功する
let number = something as? Int

// 変数の値はBoolではないためキャストは失敗しnilが返る
let boolean = something as? Bool
```

また、強制ダウンキャスト演算子として```as!```が存在し、```as?```と同じように振る舞う。違いとしてはキャストに成功した場合はオプショナルではなく与えられた型の値を返し、キャストに失敗した場合はruntime errorとなる。

```ts
let something: AnyStruct = 1

// 型はIntなのでキャストに成功する。numberの型はInt?ではなくInt
let number = something as! Int

// 型はBoolではないのでruntime errorとなる
let boolean = something as! Bool
```

## Nil-Coalescing Operator(??)

```??```はオプショナルの値がnilだった時に代替え値を返す演算子です。

```ts
let a: Int? = nil

// b = 42
let b: Int = a ?? 42

let c = 1

// NG 変数cがオプショナルの型ではないのでこれはコンパイルエラー
let d = c ?? 2
```

## Force Unwrap Operator (!)

Optionalな変数に```!```演算子を使用することで値があればその値を返すことができます。ただし、値がなければruntime errorとなります。

```ts
let a: Int? = nil

// 変数aの値がnilのためerrorとなる
let b: Int = a!

let c: Int? = 3

// 値が存在するので取り出せる
let d: Int = c!
```

