---
title: "Cadenceの基礎[変数と型について]"
---

このチャプターではスマートコントラクトを開発するにあたり必要なCadence言語の最低限の基礎文法を学びます。Cadenceには以下のような特徴があります。

- 型システム、リソース指向の言語設計により安全性と強固なセキュリティを提供します
- 可読性を重視して設計されており、コードが何をしているかが直感的にわかるようになっています
- Cadenceは主要な様々な言語を参考に作成されています。(整数や文字列の安全な取り扱いはSwift, リソースはRust、イベントはSolidityから触発されている。)

基本的に公式ドキュメントに詳しく記載されいていますので本チャプターを読んだら一度[公式ドキュメント](https://developers.flow.com/cadence/language)に目を通すことをおすすめします。

## コメント

Cadenceでのコメントは```//```で表現します。複数行のコメントに```/* */```を使用することもできます。

```ts
// ここにコメントを書く
let x = 1 // この位置にもコメントは書ける

/*
コメント1
コメント2
コメント3
*/
let x = 1
```

ドキュメントコメントとして```///```や```/** **/```も使用することができる。

```ts
/// ドキュメント
let x = 1

/**
ドキュメント１
ドキュメント２
**/
let y =2
```

## 変数名

変数名は慣習的に小文字はじまりのキャメルケースで型名は大文字はじまりのキャメルケースで記述することが多いようです。

## セミコロン

セミコロンは宣言と文の間の区切り文字として使用されます。セミコロンは宣言と文の後に置くことができますが、宣言と宣言の間や1行に1つしか文がない場合は省略することができます。複数の文が同じ行にある場合はセミコロンを使って区切る必要があります。

```ts
// セミコロンなしに定数宣言
let a = 1

// セミコロン付きで変数宣言
var b = 2;

// 1行に定数と変数の宣言をセミコロンで区切ってする
let a = 1; var b = 2
```

## 定数と変数

Cadenceでは定数の宣言に```let```、変数の宣言に```var```を使用します。letで宣言した変数には再代入することはできませんが、varで宣言した変数には値を再代入することが可能です。

```ts
let a = 1
// コンパイルエラー
a = 2

// ok
var b = 1
b = 2
```

## 型注釈

Cadenceでは型推論が効くので型注釈を付けなくても変数の宣言ができますが、明示的に型注釈を付けることができます。また、関数の戻り値の型は明示的に型注釈を付ける必要があります。

```ts
// 型推論が効くのでBool型の変数として解釈される
let boolValue = true

// 明示的にUInt64の型注釈を付けることでUInt64型の変数として解釈される
let num: UInt64 = 1

// 関数の戻り値がある場合は型を明示する必要がある
pub fun add(_ num1: Int, _ num2: Int): Int {
  return num1 + num2
}
```

## Cadenceの様々な型について
Cadenceは静的型付けの言語のため様々な型が存在します。

### 真偽値
- ```Bool``` 真偽値(true, false)を表す。

### 数値

```ts
// 数値を表現
123456789

// 2進数表記
0b101010  // is `42`

// 8進数表記
0o12345670  // is `2739128`

// 16進数表記
0x1234567890ABCabc  // is `1311768467294898876`

// prefixのない先頭の0は無視される
00123 // is `123`

// 桁区切りに_が使える
let largeNumber = 1_000_000
```

#### 整数
Cadenceの数値型は標準でオーバーフローなどが発生しないようになっているため安全に数値を扱うことができる。以下に記載する```Int```型や```UInt```型はオーバーフローが発生するとエラーとなる。

符号付きの整数には以下のような型が存在する。

- ```Int8``` -2^7 through 2^7 − 1 (-128 through 127)
- ```Int16``` -2^15 through 2^15 − 1 (-32768 through 32767)
- ```Int32``` -2^31 through 2^31 − 1 (-2147483648 through 2147483647)
- ```Int64``` -2^63 through 2^63 − 1 (-9223372036854775808 through 9223372036854775807)
- ```Int128``` -2^127 through 2^127 − 1
- ```Int256``` -2^255 through 2^255 − 1

符号なし整数には以下のような型が存在する。

- ```UInt8``` 0 through 2^8 − 1 (255)
- ```UInt16``` 0 through 2^16 − 1 (65535)
- ```UInt32``` 0 through 2^32 − 1 (4294967295)
- ```UInt64``` 0 through 2^64 − 1 (18446744073709551615)
- ```UInt128``` 0 through 2^128 − 1
- ```UInt256``` 0 through 2^256 − 1

オーバーフロー、アンダーフローをチェックしない符号なし整数型は接頭辞にWordを持ち、以下の範囲で値を表現できる。

- ```Word8``` 0 through 2^8 − 1 (255)
- ```Word16``` 0 through 2^16 − 1 (65535)
- ```Word32``` 0 through 2^32 − 1 (4294967295)
- ```Word64``` 0 through 2^64 − 1 (18446744073709551615)

また、型注釈をつけないで宣言した数値(数値リテラル)は任意精度の```Int```型として扱われる。

#### 固定少数点数

執筆時点では以下の64ビット幅の型が用意されているが将来的なリリースでより多くの固定小数点型が追加される予定のようです。

- ```Fix64```  Factor 1/100,000,000; -92233720368.54775808 through 92233720368.54775807
- ```UFix64``` Factor 1/100,000,000; 0.0 through 184467440737.09551615

### Address(アドレス)

Address型はアドレスを表します。

- ```Address``` 64bitの符号なし整数。16進数の整数リテラルで表現できる。

```ts
let someAddress: Address = 0x436164656E636521

// 数値でないのでコンパイルエラー
let notAnAddress: Address = ""

// 64bit整数の範囲を超えているのでコンパイルエラー
let alsoNotAnAddress: Address = 0x436164656E63652146757265766572
```

### AnyStruct / AnyResource

Cadenceでは全ての型はAnyStruct型のサブクラスもしくはAnyResource型のサブクラスに分類される。

- ```AnyStruct``` 全ての非リソース型の上位型。
- ```AnyResource``` 全てのリソース型の上位型。

### Optionalとnil

Cadenceではnullではなくnilで値がないことを表します。そして、nilである可能性のある変数には```?```を使用してOptional型として扱うことができます。

```ts
let a: Int? = nil
```

また、```??```のように```?```を二重にしてDouble Optional型として扱うこともある。

```ts
  let anyValue: AnyStruct? = 1
  // doubleIotional: Int??
  let doubleOptional = anyValue as? Int?

  // intValue: Int
  let intValue = (doubleOptional ?? panic("castに失敗している")) ?? panic("値がない")
```

### Never

```Never```型は一番最下層の型であり全ての型のサブタイプです。Never型を持つ値は存在しません。Never型は通常では決して戻らない関数の戻り値型として使用することができます。

```ts
// panicする関数の戻り値の型に使える
fun crashAndBurn(): Never {
    panic("An unrecoverable error occurred")
}

// Never型は値をとれない
let x: Never = 1

// Never型は値をとれない
fun returnNever(): Never {
    return nil
}
```

### 文字列

文字列は文字の集合であり、Cadenceでは文字列を```String```型、文字を```Character```型で表現します。```"```で囲った文字は文字列リテラルとしてString型で扱うことができる。```\u{x}```の形式でxにUnicodeのコードポイントを当てはめることでUnicode文字を表現することができ、これはCharacter型で表現することもできます。

```ts
// str: String
let str = "test"

// `singleScalar` is `ü`
let singleScalar: Character = "\u{FC}"
```

String型には関数がいくつか用意されていますがよく使用するものとしてconcat関数がありこれは文字列の結合に使用します。

```ts
let hello = "Hello"
let world = "World"

// HelloWorld
let helloWorld = hello.concat(world)
```

### 配列

Cadenceの配列は固定長配列と可変長配列が存在します。固定長配列は[T;N]の形式で表し、Tは要素の型であり、Nはその固定長配列の要素数です。可変長配列は[T]で表現します。

```ts
let size = 2
// サイズ２の固定長配列なのでNG
let numbers: [Int; size] = []

// これはok
let array: [Int8; 2] = [1, 2]

// 配列はネストすることも可能
let arrays: [[Int16; 3]; 2] = [
    [1, 2, 3],
    [4, 5, 6]
]

// 空の配列で初期化して変数宣言
var variableLengthArray: [Int] = []

// AnyStructを使用することで異なる型を混ぜることも可能
let mixedValues: [AnyStruct] = ["some string", 42]
```

配列から値を取得するときは```[]```にインデックスを指定することで取得することができる。

```ts
let numbers = [1, 2]

numbers[0] // 1

numbers[1] // 2

numbers[2] // Run-time error

// ネストしている場合も以下のように取得できる
let numbers2 = [[1, 2], [3, 4]]

numbers2[0][1] // 2
```

配列型には要素の追加や削除などいくつか関数が用意されているので詳しく知りたい方は公式のドキュメントを参照してみてください。

### 辞書型

辞書型は他言語にある連想配列やMapと同じようなものでkey-valueで表現する順序のないコレクションです。辞書型の変数から値を取り出す場合、```[]```にkeyの値を指定して取り出すことができる。存在しないkeyを指定した場合はnilが返ります。

```ts
  // empty
  let empty: {String:String} = {}
  // {String:Int}
  let stringToInt = {
    "key1": 1,
    "key2": 2
  }

  stringToInt["key1"] // 1
  stringToInt["key2"] // 2
  stringToInt["key3"] // nil
```

辞書型にも値の追加や削除など組み込みの関数がいくつか用意されていますので詳しく知りたい方は公式のドキュメントを参照ください。

### Type型

型はランタイムに表現することができます。型値を作成するにはコンストラクタ関数```Type<T>()```を使用し、型引数に静的型を受け取ります。これはSwiftの```T.self```、Kotlinの```T::class/KClass<T>```、Javaの```T.class/Class<T>```と同様です。

```ts
  let t = Type<Int>()
  t.identifier // Int
```

### Path(パス)

アカウントにはリソースや構造体をストレージに格納するためのPathとその参照を保存するCapabilityのPathがあり、CapabilityのPathはpublicとprivateの2つが存在するため計3つのPathが存在します。Pathは```/```で始まりドメイン、パスセパレーター(/)、識別子が続きます。ドメインは```storage```、```public```、```private```の3つが存在し、識別子は任意のものになります。ストレージパスを表す型が```StoragePath```であり、パブリックパスとプライベートパスがそれぞれ```PublicPath```、```PrivatePath```の型になります。

```ts
pub contract TestContract {
  pub var storagePath: StoragePath
  pub var privatePath: PrivatePath
  pub var publicPath: PublicPath

  init() {
    // パスの初期化
    self.storagePath = /storage/test
    self.privatePath = /private/test
    self.publicPath = /public/test

    // NG StoragePath型の変数のためpublicパスは格納できない
    self.storagePath = /public/test

    // NG ドメイン/識別子の後にさらにパスセパレーターでパスを続けることはできない
    self.storagePath = /storage/test/1

    // 文字列からパスを生成することもできる
    self.storagePath = StoragePath(identifier: "test") ?? panic("パスが不正")
    self.privatePath = PrivatePath(identifier: "test") ?? panic("パスが不正")
    self.publicPath = PublicPath(identifier: "test") ?? panic("パスが不正")
  }
```

### Account(アカウント)

全てのアカウント型は```PublicAccount```と```AuthAccount```の２種類に分類されます。アカウント型は主にscriptやtransactionの処理を書く時に使用することが多く、組み込み関数である```getAccount()```、```getAuthAccount()```を使用することでアカウントアドレスから取得することができます。アカウント型で定義されている詳細な関数などが知りたい方は[公式ドキュメント](https://developers.flow.com/cadence/language/accounts)を参照してください。

#### save()

AuthAccountの```save()```を使用することでアカウントのストレージにオブジェクトを保存することができる。

```ts
fun save<T>(_ value: T, to: StoragePath)
```

```ts
pub resource Token {}

pub fun main(addr: Address) {
  let acc = getAuthAccount(addr)
  acc.save(<- create Token(), to: /storage/token)
}
```

#### load()

ストレージパスから格納されているオブジェクトをとり出すのに```load()```が使用することができます。関数を実行しオブジェクトの取得に成功した場合、そのストレージパスにはもうオブジェクトは存在しない点に注意してください。ストレージパス内のオブジェクトを移動させることなく操作するには後述する```borrow()```を使用するなどして参照を利用することができます。

```ts
fun load<T>(from: StoragePath): T?
```

取り出す値の型の指定は必須で指定されたパスにオブジェクトが格納されていない場合はnilが返ります。

```ts
pub resource Token {}

pub fun main(addr: Address) {
  let acc = getAuthAccount(addr)
  acc.save(<- create Token(), to: /storage/token)

  let token <- acc.load<@Token>(from: /storage/token) ?? panic("トークンが格納されていません")
}

```

#### bnorrow()

ストレージパス内のオブジェクトの参照を```borrow()```を使用することで作成することができます。

```ts
fun borrow<T: &Any>(from: StoragePath): T?
```

```ts
pub resource Token {}

pub fun main(addr: Address) {
  let acc = getAuthAccount(addr)
  acc.save(<- create Token(), to: /storage/token)

  let tokenRef = acc.borrow<&Token>(from: /storage/token) ?? panic("トークンが格納されていません")
}
```

### 型の全体図

Cadenceにおける型の階層構造の全体図は以下の通りです。以下の図を見ると```Int```、```UInt```などのスーパータイプは```Integer```であり、全ての数値型のスーパータイプは```Number```であることがわかります。

![](https://storage.googleapis.com/zenn-user-upload/d3414ac52ba9-20230310.png)