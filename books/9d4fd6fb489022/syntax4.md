---
title: "Cadenceの基礎[制御フロー]"
---

このチャプターではCadenceにおけるif文やfor文などの制御構文について紹介いたします。

## if文

Cadenceでも他の言語同様if文を使用することで条件分岐による処理を書くことができます。

```ts
pub fun test(bool: Bool, bool2: Bool) {
  // ()はなくても大丈夫
  if bool {
    log("hello")
  } else {
    log("world")
  }

  // ()をつけてもコンパイルエラーにはならない。
  if(bool) {
    log("hello")
  } else if(bool2) {
    log("world")
  } else {
    log("!!")
  }
}
```

## Optional Binding

Optional BindingはOptionalの中の値を取得することができる、if文の変形です。Optionalの中身が存在する場合は最初の分岐に入り、格納した変数を使用することができます。値がない場合は二つ目の分岐に入ります。

```ts
  let maybeNumber: Int? = 1

  if let number = maybeNumber {
    log(number)
  } else {
    log("値がありません")
  }
```

## Switch文

CadenceではSwitch文が使用できるため複数の条件によって処理を分けたい場合は以下のように書くことができます。また、Cadenceでは明示的なbreakの宣言はする必要はなく、最初にマッチした処理が終了した時にswitch文の実行は終了します。

```ts
pub fun word(_ n: Int): String {
    switch n {
    case 1:
        return "one"
    case 2:
        return "two"
    default:
        return "other"
    }
}

word(1)  // returns "one"
word(2)  // returns "two"
word(3)  // returns "other"
word(4)  // returns "other"
```

## ループ処理

### while文

CadenceではWhile文がサポートされているため、以下のように条件式で与えた式がtrueである限りループ処理を実行し続けます。

```ts
  var counter = 0
  while counter < 5 {
    counter = counter + 1
  }
```

### for文

Cadenceでは配列を```for-in```キーワードを使用することで繰り返し処理をすることが可能です。

```ts
  let array = ["Hello", "World", "Foo", "Bar"]

  for element in array {
      log(element)
  }

  // The loop would log:
  // "Hello"
  // "World"
  // "Foo"
  // "Bar"

  // indexと共に繰り返しも可能
  for index, element in array {
      log(index)
  }

  // The loop would log:
  // 0
  // 1
  // 2
  // 3

  // 辞書型はkeys()を使用して繰り返し可能
  let dictionary = {"one": 1, "two": 2}
  for key in dictionary.keys {
      let value = dictionary[key]!
      log(key)
      log(value)
  }

  // The loop would log:
  // "one"
  // 1
  // "two"
  // 2
```

### continueとbreak

ループの途中で```continue```、```break```などを使用して処理のスキップや中断が可能です。

```ts
 var i = 0
  var x = 0
  while i < 10 {
      i = i + 1
      if i < 3 {
          continue
      }
      x = x + 1
  }
  // `x` is `8`


  let arr = [2, 2, 3]
  var sum = 0
  for element in arr {
      if element == 2 {
          continue
      }
      sum = sum + element
  }
  // `sum` is `3`

  var x = 0
  while x < 10 {
      x = x + 1
      if x == 5 {
          break
      }
  }
  // `x` is `5`


  let array = [1, 2, 3]
  var sum = 0
  for element in array {
      if element == 2 {
          break
      }
      sum = sum + element
  }

  // `sum` is `1`
```