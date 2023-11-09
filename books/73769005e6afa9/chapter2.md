---
title: "DBの準備"
---

まずはアプリケーションの作成を始める前にDBの準備をします。本書で作成するアプリではMySQLを使用したいと思います。

## Dockerコンテナを起動する

今回は直接ローカルマシンにDBを起動するのではなく、Dockerを使用してMySQLコンテナを起動したいと思います。

```
% docker run --rm --name go-unittest-architecture-db \
  -p $MYSQL_PORT:$MYSQL_PORT \
  -e MYSQL_ROOT_PASSWORD=$MYSQL_PASS \
  -e MYSQL_DATABASE=$MYSQL_DBNAME \
  -d mysql:latest
```

今回作成するようなデモアプリではハードコーディングでも良かったのですが、なるべく実際の開発プロダクトに寄せるためにDBのパスワードなどの情報は環境変数に設定します。直接ローカルマシンに環境変数を設定してもよいのですが、何かと便利なので今回は```.env```ファイルを作成して管理したいと思います。また、```.env```に記載した値は[direnv](https://github.com/direnv/direnv)を使用することで自動で設定されるようにします。

このようなdotenv + direnvによる環境変数の管理は環境変数をファイルに記載するだけであまり意識せず使用できるので筆者はよく使います。

```shell:.env
MYSQL_USER=root
MYSQL_PASS=mysql
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DBNAME=gacha-demo
```

:::message alert
今回はデモアプリですが```.env```は秘匿情報が記載されるものなので```.gitignore```に追加し、バージョン管理の対象外にしてください。
:::

direnvは```.envrc```に記載されている変数をそのディレクトリに移動したときだけ、自動で環境変数に割り当てることができます。direnvはMacであればHomebrewでインストール可能です。

```
% brew update && brew install direnv

% direnv --version
2.32.1
```

インストールが完了したらフックの設定をします。

```shell:~/.zshrc
eval "$(direnv hook zsh)"
```

フックの設定までできたらシェルを再起動して、プロジェクトのルートに```.envrc```を作成し以下のように記載します。

```:.envrc
dotenv
```

こうすることで上述した```.env```ファイルを読み込み、自動的に環境変数を割り当ててくれるはずです。

:::message
新規作成後は初回のみ以下のコマンドを実行して有効化する必要がある。
```
% direnv allow .
```
:::

環境変数が設定できたらDockerコマンドを実行してコンテナを起動します。

```
% docker ps
CONTAINER ID   IMAGE          COMMAND                   CREATED         STATUS         PORTS                               NAMES
ea25a3d7d315   mysql:latest   "docker-entrypoint.s…"   4 seconds ago   Up 3 seconds   0.0.0.0:3306->3306/tcp, 33060/tcp   go-unittest-architecture-db
```

本書籍で使用するDBはこのコンテナを使用していきたいと思います。