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
