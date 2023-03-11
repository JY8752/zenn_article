---
title: "Cadenceの基礎[Contract]"
---

Folwにおけるスマートコントラクトの実態はCadenceにおけるContractで定義します。コントラクトを定義するには```contract```キーワードを使用して定義します。Cadenceにおけるcontractはインターフェイス、構造体、リソース、ストレージデータなどの型定義の集合です。Flowにおけるスマートコントラクトの実装はFlowのアカウントのコントラクト領域に格納されます。これはFlowの特徴であり、チェーン状にデプロイするEthereumなどと異なる特徴です。慣習的にコントラクトは大文字始まりのキャメルケースで宣言します。

```ts
pub contract HelloWorld {

    // フィールド
    pub let greeting: String

    // 関数
    pub fun hello(): String {
        return self.greeting
    }

    // 初期化
    init() {
        self.greeting = "Hello World!"
    }
}
```

コントラクトをデプロイしたアカウントのアドレスを使用することでコントラクトを```import```することができ、公開されているフィールドや関数があれば使用してコントラクトと対話することができます。

```ts
import HelloWorld from 0x42

log(HelloWorld.hello())    // prints "Hello World!"

log(HelloWorld.greeting)   // prints "Hello World!"

```

importは以下のようにファイルパスを指定してimportすることもできます。その場合、ファイルのパスは文字列で指定します。

```ts
import HelloWorld from "./contracts/hellowordl.cdc"

log(HelloWorld.hello())    // prints "Hello World!"

log(HelloWorld.greeting)   // prints "Hello World!"
```

また、コントラクトで宣言されたリソースやイベントのインスタンスは、同じコントラクトで宣言された関数や型の中でしか生成することができないこともコントラクトの重要な特徴の一つです。以下のコントラクトの例ではリソースを作成する方法がないため使用することはできません。

```ts
pub contract FungibleToken {

    pub resource interface Receiver {

        pub balance: Int

        pub fun deposit(from: @{Receiver}) {
            pre {
                from.balance > 0:
                    "Deposit balance needs to be positive!"
            }
            post {
                self.balance == before(self.balance) + before(from.balance):
                    "Incorrect amount removed"
            }
        }
    }

    pub resource Vault: Receiver {

        // keeps track of the total balance of the accounts tokens
        pub var balance: Int

        init(balance: Int) {
            self.balance = balance
        }

        // withdraw subtracts amount from the vaults balance and
        // returns a vault object with the subtracted balance
        pub fun withdraw(amount: Int): @Vault {
            self.balance = self.balance - amount
            return <-create Vault(balance: amount)
        }

        // deposit takes a vault object as a parameter and adds
        // its balance to the balance of the Account's vault, then
        // destroys the sent vault because its balance has been consumed
        pub fun deposit(from: @{Receiver}) {
            self.balance = self.balance + from.balance
            destroy from
        }
    }
}
```

以下のようなリソースの作成はコンパイルエラーとなり実行することはできない。

```ts
import FungibleToken from 0x42

// Invalid: Cannot create an instance of the `Vault` type outside
// of the contract that defines `Vault`
//
let newVault <- create FungibleToken.Vault(balance: 10)
```

リソースを作成できるようにするためには以下のようにコントラクト内にリソースを作成する関数を定義する必要があります。

```ts
pub fun createVault(initialBalance: Int): @Vault {
    return <-create Vault(balance: initialBalance)
}
```

また、より現実的に考えた時FungibleTokenコントラクトは初期化時に自身のアカウントのストレージにReciverを持っている必要があるでしょう。そのため、実際にはコントラクトの初期化処理には以下のようにリソースをストレージに格納する処理が必要です。

```ts
init(balance: Int) {
    let vault <- create Vault(balance: balance)
    self.account.save(<-vault, to: /storage/initialVault)
}
```

## イベント

コントラクト内の処理で発生した値などを外部で取得することができるようにコントラクト内ではイベントを定義することができます。イベントは```event```キーワードで宣言し、関数と同じように引数を引数ラベルとともに指定します。慣習的にEventも大文字始まりのキャメルケースで宣言します。

```ts
pub contract Events {
    event BarEvent(labelA fieldA: Int, labelB fieldB: Int)
}
```

コントラクト内からイベントを発火するには```emit```キーワードを使用します。

```ts
pub contract Events {
    event FooEvent(x: Int, y: Int)

    // ラベル名を指定することもできる
    event BarEvent(labelA fieldA: Int, labelB fieldB: Int)

    fun events() {
        emit FooEvent(x: 1, y: 2)
        // 指定したラベル名で引数を渡す
        emit BarEvent(labelA: 1, labelB: 2)
    }
}
```