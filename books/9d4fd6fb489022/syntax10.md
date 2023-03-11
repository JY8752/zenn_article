---
title: "Cadenceの基礎[Capabilityと参照]"
---

アカウントのストレージ領域に保存されているオブジェクトの特定のフィールドや関数にアクセスしたい場合、Cadenceでは```Capability```を作成することで実現することができます。CapabilityはAuthAccountの```link()```を使用することで作成することができます。

```ts
fun link<T: &Any>(_ newCapabilityPath: CapabilityPath, target: Path): Capability<T>?
```

