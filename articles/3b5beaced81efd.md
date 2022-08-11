---
title: "見ないようにしていたSpring Securityの認証実装と向き合った話(WebFlux)"
emoji: "😊"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["Java", "kotlin", "Spring"]
published: true
---
これの続き
https://zenn.dev/jy8752/articles/1a00cc7b077a2e

実装したコードはこちら
https://github.com/JY8752/auth-kotlin-spring

# 対象読者
- Spring bootある程度わかる人
- Spring Securityの認証実装をある程度理解している人
- WebFluxの実装に興味がある人

# Session認証
とりあえずお馴染みのUsernamePasswordAuthenticationFilterがなくてびびる
大体MVCのときのクラスにServerとかReactiveみたいなワードがついたクラスが用意されている

以下の記事を参考にさせていただきました！
https://nosix.hatenablog.com/entry/2018/07/30/143921

## SecurityConfig
- @EnableWebSecurityではなく@EnableWebFluxSecurity
- AuthenticationWebFilterをカスタムしてWebFilterとして登録する
- FilterにConverterを登録して認証パラメーターを処理できるようにしている
- UsernamePasswordAuthenticationTokenに認証情報をセットしてUserDetailsServiceまで持っていくのはMVCと変わらず

```kotlin:SecurityConfig.kt
package com.example.auth.session.demo.security

import com.example.auth.session.demo.JsonBodyAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.server.WebFilter

@EnableWebFluxSecurity //SpringSecurityの有効化
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: ServerHttpSecurity,
        authenticationManager: ReactiveAuthenticationManager,
        serverCodecConfigurer: ServerCodecConfigurer
    ): SecurityWebFilterChain {
        //認証設定
        val authenticationFilter = authenticationWebFilter(
            authenticationManager,
            serverCodecConfigurer,
            ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login")
        )
        http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

        //ログインのみ認証不要。その他は全て認証必要
        http.authorizeExchange()
            .pathMatchers("/login").permitAll()
            .anyExchange().authenticated()

        http.csrf().disable()

        //認可で拒否した場合の処理
        http.exceptionHandling().authenticationEntryPoint(AuthenticationEntryPoint())

        return http.build()
    }

    @Bean
    fun authenticationManager(
        passwordEncoder: BCryptPasswordEncoder,
        userDetailsService: HelloUserDetailsService
    ): UserDetailsRepositoryReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService).also {
            it.setPasswordEncoder(passwordEncoder)
        }
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    private fun authenticationWebFilter(
        authenticationManager: ReactiveAuthenticationManager,
        serverCodecConfigurer: ServerCodecConfigurer,
        loginPath: ServerWebExchangeMatcher
    ): WebFilter {
        return AuthenticationWebFilter(authenticationManager).also {
            //認証処理を行うリクエスト
            it.setRequiresAuthenticationMatcher(loginPath)
            //認証情報の抽出方法
            it.setServerAuthenticationConverter(JsonBodyAuthenticationConverter(serverCodecConfigurer.readers))
            //認証成功・失敗時の処理
            it.setAuthenticationSuccessHandler(AuthenticationSuccessHandler())
            it.setAuthenticationFailureHandler(AuthenticationFailureHandler())
            //セキュリティコンテキストの保存方法
            it.setSecurityContextRepository(WebSessionServerSecurityContextRepository())
        }
    }
}
```

## Converter

```kotlin:JsonBodyAuthenticationConverter.kt
package com.example.auth.session.demo

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.reactive.function.BodyExtractor
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

/**
 * Json形式で認証パラメーターを受け取るためのコンバーター
 */
class JsonBodyAuthenticationConverter(
    val messageReaders: List<HttpMessageReader<*>>
) : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return BodyExtractors.toMono(AuthenticationInfo::class.java)
            .extract(exchange.request, object : BodyExtractor.Context {
                override fun messageReaders(): List<HttpMessageReader<*>> = messageReaders
                override fun serverResponse(): Optional<ServerHttpResponse> = Optional.of(exchange.response)
                override fun hints(): Map<String, Any> = mapOf()
            })
            .map { it.toToken() }
    }
}

//認証リクエストの形式
data class AuthenticationInfo(
    @JsonProperty("mail_address")
    val mailAddress: String,
    val password: String
) {
    fun toToken() = UsernamePasswordAuthenticationToken(this.mailAddress, this.password)
}
```

## SeesionConfig
ここもアノテーションが違う

```kotlin:HttpSessionConfig.kt

package com.example.auth.session.demo.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession

@EnableRedisWebSession //WebFluxはこっち
//@EnableRedisHttpSession
@Configuration
class HttpSessionConfig {
    @Bean
    fun connectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory()
    }
}
```

# JWT認証
JWTの作成処理は前回と同様ユーティリティーのクラスを用意しておく。以下ポイント

- AuthenticationManagerとSecurityContextRepositoryを拡張している。
- AuthenticationManagerでトークンの期限とか検証している。
- SecurityContextRepositoryでリクエストヘッダーからトークンを抜き出してお馴染みのUsernamePasswordAuthenticationTokenを作成して検証と永続データとして保持。
- UserDetailsServiceも実装している。
- トークンの発行はControllerを自作して行なっている。
- この実装だとUserDetailsServiceの実装が無意味な気がする。

以下の記事がめちゃくちゃ参考になりました。
https://ard333.medium.com/authentication-and-authorization-using-jwt-on-spring-webflux-29b81f813e78

## AuthenticationManager

```kotlin:AuthenticationManager.kt
package com.example.auth.jwt.demo.security

import com.example.auth.jwt.demo.HelloUserDetailsService
import com.example.auth.jwt.demo.utils.JWTUtils
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager(
    private val userDetailsService: HelloUserDetailsService,
) : UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService) {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()
        val username = JWTUtils.getUsernameFromToken(authToken)

        return Mono.just(JWTUtils.validateToken(authToken))
            .filter { it }
            .switchIfEmpty(Mono.empty())
            .map {
                UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    emptyList() //必要であればロール設定を入れる
                )
            }
    }
}
```

## SecurityContextRepository

```kotlin:SecurityContextRepository.kt
package com.example.auth.jwt.demo.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepository(
    private val authenticationManager: AuthenticationManager
) : ServerSecurityContextRepository {
    override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> {
        throw java.lang.UnsupportedOperationException("Not supported yet.")
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        return Mono.justOrEmpty(exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION))
            .filter { it.startsWith("Bearer ") }
            .flatMap { authHeader ->
                val authToken = authHeader.substring(7)
                val auth = UsernamePasswordAuthenticationToken(authToken, authToken)
                this.authenticationManager.authenticate(auth).map { SecurityContextImpl(it) }
            }
    }
}
```

## SecurityConfig

```kotlin:SecurityConfig.kt
package com.example.auth.jwt.demo.security

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: AuthenticationManager,
    private val securityContextRepository: SecurityContextRepository,
) {

    @Bean
    fun securityFilterChain(
        http: ServerHttpSecurity,
    ): SecurityWebFilterChain {
        //csrf無効化
        http.csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()

        //ログインのみ認証不要。そのほかは認証要
        http.authorizeExchange()
            .pathMatchers("/login").permitAll()
            .anyExchange().authenticated()

        //認証・認可失敗時の処理
        http.exceptionHandling()
            .authenticationEntryPoint { swe, _ ->
                Mono.fromRunnable { swe.response.statusCode = HttpStatus.UNAUTHORIZED }
            }
            .accessDeniedHandler { swe, _ ->
                Mono.fromRunnable { swe.response.statusCode = HttpStatus.FORBIDDEN }
            }

        //カスタムした認証処理を挟む
        http.authenticationManager(this.authenticationManager)
            .securityContextRepository(this.securityContextRepository)

        return http.build()
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}
```

## Controller

```kotlin:AuthenticationController.kt

package com.example.auth.jwt.demo.controller

import com.example.auth.jwt.demo.utils.JWTUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class AuthenticationController(
    private val passwordEncoder: BCryptPasswordEncoder
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Mono<ResponseEntity<String>> {
        if (request.password != "password") {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
        }
        return Mono.just(ResponseEntity.ok(JWTUtils.generateToken(request.username)))
    }
}

data class LoginRequest(
    val username: String,
    val password: String
)
```

以上！！

