---
title: "helm-secretsをgpgで使用するとwarningが出るのでageを使用する"
emoji: "🎉"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["helm", "kubernetes", "sops", "gpg", "age"]
published: false
---

最近、kubernetesを学習していてMinikubeを使用してローカル環境でkubernetesクラスタを構築していろいろ試しており、Secretリソースをバージョン管理するのに暗号化したいなということでhelmのプラグインであるhelm-secretsを使用してみました。

helm-secretsは実際sopsというライブラリを使用し暗号化しており、AWSやGCP、GPGなどが使用できるので今回はGPGを使用してみたところ複合化した際に非推奨のwarningが出たためいろいろ調べたことを記事にしてみました。

結論から言うとhelm-secretsでGPGを使用したい場合、```ageの使用が推奨されています```。

## helm-secretsとは

kubernetesのSecretリソースを作成するためのマニフェストファイルには秘匿情報をbase64でエンコードして記載しますが、暗号化しているわけではないのでGitHubなどでバージョン管理することができないです。Helmを使用していてSecretリソースを作成する場合、values.yamlなどにbase64エンコードした秘匿情報を記載することができますが、これも暗号化していないのでバージョン管理することができません。

helm-secretsはHelmのプラグインであり、暗号化されたvaluesファイルを複合化してHelmに渡すことができます。helm-secretsはAWS SecretManager、Azure KeyVault、HashiCorp Vault のようなクラウドネイティブのシークレットマネージャをサポートしており、シークレットを管理することができます。

https://github.com/jkroepke/helm-secrets

helm-secretsはbackendに```sops```と```vals```を使用することができますが今回はsopsを使用しています。

### install

バージョン指定をしてインストールすることが推奨されていますので、バージョンは確認してから実行してください。

```
helm plugin install https://github.com/jkroepke/helm-secrets --version v4.4.2
```

helmのインストールがまだの方はインストールしてください。Macであればbrewでインストールできます。

```
brew install helm
```

## GPGについて

## GPGで暗号化/複合化してみる

## ageを使用する

## まとめ