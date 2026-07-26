---
title: "StitchとDESIGN.mdではじめるバイブデザイン ― デザインから実装まで"
emoji: "🎨"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["AI", "Stitch", "Codex", "デザイン", "MCP"]
published: true
---

:::message
この記事は執筆補助に最大限AIを活用していますがパッションは込めたつもりです。
:::

## はじめに

放置気味の個人で運営しているプロフィール兼ブログサイトのデザインをリニューアルしたいと思い、AIでUIデザインを生成できるStitchを使ってみることに。

この記事は以下のスクラップをまとめたものです。

https://zenn.dev/jy8752/scraps/2f2e58fdd2b3c2

今回リニューアルしたサイトと成果物のリポジトリはこちらです。

https://jy-panda.com/

https://github.com/JY8752/my-blog

:::message alert
Stitchを使う場合、執筆時点ではデフォルトでモデルの学習にプロンプトが使われて
しまいます。学習させたくない場合は、設定ページから学習しないようにした方が
良さそう。
:::

## Stitchとは

StitchはGoogle Labsが提供しているAIを活用したUIデザインツールで、執筆時点ではBetaリリースとなっている。

https://stitch.withgoogle.com/

文章や参考画像、WebサイトのURLを入力すると、Webサイトやアプリの画面デザインを
生成し、生成結果はFigmaやフロントエンドコード、MCP経由で実装工程へ引き継げる。
さらに、色、タイポグラフィ、余白、コンポーネントの方針などをまとめた
`DESIGN.md` も出力できる。

AIを使ってデザインや実装を生成するツールはいろいろありそうです。筆者が知っていた
ものは以下の3つで、そのうちの1つがStitchでした。とはいえ、聞いたことがある程度。

| ツール | 調べた範囲での印象 |
| --- | --- |
| v0 | Next.jsやReactのコード生成までまとめて進めたい場合に向いていそう |
| Figma Make | 既存のFigmaコンポーネントやデザインシステムを利用したプロトタイプ作成に向いていそう |
| Stitch | デザインと `DESIGN.md` をAIエージェントへ渡し、デザインと実装を往復する開発に向いていそう |

v0とFigma Makeは実際には使用していないため、あくまで調べた範囲での印象にすぎないです。

今回は既存の個人ブログをリニューアルし、普段使っているAIエージェントで実装したかったのと、単純に気になっていたのでStitchを触ってみることにしました。

## Stitchでデザインを生成する

とりあえず使ってみる。Stitchはデザイン生成ツールなのでStitchでデザインを生成してみることに。

StitchはWebアプリでブラウザからアクセスし、トップページにはよくあるAIチャットのUIが表示されています。Webとアプリのどちらを作成するか選ぶことができ、プロンプトを入力すると、生成したデザインがキャンバス上へ追加されます。

執筆時点では入力に以下も指定できそう。

- ファイルアップロード
- WebサイトURL

モデルは現時点で以下が選択できる。

- Thinking with 3 Pro
- Redesign (Nano Banana Pro)
- 2.5 Pro
- Fast

モデルは用途に応じて切り替えられる。公式ドキュメントでは `Fast` と記載されて
いた箇所が、実際の画面では `Ideate` になっているなど、UIとドキュメントに差がある
部分も。

https://stitch.withgoogle.com/docs/learn/design-modes/

まずはリニューアル前のプロフィールサイトをStitchへ。

![リニューアル前のプロフィールサイト](https://static.zenn.studio/user-upload/12462f73f0e5-20260718.png)
*リニューアル前のプロフィールサイト*

公開しているサイトのURLを入力に指定し、Nano Bananaへ次のプロンプト。

> プロフィールサイトのデザインがイケてないのでデザインをリニューアルしてください。
> 全体的にはダーク系
> 暗すぎないでPopで可愛い感じがだせると良い

![](https://static.zenn.studio/user-upload/b76657ae0a15-20260718.png)

なるほど、Nano Bananaで作ったスライドっぽい感じのデザインが出てきた。
ファイルアップロードもできるので、プロフィール画像を追加してもう1回生成。

![](https://static.zenn.studio/user-upload/b3d10ae10421-20260718.png)

なんか微妙。というかデザインというより1枚絵みたいな感じ？

そこでモードをThinking with 3.1 Proへ変更し、次のプロンプトでもう1回生成してみる。

> この感じでいいんだけど全体的にうるさすぎる印象。可愛い感じにはなってるけどもう少し余白を作って色の数も減らしていいと思う。あと背景色はもう少し明るくできると良さそう

![Thinking with 3.1 Proで生成したデザイン](https://static.zenn.studio/user-upload/7d1ee8c7b986-20260718.png)
*Thinking with 3.1 Proで生成したデザイン*

だいぶ良くなった。

やって気づいたのですが、Thinking with 3.1 Proや3 Flashを使う場合は
`DESIGN.md` も生成されます。Nano Bananaで作るデザインはあくまで画像データに
なるんだろうか？このあたりはあんまりよく理解してないです。

複数のデザイン案を出してもらうことも可能なためデザインが決まっていない案出しの段階で複数パターンのデザインをまとめて出してもらえるのは良さそう。

これはただの感想だがデザインのイメージや要件出しの段階でライブデザインからのプロトタイプ動かすみたいなことがかなり現実的にできるようになっているなと感じた。

## DESIGN.md

これまで何度か出てきた `DESIGN.md`。簡単にいうと、デザイン版の `AGENTS.md`。
AIエージェントへデザイン情報を渡すためのファイル。

Stitchから出力したファイルを見ると、カラー、タイポグラフィ、角丸、余白などの
デザイントークンが細かく定義されている。

```yaml:.stitch/DESIGN.md
---
name: Soft Neon Dev
colors:
  surface: "#101224"
  on-surface: "#e1e0fb"
  primary: "#ffc9e0"
  secondary: "#64d5f4"
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 48px
    fontWeight: "800"
rounded:
  DEFAULT: 0.5rem
  xl: 1.5rem
spacing:
  unit: 8px
  section-margin: 6rem
---
```

デザイントークンだけでなく、デザインの概要、レイアウト、コンポーネントごとの
方針も記載されている。Stitchのデザインから出力するほか、手書きしたり、
Webサイトやコードベースから生成したりもできる。

成果物のリポジトリには、最初にStitchから出力した `DESIGN.md` と、後述するTasteを
使って作成した `TASTE_DESIGN.md` の両方を配置している。

https://github.com/JY8752/my-blog/blob/main/.stitch/DESIGN.md

https://github.com/JY8752/my-blog/blob/main/.stitch/TASTE_DESIGN.md

## Stitch MCPを設定する

Stitchにはremote MCPサーバーがあるため、MCPを設定すれば手元のAIエディタや
AIエージェントからStitchへアクセスできる。Stitchを使って実装までやる場合、おそらく後述の公式Skillsを使うことになり、SkillsはStitchのMCPを使うため、とりあえず、MCPの設定に進みます。

認証はAPI keyかOAuthのどちらか。今回はシンプルにAPI key。
Stitchの設定画面でAPI keyを生成し、Codexへ次の設定を追加。

```toml:config.toml
[mcp_servers.stitch]
url = "https://stitch.googleapis.com/mcp"

[mcp_servers.stitch.http_headers]
"X-Goog-Api-Key" = "xxxx"
```

永続的なAPI keyの使用に抵抗がある場合は、OAuth認証を選ぶと良さそう。

## Stitchのskillsを追加する

繰り返しになりますが、Stitchの良いところは、**Stitch上でのデザイン作業と
ローカルの開発環境を行き来しやすいところ**。そのフローに乗るためのskillsも
公開されているため追加する。

```shell
npx plugins add google-labs-code/stitch-skills --scope project --target claude-code
npx plugins add google-labs-code/stitch-skills --scope project --target cursor
npx plugins add google-labs-code/stitch-skills --scope project --target gemini-cli
```

Codexではマーケットプレイス経由でSkillsを追加するように記載されていたので、次のコマンドでStitch skillsのマーケットプレイスを追加した。

```shell
codex plugin marketplace add google-labs-code/stitch-skills --ref main \
  --sparse .agents/plugins \
  --sparse plugins/stitch-design \
  --sparse plugins/stitch-build \
  --sparse plugins/stitch-utilities
```

https://github.com/google-labs-code/stitch-skills

リポジトリには、デザインの生成、コードからのデザイン抽出、Stitchの画面から
Reactコンポーネントへの変換などを行うskillがある。これらを使うことで、
Stitchとローカルの開発環境を双方向に行き来できる。

興味がある方は以下の公式ドキュメントに目を通しておくと良さそう。

https://stitch.withgoogle.com/docs/skills/reference/

## ブログをリニューアルする

ここまでで以下までを試した。

- Stitchを利用し、既存サイトからデザインをStitch上のキャンバスへインポート
- インポートしたデザインをリデザイン
- リデザインしたデザインから `DESIGN.md` を抽出

Stitchから出力した DESIGN.md を成果物のリポジトリへ配置して、AIエージェントに適当にリニューアルの指示を出してみた。

![DESIGN.mdを使ってリニューアルしたブログ](https://static.zenn.studio/user-upload/a09fce7dc397-20260719.png)
*DESIGN.mdを使ってリニューアルしたブログ*

前よりは断然良くなった。`DESIGN.md` に定義されているデザイントークンも
ちゃんと使われていそう。プロンプトもそんなに複雑な指示も出していないし、AIエージェントとのやりとりも何往復かしかしていない。

ただ、たぶんコード生成にはskillが使われていなさそう。この時点ではコード生成用skillを
明示的に指定していなかったので、ちゃんと指示すれば使ってくれたのかも。

:::message
執筆時点でDESIGN.mdからコードを生成するskillsは以下。

- react-components
- shadcn-ui

コンポーネントを生成するような明示的な指示をすればSkillsが使われたのかもしれない。画面をまるっと作成するのは`stitch-loop`のようなSkillsを使うのを想定しているのかもしれない。この、Skillsに関しては公式の以下のリンクにあるようなデザインから画面生成のループフローのためのSkillsだが今回は触れていないので興味がある方は使ってみてください。

https://stitch.withgoogle.com/docs/skills/build-in-a-loop/
:::

やりたいことはできたので完成したデザインをStitchにアップロードしてみる。これには`code-to-design`とかのskillが使えそう。
公開URLをそのまま画面としてインポートするMCPは現状ないため、コードとサイト構成を参照してStitch上に画面を再構築してもらった。

![コードベースからStitch上に再構築した画面](https://static.zenn.studio/user-upload/7aaf604703b6-20260720.png)
*コードベースからStitch上に再構築した画面デザイン*

アップロードというより、コードベースから頑張ってデザインを再現するような感じ。
勝手に再現されたデザインもあるけど、見た限りでは9割くらい再現された。

## TasteでAIっぽいデザインを減らす

いい感じではあるのですが、生成したデザインは如何にも「AIで生成しました」という雰囲気が
デザインに残っているようです。このAI味を消すアプローチとして、
`taste-design` というskillが用意されていたりするようです。

https://stitch.withgoogle.com/docs/skills/taste/

> この素晴らしいエージェントスキルは、コミュニティメンバーであるレオン・リン（Leon Lin）氏によって提供されました。

https://github.com/Leonxlnx/taste-skill

このskillは、AIっぽくなりがちなデザインを厳密に禁止しているようで、
具体的には次のようなものがある。

- デザイン内での絵文字
- Interフォントや純粋な黒
- ネオンのような外側のグロー
- 均等幅の3カラムカード
- 架空の数値や中身のないUIテキスト
- AIが使いがちな決まり文句

このskillでは、デザイン前に**コンセプト案**も出力するようです。今回は次のような
記載があった。

> リニューアル案は、北海道の静けさと子育て後の学びを表す「Quiet Hokkaido Editorial」とし、温かい紙色・チャコール・低彩度の朱色1色、非対称レイアウト、カードを減らした編集的な記事一覧へ切り替えます。

「北海道」「子育て」「学び」というキーワードから、温かみのあるデザインテーマに
なったようです。サイト内容と関係なく汎用的なAIのデフォルトデザインを割り当てられるより、
サイトの内容からデザインを決めてもらえるのは良さそう。

`taste-design` skillで更新した `DESIGN.md` を使って再実装した結果がこちらです。

![Tasteを適用したライトモード](https://static.zenn.studio/user-upload/3ddb69204085-20260720.png)
*Tasteを適用したライトモード*

意外と良いか？ダーク系も作ってもらいました。

![Tasteを適用したダークモード](https://static.zenn.studio/user-upload/ccfae96efe54-20260720.png)
*Tasteを適用したダークモード*

良さそう。どちらも良かったので、ライトモードとダークモードを切り替えられるように
実装してもらった。

デザインは何度か調整。前の方が良かった気もしてきたが淡い感じの色味の方が好みなので、Tasteを使ったデザインを採用することに。

## 新しい画面をデザインして実装する

Stitchを使うユースケースはいろいろありそうですが、ざっくり次の2つ。

- 新しくサイトを作るとき、最初にデザインを作成する
- 既存サイトから `DESIGN.md` を抽出し、デザインテーマを維持して新しい画面を作成する

今回は後者を試すため、ブログにスクラップ一覧画面を追加してみる。

:::message
筆者はZennのスクラップ機能が大変気に入っており、技術内容以外にも調べたことをスクラップとして公開する場が欲しいと思っていたのでずっとブログに実装したいと思っていた。
:::

いろいろ試行錯誤しましたが、ブラウザのStitchからシンプルに `DESIGN.md` を指定して、作成してほしい
画面のプロンプトを投げることに。これはSkillsを使って手元のAIエージェントからデザインさせてもたぶん良い。

ただ、プロンプトで指示するたびに新しい画面が生成されるため、やりとりが増えると
キャンバスが混雑する。生成した画面を編集し続ける方法があるのかもしれませんが、
今回は見つけられなかったので、いったんあきらめ。

あとは画面遷移で紐付けたワイヤーフレーム的な形にも、現状ではできなそう。
プロトタイプなら画面遷移を再現できそうですが、画面同士に結びつきを
持たせるのは難しいんだろうか？

ここらへんはFigmaのほうがちゃんとしたデザイン構成ができそうな印象。

とりあえずStitchでざっくりとした画面を生成し、コードベースを参照できる
手元のCodexからデザインを調整してもらった。

![Codexから調整したスクラップ画面のデザイン](https://static.zenn.studio/user-upload/0008ddc6938e-20260721.png)
*Codexから調整したスクラップ画面のデザイン*

このデザインと `DESIGN.md` を使って実装した画面がこちら。

![実装したスクラップ画面](https://static.zenn.studio/user-upload/1f0b9111bb71-20260721.png)
*実装したスクラップ画面*

再現度高い。

今回はStitchのキャンバス上でデザインを作成しましたが、トークン消費が問題なければ、
コードベースを理解している普段のAIエージェントからStitchを操作した方が
良いかもしれません。Tasteのskillも使えるし。

## 使ってみた感想

デザインわからんエンジニアが、Figmaを使わずにサクッと動くものを作りたいときに
めっちゃ刺さると思う。

Stitchならskillsを使って、エディタやデスクトップアプリからremoteでデザインを
練ることも、そのまま実装することも可能。コードベースも参照できるので、
実装の柔軟性も高そうです。

個人的には、何か動くものを作るときにデザインがネックでモチベーションが上がらない
ことも多かったのですが、Stitchがあればそのハードルがかなり下がりそう。

その分、Figmaに比べれば雰囲気デザインではあります。ちゃんとしたものを最終的に
作りたいなら、最初からFigmaを使うか、Stitchで作成したデザインをFigmaへ
エクスポートして移行した方が良さそうです。

キャンバス上の画面が増えやすく、既存画面の編集方法や画面間の関連付けが
分かりづらい点は気になりました。まだBeta版なので、このあたりの使い勝手が
良くなるともっと良さそう。

あと`DESIGN.md` はかなり良い気がする。Figmaやデザインへの習熟が求められそうで、
デザインシステムにはハードルを感じていましたが、 `DESIGN.md` という形式で
抽出されると、リポジトリへ配置できてエンジニア的にも見やすいですし、コードの近くにある方が扱いやすそうです。

個人開発や、既存資産がない状態から最速で作りたいときに良いのかもしれない。

## まとめ

- Stitchで既存サイトのURLや画像からデザインを生成できた
- Stitch上に作成したデザインや実際の公開サイトURL、コードベースから`DESIGN.md` を抽出できた
- `DESIGN.md` を介してデザイン情報をAIエージェントへ渡せた
- MCPとskillsを使ってStitchとローカルの開発環境を往復できた
- Tasteを使うことで、AIが生成しがちな汎用的なデザインを調整できた
- Stitchで作成した新しい画面を高い再現度で実装できた
- キャンバス上での画面編集や画面同士の関連付けには分かりづらい部分があった

まだBeta版リリースですが、十分活用できる感じがします。とりあえず個人的には
かなり気に入ったので、今後、個人で何かデザインを作るときは積極的に使っていこうとい思います。

今回は以上です🐼
