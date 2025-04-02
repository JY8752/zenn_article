---
title: "Haskellに入門したのでじゃんけんゲームを作ってみた"
emoji: "✂️"
type: "tech"
topics: ["haskell"]
published: true
---

## はじめに

今まで関数型プログラミングについてちょこちょこ学んできましたが「関数型ドメインモデリング」に影響されてHaskellに入門しました。入門過程は以下にまとめてあるので興味がある方はご参照ください。

https://zenn.dev/jy8752/scraps/47a88a2e367f2a

本記事は学んだことのアウトプットとして簡単なじゃんけんゲームを実装してみたのでその備忘録になります。
以下は実装したプログラムの実行イメージになります。

![](https://storage.googleapis.com/zenn-user-upload/85731e4c03e6-20250402.gif)

## 対象読者

- Haskellに興味がある方
- 関数型プログラミングを学び始めた方
- 基本的なプログラミングの概念を理解している方

## Haskellプロジェクトの作成

まず、Haskellのプロジェクトを作成するために、Stackというビルドツールを使用します。StackはHaskellのプロジェクト管理やビルドを簡単に行うことができるツールです。

```bash
stack new janken-app
cd janken-app
```

これで基本的なプロジェクト構造が作成されます。主なファイルは以下の通りです：

- `app/Main.hs`: メインの実行ファイル
- `src/Lib.hs`: ライブラリコード
- `test/Spec.hs`: テストコード
- `package.yaml`: プロジェクトの設定ファイル

## Haskellでじゃんけんゲームを実装してみる

じゃんけんゲームの実装では、以下のような型と関数を定義していきます。

```haskell
data Hand = Rock | Scissors | Paper
  deriving (Show, Eq)

data Result = Win | Lose | Draw
  deriving (Show, Eq)

handToStr :: Hand -> String
handToStr Rock = "グー"
handToStr Scissors = "チョキ"
handToStr Paper = "パー"

strToHand :: String -> Maybe Hand
strToHand "グー" = Just Rock
strToHand "チョキ" = Just Scissors
strToHand "パー" = Just Paper
strToHand _ = Nothing

judgeJanken :: Hand -> Hand -> Result
judgeJanken Rock Scissors = Win
judgeJanken Scissors Paper = Win
judgeJanken Paper Rock = Win
judgeJanken h1 h2
  | h1 == h2 = Draw
  | otherwise = Lose
```

このコードでは：

1. `Hand`型でじゃんけんの手を表現
2. `Result`型で勝敗を表現
3. `handToStr`関数で手を文字列に変換
4. `strToHand`関数で文字列を手に変換
5. `judgeJanken`関数で勝敗を判定

という基本的な機能を実装しています。

### じゃんけんゲームの実行

実際にじゃんけんゲームを実行するための`playJanken`関数を実装します：

```haskell
import System.Random (randomRIO)

playJanken :: IO ()
playJanken = do
  putStrLn "じゃんけんゲームを開始します"
  putStrLn "グー、チョキ、パーのいずれかを入力してください"
  userInput <- getLine
  case strToHand userInput of
    Nothing -> do
      putStrLn "不正な入力です"
      playJanken
    Just userHand -> do
      computerHand <- randomRIO [Rock, Scissors, Paper]
      putStrLn $ "あなた: " ++ handToStr userHand
      putStrLn $ "コンピュータ: " ++ handToStr computerHand
      case judgeJanken userHand computerHand of
        Win -> putStrLn "あなたの勝ちです！"
        Lose -> putStrLn "コンピュータの勝ちです！"
        Draw -> putStrLn "あいこです！"
      putStrLn "もう一度プレイしますか？(y/n)"
      again <- getLine
      when (again == "y") playJanken
```

この`playJanken`関数では：

1. ユーザーからの入力を受け取り、`strToHand`関数で`Hand`型に変換
2. コンピュータの手をランダムに選択
3. 両者の手を表示
4. `judgeJanken`関数で勝敗を判定して結果を表示
5. もう一度プレイするかどうかを確認

という流れでじゃんけんゲームを実行します。

作成した完全なコードはこちらになります。

:::details src/Lib.hs
```haskell:src/Lib.hs
module Lib
  ( playJanken,
    Hand (..),
    Result (..),
    handToStr,
    strToHand,
    judgeJanken,
  )
where

import Control.Monad (when)
import qualified Data.Text as T
import qualified Data.Text.IO as TIO
import qualified System.IO as IO
import System.Random (randomRIO)

-- じゃんけんの手を表す型
data Hand = Rock | Scissors | Paper
  deriving (Eq, Show, Read)

-- 勝敗の結果を表す型
data Result = Draw | Win | Lose
  deriving (Eq, Show)

-- 日本語表記への変換
handToStr :: Hand -> String
handToStr Rock = "グー"
handToStr Scissors = "チョキ"
handToStr Paper = "パー"

-- 結果の日本語表記への変換
resultToStr :: Result -> String
resultToStr Draw = "あいこです"
resultToStr Win = "あなたの勝ちです"
resultToStr Lose = "あなたの負けです"

-- 日本語入力からHandへの変換
strToHand :: String -> Maybe Hand
strToHand "グー" = Just Rock
strToHand "チョキ" = Just Scissors
strToHand "パー" = Just Paper
strToHand _ = Nothing

-- ランダムな手を生成
randomHand :: IO Hand
randomHand = do
  n <- randomRIO (0, 2) :: IO Int
  return $ case n of
    0 -> Rock
    1 -> Scissors
    _ -> Paper

-- 勝敗判定
judgeJanken :: Hand -> Hand -> Result
judgeJanken player computer
  | player == computer = Draw
  | (player == Rock && computer == Scissors)
      || (player == Scissors && computer == Paper)
      || (player == Paper && computer == Rock) =
      Win
  | otherwise = Lose

-- じゃんけんゲームを実行
playJanken :: IO ()
playJanken = do
  IO.hSetEncoding IO.stdin IO.utf8
  IO.hSetEncoding IO.stdout IO.utf8

  putStrLn "じゃんけんをしましょう！"
  putStrLn "「グー」、「チョキ」、「パー」のいずれかを入力してください："

  input <- TIO.getLine
  case strToHand (T.unpack input) of
    Nothing -> do
      putStrLn "入力が正しくありません。「グー」、「チョキ」、「パー」のいずれかを入力してください。"
      playJanken
    Just playerHand -> do
      computerHand <- randomHand

      putStrLn $ "あなた: " ++ handToStr playerHand
      putStrLn $ "コンピュータ: " ++ handToStr computerHand

      let result = judgeJanken playerHand computerHand
      putStrLn $ resultToStr result

      when (result == Draw) $ do
        putStrLn "もう一度じゃんけんをしましょう！"
        playJanken
```
:::

## テストを書く

Haskellでは、HUnitとQuickCheckという2つのテストフレームワークを使用してテストを書くことができます。

### HUnitテスト

HUnitはユニットテストを書くためのフレームワークです。以下のようにテストを書くことができます：

```haskell
unitTests :: Test
unitTests = TestList
  [ "handToStr tests" ~: TestList
    [ "グー" ~: "グー" ~=? handToStr Rock
    , "チョキ" ~: "チョキ" ~=? handToStr Scissors
    , "パー" ~: "パー" ~=? handToStr Paper
    ]
  , "judgeJanken tests" ~: TestList
    [ "同じ手はあいこ" ~: Draw ~=? judgeJanken Rock Rock
    , "グーはチョキに勝つ" ~: Win ~=? judgeJanken Rock Scissors
    ]
  ]
```

作成した完全なテストコードはこちら

```haskell:test/LibUnitTest.hs
module LibUnitTest (unitTests) where

import Test.HUnit
import Lib

unitTests :: Test
unitTests = TestList
  [ "handToStr tests" ~: TestList
    [ "グー" ~: "グー" ~=? handToStr Rock
    , "チョキ" ~: "チョキ" ~=? handToStr Scissors
    , "パー" ~: "パー" ~=? handToStr Paper
    ]
  , "strToHand tests" ~: TestList
    [ "グー" ~: Just Rock ~=? strToHand "グー"
    , "チョキ" ~: Just Scissors ~=? strToHand "チョキ"
    , "パー" ~: Just Paper ~=? strToHand "パー"
    , "不正な入力" ~: Nothing ~=? strToHand "不正な入力"
    ]
  , "judgeJanken tests" ~: TestList
    [ "同じ手はあいこ" ~: Draw ~=? judgeJanken Rock Rock
    , "グーはチョキに勝つ" ~: Win ~=? judgeJanken Rock Scissors
    , "チョキはパーに勝つ" ~: Win ~=? judgeJanken Scissors Paper
    , "パーはグーに勝つ" ~: Win ~=? judgeJanken Paper Rock
    , "グーはパーに負ける" ~: Lose ~=? judgeJanken Rock Paper
    , "チョキはグーに負ける" ~: Lose ~=? judgeJanken Scissors Rock
    , "パーはチョキに負ける" ~: Lose ~=? judgeJanken Paper Scissors
    ]
  ]
```

### QuickCheckテスト

QuickCheckはプロパティベーステストを書くためのフレームワークです。以下のような性質をテストできます：

```haskell
-- 同じ手を出したら必ず引き分けになる
prop_sameHandIsDraw :: TestHand -> Bool
prop_sameHandIsDraw (TestHand h) = judgeJanken h h == Draw

-- strToHandとhandToStrは互いに逆関数
prop_strToHandInverse :: TestHand -> Bool
prop_strToHandInverse (TestHand h) = strToHand (handToStr h) == Just h
```

詳しくは割愛しますが、プロパティベーステスト(PBT)ではテスト対象の関数をとても多くのプロパティを与えてテストすることで通常のテストではテストしきれないエッジケースもテストすることができるテスト手法です。これだけ聞くと銀の弾丸のように聞こえてしまいますがPBTを実際に実施するには同じ入力であれば毎回同じ出力が返ってくるようないわゆる副作用のない純粋関数であることが必要だったり(厳密には副作用のある関数もPBTでテストすることができたと思いますがあまりに複雑な関数はPBTでテストを書く難易度が格段に上がるでしょう)、テストの書き方が通常のユニットテストと少し雰囲気が違うためPBTに対しての知見が求められます。

前述のような特徴があるので主に関数型言語でよく聞く手法ですがGoやRubyといった言語でもPBTを使ってテストを書くためのツールが存在します。しかし、ErlangやHaskellのような関数型言語で使われるPBTツールと比べてしまうと機能が足りてないものが多いでしょう。

プロパティベーステストについては大変奥が深い内容だと思っているのでもし興味がある方は[実践プロパティベーステスト](https://www.lambdanote.com/collections/proper-erlang-elixir)という素晴らしい書籍が出版されているのでそちらを読むことをおすすめいたします。また、筆者がPBTについて学んだときの記事がありますので雰囲気を知りたい方はこちらもどうぞ。

https://zenn.dev/jy8752/articles/222d83ffa4bbf8

作成した完全なテストコードはこちら

```haskell:test/LibPropertyTest.hs
module LibPropertyTest (main, prop_sameHandIsDraw, prop_strToHandInverse, prop_judgeJankenSymmetric) where

import Test.QuickCheck
import Lib

newtype TestHand = TestHand { unTestHand :: Hand }
  deriving (Show)

instance Arbitrary TestHand where
  arbitrary = TestHand <$> elements [Rock, Scissors, Paper]

-- 同じ手を出したら必ず引き分けになる
prop_sameHandIsDraw :: TestHand -> Bool
prop_sameHandIsDraw (TestHand h) = judgeJanken h h == Draw

-- strToHandとhandToStrは互いに逆関数
prop_strToHandInverse :: TestHand -> Bool
prop_strToHandInverse (TestHand h) = strToHand (handToStr h) == Just h

-- じゃんけんの判定は対称的
prop_judgeJankenSymmetric :: TestHand -> TestHand -> Bool
prop_judgeJankenSymmetric (TestHand h1) (TestHand h2) =
  case judgeJanken h1 h2 of
    Draw -> judgeJanken h2 h1 == Draw
    Win -> judgeJanken h2 h1 == Lose
    Lose -> judgeJanken h2 h1 == Win

main :: IO ()
main = do
  putStrLn "\nRunning QuickCheck tests..."
  quickCheck prop_sameHandIsDraw
  quickCheck prop_strToHandInverse
  quickCheck prop_judgeJankenSymmetric 
```

## おわりに

正直AI補助ありで書いたので0からHaskellのプログラムを書けるほど習得できたかというとあやしいですが、当初の目的であった純粋関数型言語のプログラミングスタイルを実際に手を動かすことで学べたと思います。また、Haskellを学ぶ中でFunctor, Applicative, Monad, 関数合成, カリー化, パターンマッチング, 再帰関数といったよく聞く概念を改めて学ぶことができました。これらの概念を0から説明できるほど理解できたかというとちょっと難しいかなとは思いますがHaskellを学ぶ前よりは確実に理解度は深まったと思いますし、関数型言語への苦手意識というか勝手に思っていた難しさみたいなのがだいぶなくなりました。何が言いたいかというとHaskellを通して関数型言語とちょっと仲良く慣れた気がします。

あと、関数型言語の解説などを見るとどうしてもHaskellのコードを目にすることが多いのでHaskellをある程度読めるようになったのは良かったかなと思ってます。

競プロをHaskellで解いたりすることもできるようなので機会があったらもう少しHaskell書いてみたいなと思います。

実際のコードは[GitHubリポジトリ](https://github.com/JY8752/haskell-janken-app)で公開していますので、興味のある方はぜひご覧ください。

今回は以上です🐼
