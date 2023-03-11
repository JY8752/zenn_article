---
title: "開発環境の構築"
---

## Playground

Cadenceを一番手軽に実行できるのはWebブラウザ上で実行できる[Flow Playground](https://play.flow.com/local-project)です。試しにリンクからPlaygroundを開きスクリプトを実行してみましょう。

## VSCode

執筆時点でFlowの開発をするならばVSCodeでの開発が最適です。以下の拡張機能をインストールすることで簡単にFlowでのスマートコントラクトを開発するための環境が整います。

- [Cadence](https://marketplace.visualstudio.com/items?itemName=onflow.cadence)
シンタックスハイライトや型チェック、コード補完などの機能を提供してくれるのでほぼ必須で必要です。

- [Material Icon Theme](https://marketplace.visualstudio.com/items?itemName=PKief.material-icon-theme)
Cadenceファイルのアイコンが用意されているので開発時のテンションがあがります。なくてもよいです。

## Flow CLI

Flowアプリケーションを開発するためには必須のツールです。Flow CLIはFlowネットワークとやり取りするためのコマンドやアカウントの作成や照会、トランザクションの送信などの機能を提供するコマンドラインです。インストールにはbrewを使用するか直接バイナリをダウンロードする方法があります。

```
// brewを使用
brew install flow-cli

or 

// バイナリを直接ダウンロード
sh -ci "$(curl -fsSL https://raw.githubusercontent.com/onflow/flow-cli/master/install.sh)"
```
