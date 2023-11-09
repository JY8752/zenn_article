---
"title": "ドメイン層の単体テスト"
---

本章では前回抽出したドメイン層の単体テストを作成していきます。

## testifyの導入

Go特有の話ですがGoではアサーションライブラリを使用せず標準モジュールの```testing```モジュールとif文によるチェックでアサーションをすることを推奨している言語です。
[Why does Go not have assertions?](https://go.dev/doc/faq#assertions)

ですが「単体テストの考え方/使い方」では**テストコードは資産ではなく負債である**と述べており、テストコードの保守性を高めるためには可能な限り**テストコードを読みやすく、短くする**必要があるとしています。

これは意見が分かれそうですがテストコードを極力短くするということを優先して今回は[testify](https://github.com/stretchr/testify)というモジュールを使用します。

```
go get github.com/stretchr/testify
```

## テストを作成する

作成した単体テストは以下の通りです。Goではテーブル駆動テストによるテストを推奨しているためテーブル駆動テストで作成しています。データ駆動テストやパラメーターテストなどとも呼ばれるようなテスト手法です。テーブル駆動テストについては公式のwikiでも紹介されています。

[TableDrivenTestsについて](https://github.com/golang/go/wiki/TableDrivenTests)

```go:domain/gacha_test.go
package domain_test

import (
	"testing"

	"github.com/JY8752/go-unittest-architecture/domain"
	"github.com/stretchr/testify/assert"
)

func TestDraw(t *testing.T) {
	t.Parallel()
	tests := map[string]struct {
		Weights []int
		Expect  int64
	}{
		"case1: when seed 10 total weights 16, rand 14": {
			Weights: []int{1, 5, 10},
			Expect:  3,
		},
		"case2: when seed 10 total weights 16, rand 14": {
			Weights: []int{1, 10, 5},
			Expect:  3,
		},
		"case3: when seed 10 total weights 16, rand 14": {
			Weights: []int{5, 1, 10},
			Expect:  3,
		},
		"case4: when seed 10 total weights 16, rand 14": {
			Weights: []int{5, 10, 1},
			Expect:  2,
		},
		"case5: when seed 10 total weights 16, rand 14": {
			Weights: []int{10, 1, 5},
			Expect:  3,
		},
		"case6: when seed 10 total weights 16, rand 14": {
			Weights: []int{10, 5, 1},
			Expect:  2,
		},
		"case1: only one item": {
			Weights: []int{1},
			Expect:  1,
		},
		"case2: only one item": {
			Weights: []int{1000},
			Expect:  1,
		},
	}

	for name, tt := range tests {
		name, tt := name, tt
		t.Run(name, func(t *testing.T) {
			// Arrange
			t.Parallel()
			sut := domain.NewGacha(newGachaItemWeights(t, tt.Weights))
			seed := int64(10)

			// Act
			itemId, err := sut.Draw(seed)
			if err != nil {
				t.Fatal(err)
			}

			// Asertion
			assert.Equal(t, tt.Expect, itemId)
		})
	}
}

func newGachaItemWeights(t *testing.T, weights []int) domain.GachaItemWeights {
	t.Helper()
	gachaItemWeights := make(domain.GachaItemWeights, len(weights))
	for i, w := range weights {
		gachaItemWeights[i] = struct {
			ItemId int64
			Weight int
		}{
			ItemId: int64(i + 1),
			Weight: w,
		}
	}
	return gachaItemWeights
}

```

このようなテーブル駆動テストが書けるのはテスト対象の関数が副作用のない純粋関数だからです。単体テストの対象を副作用のない純粋関数にすることでこのような**テーブル駆動テストが書きやすい**といったメリットもあります。

テストの内容としては関数に与える**seed値を固定**することで関数内で生成される乱数を固定にしています。生成される乱数が固定にされることで**モデルが持つアイテム一覧によってどのようなアイテムが抽選されるかが予測可能**なものとなるためいくつかのパターンを用意しテストを実施しています。

このような疑似乱数を扱う単体テストはしばしばテストが書きづらい例として挙げられます。もし、引数でseed値を指定せず関数内部でseedを生成するようにしていた場合、**関数の出力が予測不可能なものとなり単体テストを作成さることは困難です**。そのため、外部からseed値を与えるような作りで実装しました。このような例は疑似乱数以外にも現在時刻を取得するような処理でも同じようなことが言えるでしょう。

また、テスト対象の変数名を**sut**としているのは**テスト対象システム**(System Under Test)の略でどれがテスト対象なのかがわかりやすいように命名しています。

### AAAパターンについて

上記テストは**AAA(Arrange-Act-Assert)パターン**で書かれています。これは**Given-When-Then**パターン同様、準備、実行、検証の３ブロックでテストを記述するパターンでテストの可読性に繋がります。

各ブロックはわざわざコメントで何のブロックかを書かなくても自明のため適切に空行をはさむことでコメントに書かなくてもよいとされています。しかし、例えばArrangeブロックなんかはテスト対象オブジェクトの作成やモックオブジェクトの作成と長くなることもあり、そのような場合は同じブロック内で空行を挟みたくなるでしょう。そういった場合は明示的に**コメントで何のブロックかを記載したほうが良い**です。

筆者はコメントで明示的に記載したほうがどこからどこまでが何をしているかが明白で自分で書いていて読みやすいのでコメントを書いてしまうことが多いです。

### Actブロックは１行で十分

Actブロックは基本的にテスト対象の関数を実行するだけのはずなので**１行で済む**はずです。もし、これが1行で済まないのであればテスト対象のビジネスロジックで一連のビジネスフローが実施できていないことになる可能性があるため注意が必要です。

もし、テスト対象システムのエラー処理が気になる方は以下のようにエラー処理をラップするような関数を作成することで1行で書くことも可能です。(Goでは関数の戻り値が期待する出力とエラーの2値を返すことが多いため。)

```go
func execute[T any](t *testing.T, f func() (T, error)) T {
	t.Helper()
	v, err := f()
	if err != nil {
		t.Fatal(err)
	}
	return v
}
```

```go
// Act
// itemId, err := sut.Draw(seed)
// if err != nil {
// 	t.Fatal(err)
// }
itemId := execute(t, func() (int64, error) {
  return sut.Draw(seed)
})
```

### if文などの制御構文を含むできでない

テストコード内でif文などの制御構文がある場合は注意が必要です。もし、if文がどうしても必要なのであればそれは**一つのテストケースで多くのことを検証しようとしている**可能性があります。

例えばですが以下のような割り算を実行する関数のテストを作成するとします。

```go
func Divide(x, y int) (int, error) {
	if y == 0 {
		return 0, errors.New("zero divide")
	}
	return x / y, nil
}

func Test(t *testing.T) {
	tests := map[string]struct {
		X, Y    int
		Want    int
		WantErr error
	}{
		"10 / 5 = 2": {
			X:    10,
			Y:    5,
			Want: 2,
		},
		"10 / 3 = 3": {
			X:    10,
			Y:    3,
			Want: 3,
		},
		"10 / 6 = 1": {
			X:    10,
			Y:    6,
			Want: 1,
		},
		"0 divide is error": {
			Y:       0,
			WantErr: errors.New("zero divide"),
		},
	}

	for name, tt := range tests {
		name, tt := name, tt
		t.Run(name, func(t *testing.T) {
			result, err := Divide(tt.X, tt.Y)

			if tt.WantErr != nil {
				assert.Equal(t, tt.WantErr, err)
			}
			if tt.WantErr == nil && err != nil {
				t.Fatal(err)
			}
			assert.Equal(t, tt.Want, result)
		})
	}
}
```

二つ目の引数の値が0のときに0徐算となってしまうためエラーを返すような関数となっており、正常系と異常系をまとめてテーブル駆動テストで記述しています。そうすると、そのエラーが**期待するエラーなのかどうか**を判断するために**WantErr**というフィールドを構造体に追加することになり、検証フェーズにおいての**if文の条件分岐が複雑になり、かつ条件分岐が一つ増えて**しまっています。

Go言語特有の話ですが関数の戻り値に値とerrorの二つの値を返すことは非常に多く、このerrorの処理は仕方ないとして、それ以外のif文の条件分岐は余計な複雑性を招くことになり望むものではありません。

このような場合は無理に一つのテーブル駆動テストで全てを検証しようとせず、少なくとも**正常系と異常系でテストを分けてもいい**と筆者は考えます。

### テストの後始末はどうするか

各テストケースが完了した後に何かしらの後始末をしたいと考える方もいるかもしれません。通常このような後始末が求められるのは**一時ファイルの削除**や**DBの切断**などが考えられる。しかし、適切に単体テストの設計がされているのであればそのような**プロセス外依存**とのやりとりは発生しないため、単体テストにおいて後始末が求められることはほとんどないはずです。

## まとめ

- Go言語では標準モジュールでアサーションライブラリを提供していませんが、**可読性を優先してtestifyを導入**しました。
- Go言語では**テーブル駆動テストを推奨**しており、純粋関数に近い形で関数を作成することでテーブル駆動テストが書きやすくなります。
- 疑似乱数や日付を扱う場合、外部依存とすることでテストが書きやすくなることがあります。
- テストは**AAAパターン**(もしくはGiven-When-Thenパターン)を意識して書くと可読性が上がります。
- Actブロックが**1行にならない**時は**期待する振る舞いがドメイン層でできていない**可能性があります。
- テストコード中でif文が現れた場合、そのテストは**多くのことを検証しようとしている**可能性があります。

本章ではcontrollerから抽出したドメイン層の単体テストの作成と単体テストを書くにあたってのポイントや注意点について説明いたしました。次章ではcontrollerからDB処理を切り出したいと思います。