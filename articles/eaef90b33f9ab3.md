---
title: "helm-secretsをGPGで使用したいならモダンでシンプルなGo製のageを使おうという話"
emoji: "🔑"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["helm", "kubernetes", "sops", "gpg", "age"]
published: true
---

最近、kubernetesを学習していてMinikubeを使用してローカル環境でkubernetesクラスタを構築していろいろ試しており、Secretリソースをバージョン管理するのに暗号化したいなということでhelmのプラグインであるhelm-secretsを使用してみました。

helm-secretsは実際[sops](https://github.com/mozilla/sops)というライブラリを使用し暗号化しており、AWSやGCP、GPGなどが使用できるので今回はGPGを使用してみたところ複合化した際に非推奨のwarningが出たためいろいろ調べたことを記事にしてみました。

**結論から言うとhelm-secretsでGPGを使用したい場合、ageの使用が推奨されています**。

## helm-secretsとは

kubernetesのSecretリソースを作成するためのマニフェストファイルには秘匿情報をbase64でエンコードして記載しますが、暗号化しているわけではないのでGitHubなどでバージョン管理することができないです。Helmを使用していてSecretリソースを作成する場合、values.yamlなどにbase64エンコードした秘匿情報を記載することができますが、これも暗号化していないのでバージョン管理することができません。

helm-secretsはHelmのプラグインであり、暗号化されたvaluesファイルを複合化してHelmに渡すことができます。helm-secretsはAWS SecretManager、Azure KeyVault、HashiCorp Vault のようなクラウドネイティブのシークレットマネージャをサポートしており、シークレットを管理することができます。

https://github.com/jkroepke/helm-secrets

helm-secretsはbackendに```sops```と```vals```を使用することができますが今回は**sops**を使用しています。また、今回は学習目的の利用だったため各クラウドサービスにおけるシークレットマネージャーは利用せず**GPG**を使用することにしました。

### install

バージョン指定をしてインストールすることが推奨されていますので、バージョンは確認してから実行してください。

```
helm plugin install https://github.com/jkroepke/helm-secrets --version v4.4.2
```

sopsも裏で使用するのでインストールが必要です。Macであればbrewでインストール可能です。

```
brew install sops
```

helmのインストールがまだの方はインストールしてください。Macであればbrewでインストールできます。

```
brew install helm
```

## GPGについて

GPG(GunPG)とはOpenPGPという暗号方式のOSSの実装です。OpenPGPとは安全に電子メールを利用するために開発された暗号化システムであるPGPのオープンスタンダード版です。

OpenPGPにおける公開鍵は分散ネットワーク上に公開、共有するためのキーサーバーシステムであるSKS(Synchronizing Key Server)を使用することでOpenPGPにおける公開鍵をインターネット上に公開、検索することができる。

しかし、このSKSはアップロードした公開鍵を変更、削除することができず「公開鍵汚染」攻撃が問題となっていたりする。

今回使用するGPGの利用自体は問題ないですが、SKSを使用した公開鍵の扱いには前述した公開鍵汚染攻撃のリスクがあるようです。そのため、公開鍵の公開には```Keybase```を使用したり```keys.openpgp.org ```という新しい鍵サーバーを使用するなどの方法が主流のようです。

## GPGで暗号化/複合化してみる

### install

Macであればbrewでインストール

```
brew install gpg
```

### 鍵の生成

対話形式でいろいろ入力すると鍵が生成される

```
gpg --gen-key

> 氏名をユーザーIDとして入力
> メールアドレス
> キーフレーズ
```

:::message
記事を書く際には問題なく鍵生成ができたのですが、サブのIntel Macで鍵生成を実行したところscreenサイズが小さすぎるみたいなエラーが発生しました。これは、おそらくgpgにウインドウサイズが渡せてない的な話のようです。

https://github.com/kovidgoyal/kitty/issues/5844

もし、似たようなエラーが発生した方がいたら以下のコマンドを試してみてください。

```
brew install pinentry-mac

vim ~/.gnupg/gpg-agent.conf

# これを追記
pinentry-program /usr/local/bin/pinentry-mac

# これでエージェント停止したらできた
killall gpg-agent
```
:::

### 暗号化

慣習的に```secrets.yaml```というファイル名に秘匿情報を記載します。とりあえず以下のようにしてhelmチャートの作成と```secrets.yaml```を作成します。

```
helm create gpg-demo
```

```
vim secrets.yaml

secret: test
```

また、sopsを使用して暗号化する際に設定ファイルが必要なため```.sops.yaml```を作成します。

```
vim .sops.yaml

---
creation_rules:
# Encrypt with AWS KMS
# - kms: 'arn:aws:kms:us-east-1:222222222222:key/111b1c11-1c11-1fd1-aa11-a1c1a1sa1dsl1+arn:aws:iam::222222222222:role/helm_secrets'

# Encrypt using GCP KMS
# - gcp_kms: projects/mygcproject/locations/global/keyRings/mykeyring/cryptoKeys/thekey

# As failover encrypt with PGP (obtan via gpg --list-secret-keys)
# ここにgpg --list-secret-keysで確認できる鍵を記載する
- pgp: '000111122223333444AAAADDDDFFFFGGGG000999'
```

ディレクトリ構成は以下のようになります。

```
tree .

.
├── Chart.yaml
├── charts
├── secrets.yaml
├── templates
│   ├── NOTES.txt
│   ├── _helpers.tpl
│   ├── deployment.yaml
│   ├── hpa.yaml
│   ├── ingress.yaml
│   ├── service.yaml
│   ├── serviceaccount.yaml
│   └── tests
│       └── test-connection.yaml
└── values.yaml
```

では、以下のコマンドで暗号化してみます。

```
% helm secrets encrypt secrets.yaml

[PGP]	WARN[0000] Deprecation Warning: GPG key fetching from a keyserver within sops will be removed in a future version of sops. See https://github.com/mozilla/sops/issues/727 for more information. 
secret: ENC[AES256_GCM,data:wKtmEJ1C,iv:F90Q1VzlqH73aXNZFwMBlbWgO1N78FryK8Y66OT/l/I=,tag:vmvQfmkLm+QnJkPLawnAaw==,type:str]
sops:
    kms: []
    gcp_kms: []
    azure_kv: []
    hc_vault: []
    age: []
    lastmodified: "2023-06-28T11:00:24Z"
    mac: ENC[AES256_GCM,data:vEWV+mmhaGSHgmgfCHUXjiHbOrSeGoUwYxS4c1/dkE5TKQyg2sLHCmY9u2qbqMSy6EWBhUmja23rLYJa81ngjqwFokKGSaygonH51nYn3JUStU27FFv6usK7o4IjblKdviqZG2CS8W9CvCEemciYV0WNTWs3OtAZ3UOaxMTim0k=,iv:hKkO9ckeogaUMbl6EEEV1IXvgEyeGiOsP/ZlvFNeYDM=,tag:SpkAKo9DVO/Kercd9Tashw==,type:str]
    pgp:
        - created_at: "2023-06-28T11:00:22Z"
          enc: |
            -----BEGIN PGP MESSAGE-----

            hF4D3Pet+RYHh9sSAQdAYAzdXgsQApA/8cGmyArlecA4kB/wP4+BNOJ8HAC3XR8w
            Pw/j0t64dYiOFKoPIfNgL4/4gxHg/NdKQzbKs2KCpv5/j5WxiGaAOto5/M2nL9UV
            1GgBCQIQRt2aIaq6lPaobVGFu6svZ+qzVmREkNi/tZnt9iMDsy65gD/vdXmcVZyO
            kL/+6i2/8cGiUiJv5LbFTbD2buk22qsQtTuxspSxcaptCzbnp89KG2tdJyv2Wp30
            DfTYcqPjoNGq6A==
            =ZgrG
            -----END PGP MESSAGE-----
          fp: A75087362F034C31D8F328022F637F33F5AF15D1
    unencrypted_suffix: _unencrypted
    version: 3.7.3
```

secrets.yamlの中身は上記のように可読性が保たれたまま暗号化できましたが、警告が出ています。

> Deprecation Warning: GPG key fetching from a keyserver within sops will be removed in a future version of sops. 

これについてはsopsの以下のissueで議論されていました。

https://github.com/mozilla/sops/issues/727

デフォルトでハードコードされている```gpg.mozilla.org```キーサーバーは破綻しているので何かしらの対応が必要だと提案されており具体的には以下の2つが提案されている。

1. 推奨するデフォルトを```age```に切り替える。
2. Keybaseをサポートし、推奨するデフォルトに切り替える。

Keybaseのサポートは多くの人が望んでいるようだけど実装するのはそれなりにコストがかかるから```age```の推奨でいいんじゃないかみたいな感じだと思う。

また、キーをfetchしているのを削除まではしなくてもいいけど非推奨で警告出すでいいんじゃない？みたいな意見が交わされていたのでGPGが使えないわけではなさそうだけどできるなら使わない方がいいでしょう。

## ageとは

> ageはシンプルでモダンで安全なファイル暗号化ツール、フォーマット、Goライブラリです。

> 小さな明示的な鍵、設定オプションなし、UNIXスタイルの合成可能性が特徴です。

https://github.com/FiloSottile/age

新しめのGo製でシンプルでモダンなファイル暗号化ツールのようです。とりあえず、インストールします。Macであればbrewでインストール可能です。

```
brew install age
```

## ageを使用する

### 鍵の生成

以下のコマンドでkey.txtに鍵を生成して書き出します。

```
age-keygen -o keys.txt
```

### .sops.yamlを編集する

GPGの記載をコメントアウトしてageを新しく追記します。

```diff yaml:.sops.yaml
---
creation_rules:
# Encrypt with AWS KMS
# - kms: 'arn:aws:kms:us-east-1:222222222222:key/111b1c11-1c11-1fd1-aa11-a1c1a1sa1dsl1+arn:aws:iam::222222222222:role/helm_secrets'

# Encrypt using GCP KMS
# - gcp_kms: projects/mygcproject/locations/global/keyRings/mykeyring/cryptoKeys/thekey

# As failover encrypt with PGP (obtan via gpg --list-secret-keys)
- - pgp: 'xxxxxxxxxxxxxxxxxxxxxx'
+ # - pgp: 'xxxxxxxxxxxxxxxxxxxxxx'

+ age: 'xxxxxxxxxxxxxxxxxxxx'
```

### keys.txtのパスを指定する

sopsはユーザー構成ディレクトリのsopsサブディレクトリにあるkeys.txtという名前のファイルをデフォルトで探しに行きます。この場所はMacであれば```$HOME/Library/Application Support/sops/age/keys.txt```です。このパスは環境変数を指定することで変更できるようです。

```
export SOPS_AGE_KEY_FILE=/Users/yamanakajunichi/age/demo/key.txt
```

### 暗号化する

以下のコマンドで```secrets.yaml```を暗号化する。

```
helm secrets encrypt -i secrets.yaml
```

これでGitHubなどでバージョン管理が可能。secrets.yamlの値を複合化して使用するには通常のvalues.yamlと同じようにファイルを指定して使用することが可能です。

```
helm secrets upgrade gpg-demo . --install -f values.yaml -f secrets.yaml
```

## まとめ

今回の記事では以下について紹介しました。

- helm-secretsの基本的な使用の流れについて。
- GPGについての紹介。
- helm-secretsでGPGを使用したい場合の対応について。
- GO製のモダンなファイル暗号化ツールであるageの紹介。

kubernetesの秘匿情報の扱いについては他にもいくつか選択肢があるため、プロダクトに適切なものを使用してください。

今回は学習用途で利用していたため手元で鍵生成ができるGPGを使用したかったのと、values.yamlに安全に秘匿情報を記載してサクッとhelmテンプレートで指定したいという狙いがあったためhelm-secretsとGPGという組み合わせを選びました。

結局のところGPGは非推奨となりhelm-secretsとageの組み合わせにはなりましたがやりたいことは実現することができましたし、暗号まわりの技術についても少し学べたので結果的にはよかったです。

プロダクトで採用するならばAWSやGCPのシークレットマネージャーを利用するでしょうが、この記事が誰かのお役に立てれば幸いです。

今回は以上です🐼

## 参考

GPGについて
https://blog.livewing.net/gpg-life

