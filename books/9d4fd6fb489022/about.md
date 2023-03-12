---
title: "FlowとDapper Labs"
---

## Flowについて

[Flow](https://flow.com/)は[Dapper Labs](https://www.dapperlabs.com/)により開発されたL1のパブリックチェーンです。Dapper LabsはもともとEthereumで流行した[Crypto Kitties](https://www.cryptokitties.co/)を開発していたチームです。Crypto KittiesはEthereumにおけるERC721という規格を生み出し、NFTという言葉を世に広め、トークンを用いたブロックチェーンゲームというジャンルを開拓してくれた偉大なプロジェクトだったと思います。しかし、Crypto KitiesはEthereumのトランザクション処理の遅さなどの課題を浮き彫りにしたとも言われています。([The Inside Story of the CryptoKitties Congestion Crisis](https://consensys.net/blog/news/the-inside-story-of-the-cryptokitties-congestion-crisis/))

そこで、Ethereumの課題を解決するために開発されたのがFlowです。

### マルチロールアーキテクチャ

Flowは収集、コンセンサス、実行、検証といった役割別でノードが分割されています。これは水平方向（シャーディング）ではなく垂直方向のスケーリングを実現し高速なトランザクション処理と安価なガス代を可能としています。

### リソース指向プログラミング

Dapper Labsの開発チームはFlowだけでなくスマートコントラクトを実装するのにCadenceというプログラミング言語も開発しました。Cadenceはリソース指向言語として開発されておりSolidityでのスマートコントラクト開発と比較してより現実世界とリンクした直感的な設計と実装を可能としています。

Flowチェーンについてより詳しく知りたい方は公式や[ホワイトペーパー](https://flow.com/technical-paper)などを参照してみてください。
