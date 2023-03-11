---
title: "Cadenceの基礎[列挙型]"
---

Cadenceでは列挙型としてenumを使用することができます。enumの定義は```enum```キーワードを使用し、rawの型として```UInt8```もしくは```UInt128```を指定し、```case```キーワードでフィールドを宣言します。enumで宣言したフィールドには0から順番に```rawValue```が割り振られています。rawValueを指定することでenumを取得することもできます。

```ts
pub fun main() {
  let red = Color.red
  red.rawValue // 0

  let green = Color(rawValue: 1) // Color.Green
  
  let nothing = Color(rawValue: 5) // nil
}

pub enum Color: UInt8 {
    pub case red
    pub case green
    pub case blue
}
```