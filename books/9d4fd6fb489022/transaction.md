---
title: "transactionファイルの作成と実行"
---

mintやトークンのTransferなど、スマートコントラクトの状態を変化させるようなトランザクションを実行する場合、cadenceでtransactionファイルを作成して実行します。

## transactionの実行

scriptのチャプターで使用したHelloコントラクトをそのまま使用して、hello関数を実行するようなtransactionを送信するには以下のようなファイルを作成しFlow CLIのコマンドを実行します。

```ts:hello.cdc
import Hello from "../contracts/Hello.cdc"

transaction {
  execute {
    Hello.hello()
  }
}
```

transactionの実行は```flow transactions send <transactionファイルのパス> (引数)...```のようにして実行します。

```
flow transactions send cadence/transactions/hello.cdc                

> Transaction ID: fcacbb2b30c3e1046f248f1988832f9b0b26e5608f053baa6a2ceecb68933868

Status		✅ SEALED
ID		fcacbb2b30c3e1046f248f1988832f9b0b26e5608f053baa6a2ceecb68933868
Payer		f8d6e0586b0a20c7
Authorizers	[]

Proposal Key:	
    Address	f8d6e0586b0a20c7
    Index	0
    Sequence	5

flow scripts execute cadence/scripts/get_hello_count.cdc

> Result: 2 // countが更新されている
```

## transactionファイルの構造

transactionは```transaction```キーワードで宣言されその内容は処理ブロック内に記述していきます。transactionブロック内にはローカル変数の宣言、```prepare```、```pre```、```execute```、```post```ブロックの宣言が可能となっています。

```ts
transaction {
    pub let localVar: Int

    prepare(signer1: AuthAccount, signer2: AuthAccount) {
        // ...
    }

    pre {
        // ...
    }

    execute {
        // ...
    }

    post {
        // ...
    }
}
```

### transactionブロック

transactionの実行時に引数を渡すことが可能となっており、引数を取る場合はtransactionブロックの引数を指定する必要があります。

```ts
// amountを引数に取る
transaction(amount: UFix64) {

}
```

### prepareブロック

prepareブロックでは署名アカウントのアクセスが必要な場合に使用されます。署名アカウントはprepareブロックのみ引数に取ることができ使用することができます。また、Flowではトランザクションの署名アカウントは複数取ることができるため、引数に取る署名アカウントも複数指定することが可能となっています。

```ts
prepare(signer1: AuthAccount) {
      // ...
 }
```

:::message
ベストプラクティスとして署名アカウントのAuthAccountを利用するロジックのみprepareブロックに記述し、それ以外の処理はexecuteブロックなどに記述することが推奨されています。アカウントにアクセスしての変更は重要な意味を持つため、このフェーズでは無関係なロジックを排除しAuthAccountを利用するロジックを固めておくことでコードの可読性を上げコードの処理がどのようなものかを理解しやすくします。
:::

### preブロック

preブロックはprepareブロックの後に実行され、transactionの残りの部分を実行する前に明示的な条件が成立するかどうかをチェックします。一般的な例として、アカウント間でトークンを転送する前に必要な残高をチェックします。

```ts
pre {
    sendingAccount.balance > 0
}
```

preブロックでエラーを返すか、trueを返さない場合トランザクションの実行は中断され完全に元に戻されます。

### executeブロック

executeブロックはその名の通りメインロジックを記述するブロックです。prepareブロックで準備し、残ったロジックをこのブロック内で処理します。

### postブロック

postブロックはトランザクションロジックが正しく実行されたことを検証するために使用されます。例えば、転送トランザクションの最終残高が特定の値を持つことを保証したい場合などに利用することができます。

```ts
post {
    result.balance == 30: "Balance after transaction is incorrect!"
}
```

条件チェックがfalseとなった場合、トランザクションは失敗し、完全に元に戻されます。