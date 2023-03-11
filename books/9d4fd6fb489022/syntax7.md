---
title: "Cadenceの基礎[インターフェイス]"
---

インターフェイスは実装する型が必要とする関数やフィールド、それに対するアクセス制御、前提条件や事後条件を宣言します。インターフェイスには3種類ありstruct、resource、contractに対して作成することができます。インターフェイスを実装するには```:```に続けて指定することで実装することができます。

```ts
// インターフェイス定義
pub struct interface Add {
  // フィールド
  access(contract) let x: Int
  access(contract) let y: Int

  // 関数
  pub fun add(): Int {
    // 事前条件
    pre {
      self.x > 0; self.y > 0:
        "xとyは正数で指定してください"
    }
    // 事後条件
    post {
      result <= 100:
        "計算結果は100を超えないようにしてください"
    }
  }

  // デフォルト実装
  pub fun hello() {
    log("Hello World")
  }
}

// インターフェイスの実装
pub struct Calculator: Add {
    // Addインターフェイスを実装しているので宣言必須
    access(contract) let x: Int
    access(contract) let y: Int

    // Addインターフェイスを実装しているので実装必須
    pub fun add(): Int {
      return self.x + self.y
    }

    init(x: Int, y: Int) {
      self.x = x
      self.y = y
    }
}

pub fun main() {
  let calc = Calculator(x: 1, y: 2)
  let result = calc.add()

  log(result)
  calc.hello()
}
```

## 制限型

インターフェイスは型注釈に使用することができます。```{T}```のようにインターフェイスTの型注釈を付けることができ、インターフェイスの機能のみを使用するよう機能を制限させることができます。

```diff ts
// インターフェイス定義
pub struct interface Add {
  // フィールド
  access(contract) let x: Int
  access(contract) let y: Int

  // 関数
  pub fun add(): Int {
    // 事前条件
    pre {
      self.x > 0; self.y > 0:
        "xとyは正数で指定してください"
    }
    // 事後条件
    post {
      result <= 100:
        "計算結果は100を超えないようにしてください"
    }
  }

  // デフォルト実装
  pub fun hello() {
    log("Hello World")
  }
}

// インターフェイスの実装
pub struct Calculator: Add {
    // Addインターフェイスを実装しているので宣言必須
    access(contract) let x: Int
    access(contract) let y: Int

    // Addインターフェイスを実装しているので実装必須
    pub fun add(): Int {
      return self.x + self.y
    }

+    pub fun substruct(): Int {
+      return self.x - self.y
+    }

    init(x: Int, y: Int) {
      self.x = x
      self.y = y
    }
}

pub fun main() {
  // 機能をAddインターフェイスの範囲で制限する
-  let calc = Calculator(x: 1, y: 2)
+  let calc: {Add} = Calculator(x: 1, y: 2)
  let result = calc.add()

  log(result)
  calc.hello()

+  // NG Addインターフェイスの機能ではないので使用できない
+  calc.substruct()
}
```

:::message
リソースを安全に扱うためにはインターフェイスを活用し、公開する機能を制限してうまく利用することがポイントとなりますので詳しく知りたい方は公式ドキュメントもご覧ください。
:::