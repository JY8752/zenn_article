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