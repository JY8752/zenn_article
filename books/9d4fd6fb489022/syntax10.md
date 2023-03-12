---
title: "Cadenceの基礎[Capabilityと参照]"
---

アカウントのストレージ領域に保存されているオブジェクトの特定のフィールドや関数を公開したり、逆にアクセスするときCadenceでは参照とCapabilityを利用することで実現することができます。

## 参照

Cadenceでは型```T```の参照を```&T```で表現することができます。参照型を作るときは```as```演算子を使用し明示的にキャストする必要があります。

```ts
    let hello = "hello"

    // OK
    let strRef = &hello as &String

    // NG
    let strRef2 = &hello
```

参照を作成する際に```auth```キーワードを使用することで認証された参照を作成することができます。通常、認証されていない参照はアップキャストのみ可能ですが認証されている参照はアップキャストに加えダウンキャストまで自由に行うことができる。

```ts
pub contract CounterContract {
  pub resource interface HasCount {
    pub var count: Int
  }

  pub resource Counter: HasCount {
    pub var count: Int
    init(count: Int) {
      self.count = count
    }
    pub fun increment() {
      self.count = self.count + 1
    }
  }

  pub fun createCounter(): @Counter {
    return <- create Counter(count: 1)
  }
}
```

```ts
import CounterContract from "./contracts/counter.cdc"
pub fun main() {
    let counter <- CounterContract.createCounter()
    
    // HasCountの参照
    let countRef = &counter as &{CounterContract.HasCount}

    log(countRef.count)

    // NG increment()の参照は含まれていないためこれは実行できない
    // countRef.increment()

    // NG この参照の作成は無効です。認証されていないダウンキャストはできません。
    // let countRef2 = countRef as? &CounterContract.Counter

    // 認証された参照を作成
    let authCountRef = &counter as auth &{CounterContract.HasCount}

    // 認証されているのでダウンキャストできる
    let countRef3 = authCountRef as? &CounterContract.Counter

    countRef3?.increment()
    log(countRef3?.count)

    destroy counter
}
```

## Capability

CapabilityはAuthAccountの```link()```を使用することで作成することができます。```link()```で作成したCapablityはAccount型の```getCapability()```を使用することで取得することができ、```borrow()```を使用することでCapabilityからそのオブジェクトにアクセスするための参照を取得することができます。

CadenceではResourceなどのオブジェクトの機能を公開する際にアカウントのストレージに保存されているそのオブジェクトのCapabilityをパブリックパスなどに格納しておくことで外部のユーザーはそのCapabilityからオブジェクトの機能の参照を取得しアクセスなどを行います。

```ts
fun link<T: &Any>(_ newCapabilityPath: CapabilityPath, target: Path): Capability<T>?
```

```link()```には型パラメーターTを指定する必要があり、Tは参照型である必要があります。第一引数の```newCapabilityPath```にはCapabilityを作成するパスをパブリックもしくはプライベートで指定し、第二引数にはlinkを作成するオブジェクトが格納されているパスを指定します。Capabilityを作成するオブジェクトがインターフェイスを実装している場合、型パラメーターTには制限型でインターフェイスを指定することで公開する機能を制限することができます。

```ts
pub contract TestContract {
    pub let storagePath: StoragePath
    pub let publicPath: PublicPath
    pub let publicHelloPath: PublicPath
    pub let privatePath: PrivatePath

    pub var id: UInt64

    pub resource interface Hello {
        pub fun hello() {
            log("hello")
        }
    }

    pub resource Token: Hello {
        pub let id: UInt64

        init(id: UInt64) {
            self.id = id
        }

        pub fun getTokenId(): UInt64 {
            return self.id
        } 
    }

    pub fun mint(): @Token {
        let token <- create Token(id: self.id)
        self.id = self.id + 1
        return <-token
    }

    init() {
        self.storagePath = /storage/test
        self.publicPath = /public/test
        self.publicHelloPath = /public/hello
        self.privatePath = /private/test

        self.id = 1

        // リソースをストレージに保存
        self.account.save(<- self.mint(), to: self.storagePath)
        
        // Capabilityを作成する
        
        // リソースのCapabilityを作成する
        self.account.link<&Token>(self.publicPath, target: self.storagePath)
        
        // リソースの機能をHelloに限定してCapabilityを作成
        self.account.link<&{Hello}>(self.publicHelloPath, target: self.storagePath)

        // privateパスにCapabilityを作成する
        self.account.link<&Token>(self.privatePath, target: self.storagePath)
    }
}
```

```ts
import TestContract from "./contracts/demo12.cdc"

pub fun main(addr: Address) {
    let acc = getAccount(addr)

    // パブリックパスにあるCapabilityからTokenの参照を取得する
    let ref = acc.getCapability(TestContract.publicPath)
        .borrow<&TestContract.Token>()
        ?? panic("Capabilityが作成されていません")

    // パブリックパスにあるCapabilityからHello型のTokenの参照を取得する
    let helloRef = acc.getCapability(TestContract.publicHelloPath)
        .borrow<&{TestContract.Hello}>()
        ?? panic("Capabilityが作成されていません")

    // プライベートパスのCapabilityの取得はAuthAccountしかできないので作成
    let authAcc = getAuthAccount(addr)
    // プライベートパスにあるCapabilityからTokenの参照を取得する
    let privateRef = authAcc.getCapability(TestContract.privatePath)
        .borrow<&TestContract.Token>()
        ?? panic("Capabilityが作成されていません")
    
    // パブリックパスのCapabilityを使用
    log(ref.getTokenId())
    ref.hello()

    // helloRefはHello型しか機能公開していないのでgetTokenIdは使用することができない
    helloRef.hello()

    // プライベートパスのCapabiltyを使用
    log(privateRef.getTokenId())
    privateRef.hello()
}
```