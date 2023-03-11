pub resource Token {}

pub fun main(addr: Address) {
  let t = Type<Int>()
  t.identifier // Int

  let result = 1 + 1
  assert(result == 2, message: "計算結果は2である必要があります。")

  let acc = getAuthAccount(addr)
  acc.save(<- create Token(), to: /storage/token)

  let token <- acc.load<@Token>(from: /storage/token) ?? panic("トークンが格納されていません")
  destroy token

  let tokenRef = acc.borrow<&Token>(from: /storage/token) ?? panic("トークンが格納されていません")

}

pub contract TestContract {
  pub let storagePath: StoragePath
  pub let privatePath: PrivatePath
  pub let publicPath: PublicPath

  init() {
    // self.storagePath = /storage/test
    // self.privatePath = /private/test
    // self.publicPath = /public/test

    // NG StoragePath型の変数のためpublicパスは格納できない
    // self.storagePath = /public/test

    // NG ドメイン/識別子の後にさらにパスセパレーターでパスを続けることはできない
    // self.storagePath = /storage/test/1

    // 文字列からパスを生成することもできる
    self.storagePath = StoragePath(identifier: "test") ?? panic("パスが不正")
    self.privatePath = PrivatePath(identifier: "test") ?? panic("パスが不正")
    self.publicPath = PublicPath(identifier: "test") ?? panic("パスが不正")
  }
}