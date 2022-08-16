---
title: "kotlinでquarkusを使ってみた"
emoji: "📝"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["Java", "kotlin", "quarkus", "Spring"]
published: true
---
GoやTypeScriptなどJVM系じゃない言語を書いているとSpringが少し仰々しすぎるというかもっとライトに使えるフレームワークが欲しいなとふと思ったので前から気になっていたquarkusを触ってみた。

今回作ったサンプルコードはこちら
https://github.com/JY8752/quarkus-demo

# quarkusとは
コンテナ環境、特にk8sで使われることを意識して作られたJava製のマイクロフレームワーク。CLIコマンドでプロジェクト作成できたりと今どきな感じがする。kotlin対応も一応しているがそんなに積極的にサポートしてこうとしている感じはしない。詳しくは公式を。
https://ja.quarkus.io/about/

ちなみに同じようなマイクロフレームワークのmicronaut、helidonやktorとGithubのスター数の遷移を比べるとquarkusが急激に伸びていてスター数はトップになっている。
(ktorはサーバーkotlin開発者しか使えないから比べる対象じゃないかもだけど参考までに)
![](https://storage.googleapis.com/zenn-user-upload/d8be912568a4-20220814.png)

# セットアップ
とりあえずインストールから

```terminal
curl -Ls https://sh.jbang.dev | bash -s - trust add https://repo1.maven.org/maven2/io/quarkus/quarkus-cli/
curl -Ls https://sh.jbang.dev | bash -s - app install --fresh --force quarkus@quarkusio

quarkus -v
2.11.2.Final
```

インストールできたらプロジェクトを作成する。

```terminal
//create app
quarkus create app com.example:quarkus-demo \
--extension=kotlin,resteasy-reactive-jackson --gradle-kotlin-dsl
```

quarkus create appでプロジェクト作成。<パッケージ>:<アプリ名> を指定。--extensionでライブラリを指定。(kotlinの指定もここで)何も指定しないとmavenになるのでgradleの指定。(今回はbuild.greadle.ktsになるように指定)

成功するとこんな感じになっているはず。
![](https://storage.googleapis.com/zenn-user-upload/f3b939678bb4-20220814.png)

ビルドしてみる。

```terminal
quarkus build
```

動かしてみる。(サクッと動く)

```terminal
quarkus dev

2022-08-14 18:29:57,213 WARN  [io.net.res.dns.DnsServerAddressStreamProviders] (build-4) Can not find {} in the classpath, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS.
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2022-08-14 18:29:57,605 INFO  [io.quarkus] (Quarkus Main Thread) quarkus-zenn-demo 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.11.2.Final) started in 1.067s. Listening on: http://localhost:8080

2022-08-14 18:29:57,606 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2022-08-14 18:29:57,606 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kotlin, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, vertx]

--
Tests paused
Press [r] to resume testing, [o] Toggle test output, [:] for the terminal, [h] for more options>
```

起動するとインタラクティブにコマンドを受け付ける状態になっていてテスト動かしたりできる。ここはあんまりいじっていないのでよくわからない。

# データベースの準備(panache)
今回はMySQLをdockerで立ち上げて使用した。ORマッパーにはquarkusでpanacheというものが提供されているのでこれを使ってみる。主要なORマッパーは大体使えそうな気はする。

panacheの面白いのがRepositoryパターンとactive-recordパターンで書き方が選べるようになっている。RepositoryパターンはSpringで開発するのと同じようなパターンでRepositoryのインターフェースがあってそれを実装したクラスがあってDTO作ってみたいな感じのやつ。ここは意見が分かれるだろうがSpringのお作法的な書き方やDTOやインターフェースや実装クラスが散らばる感じがあんまりしっくりこないなと思っていたところなので迷わずactive-recordパターンで書いてみた。

とりあえず依存関係追加。build.gradle.ktsにそのまま追記してもいいけどコマンドでも追加できる。

```terminal
quarkus extension add quarkus-hibernate-orm-panache-kotlin quarkus-jdbc-mysql
 or
//panache
implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
implementation("io.quarkus:quarkus-jdbc-mysql")
```

データベースはこんな感じ
![](https://storage.googleapis.com/zenn-user-upload/6476cb1ca7c1-20220814.png)

```kotlin:User.kt
package com.example.data

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import javax.persistence.*

@Entity(name = "user")
@Cacheable
class User : PanacheEntityBase {
    companion object: PanacheCompanion<User> {
        fun findByName(name: String) = find("name", name).firstResult()
        fun deleteStefs() = delete("name", "Stef")
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int")
    var id: Long? = null

    lateinit var name: String
}
```

Userテーブルはこんな感じになる。Entityもクエリメソッドも一箇所に集まっている。これでUserEntityとUserRepositoryとUserRepositoryImplとUserDTOをバラバラの場所に作らなくて良くなる。最高。これが嫌ならRepositoryパターンで書くか、Springで実装すればいい。

実装のポイント的なことを書くと
- EntityクラスにPanacheEntityBaseもしくはPanacheEntityを継承させる。
- テーブルのフィールドはクラスのプロパティとして設定すればいい。
- クエリのメソッドはcompanion objectにPanacheCompanionを継承させていろいろ定義できる。
- @Entityとか@ColumnみたいなアノテーションはSpringのときみたいに使える

ハマったところは少しでもテーブル定義と一致しないとエラーになるので@Columnなどのアノテーションで細かく指定する必要がある。

あとPanacheEntityを継承すれば下記のような実装になっているのでIdフィールドを定義する必要はないのだけどLong型idとして定義されているので、id以外の命名の主キーを使っていたり、bigintでなくintなどで定義していたりするとエラーになるのでPanacheEntityBaseの方を継承して自分で定義する必要がある。

```kotlin:PanacheEntity
open class PanacheEntity: PanacheEntityBase {
    /**
     * The auto-generated ID field. This field is set by Hibernate ORM when this entity
     * is persisted.
     *
     * @see [PanacheEntity.persist]
     */
    @Id
    @GeneratedValue
    open var id: Long? = null

    /**
     * Default toString() implementation
     *
     * @return the class type and ID type
     */
    override fun toString() = "${javaClass.simpleName}<$id>"
}
```

一応Taskテーブルはこんな感じ

```kotlin:Task.kt

package com.example.data

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import java.time.LocalDateTime
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity(name = "task")
@Cacheable
class Task() : PanacheEntityBase {
    companion object : PanacheCompanion<Task>

    constructor(user: User, details: String) : this() {
        this.user = user
        this.details = details
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int")
    var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "user_id")
    lateinit var user: User

    @Column(name = "details", columnDefinition = "text")
    lateinit var details: String

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
}
```

# Resource(Controller)
次にControllerクラス。quarkusだとxxxResourceみたいな感じで実装していた。

```kotlin:UserResource.kt
package com.example.resource

import com.example.data.User
import javax.transaction.Transactional
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

@Path("/user")
class UserResource {
    @POST
    @Transactional
    @Produces("application/json")
    @Consumes("application/json")
    fun create(name: String): Response {
        val user = User().also {
            it.name = name
            it.persist()
        }
        return Response.ok(user).build()
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    fun get(@PathParam("id") id: Long): Response {
        val user = User.findById(id)
        return Response.ok(user).build()
    }
}
```
特に書くことないけどPOSTの時に@Transactionalをつけないとエラーになる。

Taskはこんな感じ

```kotlin:TaskResource.kt
package com.example.resource

import com.example.data.Task
import com.example.data.User
import com.fasterxml.jackson.annotation.JsonProperty
import javax.transaction.Transactional
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

@Path("/task")
class TaskResource {

    @POST
    @Transactional
    @Produces("application/json")
    @Consumes("application/json")
    fun create(request: CreateTaskRequest): Response {
        val user = User.findById(request.userId) ?: kotlin.run {
            return Response.status(403).build()
        }
        val task = Task(user, request.details).also { it.persist() }
        return Response.ok(task).build()
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    fun get(@PathParam("id") id: Long): Response {
        val task = Task.findById(id) ?: kotlin.run {
            return Response.status(404).build()
        }
        return Response.ok(task).build()
    }
}

data class CreateTaskRequest(
    @JsonProperty("user_id")
    val userId: Long,
    val details: String)
```

# テスト
テストは2種類あってnativeテストと通常のテストに分かれている。nativeは後述。

```kotlin:UserTest.kt
package com.example.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import javax.transaction.Transactional

@QuarkusTest
@TestTransaction
internal class UserTest {
    @Test
    fun test() {
        //given
        val user = User().also {
            it.name = "user"
            it.persist()
        }

        //when
        val find = User.findById(user.id!!)

        //then
        find shouldBe user
    }
}
```
ポイントとしては
- @QuarkusTestをつける。
- @Transactionalもしくは@TestTransactionをつける必要がある。@TestTransactionはテスト後にロールバックする。

基本的に単体テストであれば特に特別なことはないのだけどkotestがそのままだと使えなかったのが残念。あるにはあるっぽかったのだけどまだ開発中？のような感じだったので素直にJUnitで書いた方がいい。
https://github.com/kotest/kotest-examples-quarkus

```kotlin:UserResource.kt
package com.example.resource

import com.example.data.User
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import javax.transaction.Transactional

@QuarkusTest
@Transactional
internal class UserResourceTest {
    @Test
    fun test() {
        given()
            .pathParam("id", 1)
            .`when`().get("/user/{id}")
            .then()
            .statusCode(200)
            .body(`is`("""
                {"id":1,"name":"user"}
            """.trimIndent()))
    }
}
```

# native imageビルド
quarkusのようなマイクロフレームワークを使う理由の一つとしてnative-imageビルドがあがると思うので試してみる。簡単に説明すると今までのJVMではなくGraalVMを使用してnativeビルドをするということである。nativeビルドの何が嬉しいかっていうと事前に生成したネイティブマシンコードを実行するので爆速で起動する。これは今までのJITコンパイラではなくAOTコンパイルをしているよう。サーバーレスみたいな起動と破棄が繰り返されるようなケースには適しているようだが長期で稼働するケースだと今まで通りのJITコンパイラでの実行の方がパフォーマンスは良いことがあるらしい。

## インストール
nativeビルドをするにはGraalVM対応のJDKが必要なのでインストールする。GraalVMはHomeBrewでインストールし、パスはjenvで設定。

```terminal
//JDK
brew install --cask graalvm/tap/graalvm-ce-java17

//パスを通す
export PATH=/Library/Java/JavaVirtualMachines/graalvm-ce-java17-22.2.0/Contents/Home/bin:"$PATH"

//警告が出たら以下のコマンド必要
sudo xattr -r -d com.apple.quarantine /Library/Java/JavaVirtualMachines/graalvm-ce-java17-22.2.0/

gu --version
GraalVM Updater 22.2.0

//jenvに追加
jenv add `/usr/libexec/java_home -v "17"` 

//graalのjavaに切り替える
jenv global graalvm64-17.0.4

//native-imageのインストール
gu install native-image
```

## ビルド
他の方の記事を読んだりするとビルドが10分以上かかって耐えられないとか見るので覚悟していたが2分半くらいで終わった。
```terminal
./gradlew build --native
```

## 起動
ビルドされたrunnerを実行する。爆速
```terminal
./build/kotlin-demo3-1.0.0-SNAPSHOT-runner
```

## テスト
quarkusはネイティブ実行のテストも用意されている。以下のように@QuarkusIntegrationTestをつけ作成したテストクラスを継承させるだけで良い。

```kotlin
package com.example

import io.quarkus.test.junit.QuarkusIntegrationTest

@QuarkusIntegrationTest
class GreetingResourceIT : GreetingResourceTest()
```

# まとめ
- panacheがJavaっぽくなくて最高。
- 実際のプロダクトになるとどうかわからないけど軽くいじった感じの開発体験は悪くない。
- 公式のガイドが充実してる。
- gRPCやGraphQLのガイドとかもあったので本気で採用を考えてるならガイドをやってみるといいと思う。
- kotlinのサポートはイマイチ。対応はしてるけどいろいろとJava目線な気がする。kotest使えないのも残念。
- nativeビルドが思ったより早かった。(プロジェクトの規模感やビルド環境にもよるかもだけど、手元でいじるのに苦痛な感じはなかった。)

実プロジェクトに採用するにはもう少し調査必要な感じだと思うけど、特に問題はなさそう。とりあえずnativeビルドは一旦使わないでSpringに変わるフレームワークとして使って、いつでもnativeビルド対応できますよの状態にしておくがいい感じがする。ただ、kotlinで使うにはktorのがいいんじゃないかとなる。Javaで開発してたら少なくとも個人で何か作る分にはquarkus使うと思う。

Springに変わるのはktorが濃厚な気がするけど新しいフレームワーク触るの楽しかったので次はmicronautも触ってみる