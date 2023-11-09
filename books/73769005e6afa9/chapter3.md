---
"title": "タスクランナー(Taskfile)を導入する"
---

前回実行したDockerコマンドを毎回実行するのは少し面倒臭いので今回は[Taskfile](https://taskfile.dev/ja-jp/)というタスクランナーを導入します。以前はMakefileをタスクランナーとして使用することが多かったのですがTaskfileを知ってからは筆者はTaskfileをよく使います。

ちなみに、Taskfileについても以前記事を書いたのでもしよければこちらもご参照ください。

https://zenn.dev/jy8752/articles/80de70e2ed7d26

## Taskfileの導入

MacであればHomebrewでインストール可能です。

```
% brew install go-task

% task --version
Task version: 3.30.1
```

以下のコマンドで```Taskfile.yml```を作成します。

```
% task --init
# https://taskfile.dev

version: '3'

vars:
  GREETING: Hello, World!

tasks:
  default:
    cmds:
      - echo "{{.GREETING}}"
    silent: true
 created in the current directory

% task
Hello, World!
```

## DockerコマンドをTaskfileに追記する

前回実行したコンテナの起動コマンドを以下のようにタスク化します。

```yaml:Taskfile.yml
  db:run:
    cmds:
      - docker run --rm --name {{.CONTAINER_NAME}}
        -p $MYSQL_PORT:$MYSQL_PORT
        -e MYSQL_ROOT_PASSWORD=$MYSQL_PASS
        -e MYSQL_DATABASE=$MYSQL_DBNAME
        -d mysql:latest
    silent: true
    desc: 'Run MySQL container.'
```

コンテナ名は他のタスクでも使用する可能性があるのでグローバル変数として宣言しました。

```yaml
vars:
  CONTAINER_NAME: go-unittest-architecture-db
```

また、Taskfileは```.env```に対応しているので、以下のように記載することで```.env```記載の環境変数も使用することができます。

```yaml
dotenv:
  - ".env"
```

最終的な```Taskfile.yml```は以下のようになります。

```yaml:Taskfile.yml
version: '3'

dotenv:
  - ".env"

vars:
  CONTAINER_NAME: go-unittest-architecture-db

tasks:
  hello:
    cmds:
      - echo 'Hello World from Task!'
    silent: true
    desc: 'Hello Test Task.'
  db:run:
    cmds:
      - docker run --rm --name {{.CONTAINER_NAME}}
        -p $MYSQL_PORT:$MYSQL_PORT
        -e MYSQL_ROOT_PASSWORD=$MYSQL_PASS
        -e MYSQL_DATABASE=$MYSQL_DBNAME
        -d mysql:latest
    silent: true
    desc: 'Run MySQL container.'
```

Taskfileで```.env```の値を使用してコマンド実行できるため、タスクランナーとしてTaskfileを使用することで前回導入した```direnv```がなくても環境変数を使用してコマンド実行できるようにもなりました。

コンテナを起動する場合は以下のようにして起動することができます。

```
% task db:run
```

## Docker操作コマンドを一通りタスク化しておく

起動したコンテナへの接続とコンテナの停止もタスク化しておきましょう。

```yaml:Taskfile.yml
  db:connect:
    cmds:
      - docker exec -it {{.CONTAINER_NAME}} mysql -uroot -p$MYSQL_PASS $MYSQL_DBNAME
    silent: true
    desc: 'Connect MySQL container.'
  db:stop:
    cmds:
      - docker stop {{.CONTAINER_NAME}}
    silent: true
    desc: 'Stop MySQL container.When stop, container remove.'
```

この先もよく使うコマンドは同じようにタスク化して使用していきたいと思います。

登録したタスクは以下のように```--list-all```オプションを使用することで一覧として確認することができます。

```:(最終的に登録したタスク一覧)
% task --list-all
task: Available tasks for this project:
* generate:               execute `go generate` command. generate mock by `gomock` and di by `wire`.
* hello:                  Hello Test Task.
* db:connect:             Connect MySQL container.
* db:run:                 Run MySQL container.
* db:stop:                Stop MySQL container.When stop, container remove.
* generate:di:            execute `wire gen` command.
* migrate:create:         Create migration file.Migration name must be specified as an argument.ex) task migrate:create -- create_user_table
* migrate:down:           Execution migration down.
* migrate:force:          Execute force migration version.Migration version must be specified as an argument.ex)task migrate:force -- 2
* migrate:up:             Execution migration up.
* migrate:version:        Check current migration version.
* test:all:               execute all tests.
* test:e2e:               execute e2e tests.
* test:integration:       execute integration tests.
* test:unit:              execute unit tests.
* update:golden:          update golden file.
```