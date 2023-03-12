import Hello from "../contracts/Hello.cdc"

pub fun main(addr: Address) {
  // PublicAccount
  let acc = getAccount(addr)
  let ref = acc.getCapability(Hello.helloPublicPath)
    .borrow<&Hello.Token>()
    ?? panic("Tokenの参照が取得できませんでした")
  log("token id")
  log(ref.id)

  // AuthAccount
  let authAcc = getAuthAccount(addr)
  let authRef = authAcc.borrow<&Hello.Token>(from: Hello.helloStoragePath)
    ?? panic("Tokenの参照が取得できませんでした")
  log("token id")
  log(authRef.id)
}