pub fun main() {
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

  // リソースのインスタンス化
  let user <- create User(name: "user")
  // 別の変数にリソースを移動
  let user2 <- user
  // NG 既に移動しているのでアクセス不可
  // user.name
  
  // アクセス可能
  user2.name

  // 関数の引数にリソースを指定
  let user3 <- returnResource(user: <- user2)

  // リソースを破棄する
  destroy user3
}

pub resource User {
  pub let name: String
  init(name: String) {
    self.name = name
  }
  destroy() {
    log(self.uuid.toString().concat("が破棄されました。"))
  }
}

pub fun returnResource(user: @User): @User {
  return <- user
}