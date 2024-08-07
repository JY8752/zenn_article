---
title: "Cadenceの基礎[アクセス制御]"
---

Cadenceにおけるアクセス制御の方法にはCapabilityを用いたアクセス制御とアクセス識別子によるアクセス制御が存在します。本チャプターではアクセス識別子によるアクセス制御を紹介いたします。

## アクセス識別子

Cadenceにおけるアクセス識別子は以下の4つに分類されます。

1. pub(access(all))

```pub```または```access(all)```で宣言された変数や関数は外部に公開することができ、外部からでもアクセスすることが可能となります。

2. access(account)

```access(account)```で宣言された変数や関数は定義されているアカウント全体の範囲でのみアクセスすることができる。つまり、アカウント内の他のコントラクトは```access(account)```で宣言された変数や関数にアクセスすることができます。

3. access(contract)

```access(contract)```で宣言された変数や関数は定義されているコントラクト内でのみアクセスすることができる。つまり、アカウント内の他のコントラクトからはアクセスすることができません。

4. priv(access(self))

```priv```または```access(self)```で宣言された変数や関数は現在のスコープと内側のスコープでのみアクセスすることができる。