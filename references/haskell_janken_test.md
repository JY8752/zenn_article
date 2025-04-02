# Haskellじゃんけんゲームのテストコード

せっかくなのでテストを書いてみた。

## HUnitテスト

### test/LibUnitTest.hs

```haskell
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

## QuickCheckテスト

### test/LibPropertyTest.hs

```haskell
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

Property based testを実行するのにHandの値を生成するArbitraryの定義が必要なのだがHandと別のモジュールで定義しようとすると警告がでるのでnewtypeでラップしてねとコンパイラに言われたのでとりあえずそれっぽくしてみた。 