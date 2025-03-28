---
title: "2025年版 開発環境見直し備忘録(執筆Clineに手伝ってもらった🙄)"
emoji: "🛠"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["skk", "ghostty", "raycast", "cline", "starship"]
published: true
---

## はじめのはじめ

この記事はAIエージェントを使った技術記事作成の練習として書いてます。全てではないですが~~8~~ 6割くらいCline(Roo Code)に書いてもらって手直ししたものを公開していると思います。記事の内容については実際に手元で試行錯誤した内容をまとめてもらったので嘘はない、はずです。まだ1記事目なのでもう少し知見が溜まったら改めて記事にまとめようと思ってます。

参考

https://zenn.dev/mizchi/articles/auto-mizchi-writer

https://kiririmode.hatenablog.jp/entry/20250303/1740985951

## はじめに

> エンジニアにとって開発環境は「第二の家」とも言える大切な場所です。1日の大半を過ごすこの環境が快適であればあるほど、作業効率は上がり、ストレスは減り、結果として良いコードが書けるようになります。理想的な開発環境は一朝一夕に構築できるものではなく、日々の小さな改善の積み重ねによって、少しずつ自分に最適化された環境が出来上がっていくものです。（AIが生成した温かみある記事の導入)

モチベーションは忘れてしまったのだけど日本語入力をいい加減MacデフォルトのIMEから変えたくて、いろいろ調べてたらSKKというものを知って、そういえばターミナルもghostty良さそうだったなとか自分のローカルの開発環境一旦全部整理してどこかにまとめておきたいなと思ったのがきっかけだったと思う。

本記事では、実際に使用している開発環境のセットアップとカスタマイズについて紹介します。具体的には以下のツールと設定に焦点を当てます：

- **ghostty**: 高速で機能的なモダンなターミナルエミュレータ
- **シェル環境の最適化**: zshの設定やstarshipによるプロンプトのカスタマイズ
- **SKK**: 効率的な日本語入力を実現するIME
- **Raycast**: キーボード操作を中心としたランチャーアプリ
- **Git設定**: 日常的なGit操作を効率化するための設定

なお、本記事で紹介する設定やツールはmacOSを前提としていますが、多くの考え方や工夫はLinuxなど他の環境でも応用可能です。

それでは、具体的なツールと設定の紹介に移っていきます。

## モダンなターミナル環境の構築 - ghostty導入

https://ghostty.org/

特に不満はなかったけどkittyから乗り換えたいと思い、ghosttyを導入しました。

参考

https://blog-dry.com/entry/2024/12/27/162410#:~:text=theme%20%3D%20catppuccin%2Dmocha%0Awindow%2Dpadding%2Dx%20%3D%2020%0Awindow%2Dpadding%2Dy,0.85%0Abackground%2Dblur%2Dradius%20%3D%2020%0Amacos%2Dtitlebar%2Dstyle%20%3D%20transparent

https://engineering.konso.me/articles/iterm2-to-ghostty/



### configファイルの作成

ghosttyの設定ファイルは以下のコマンドで作成します：

```
mkdir ~/.config/ghostty
touch ~/.config/ghostty/config
```

### テーマ設定

テーマについては、cattppuccin-mochaを使っている人が多そうだったけど、久しぶりにicebergを使ってみたくなったので、しばらくそれを使っていました。`ghostty +list-themes`コマンドでテーマ一覧を見ることができ、非常に見やすいです。

ただ、Nordというテーマが気になっていたのを思い出したので最終的にNordに変更。

```
# theme = cattppuccin-mocha
# theme = iceberg-dark
theme = nord
```

### ウィンドウ設定

ウィンドウのパディング設定やテーマ設定も行いました。window-themeは設定しなくても良さそうですが、背景の透明度（opacity）とぼかし（blur）は参考ブログの設定値をそのまま使用しています。以前はblur設定をしていなかったのですが、「terminal開きながら後ろの画面見ることもないな」と思ったので有効にしました。タイトルバーについても特にこだわりはなく、参考ブログの値をそのまま使用しています。

```
window-padding-x = 20
window-padding-y = 5
window-padding-balance = true
# window-theme = ghostty

background-opacity = 0.85
background-blur-radius = 20

# macos-titlebar-style = transparent
```

### フォント設定

リガチャ（合字）を無効にする設定が必要なようでこちらも設定。

```
# リガチャを無効にする
font-feature = "-dlig"
```

### 全画面設定

Ctl2回で全画面透過表示しないと開発できない呪いにかかっているので、全画面設定も行いました。最初は`fullscreen`だけで設定しようとしたのですが、切り替えのアニメーションがとても我慢できるものではなかったので探していたら`macos-non-native-fullscreen`という設定を見つけてこちらを設定しています。この設定はmacOSのネイティブスクリーンを使いたい場合は設定できないようで、タイトルバーが表示されなくなるなどの特徴があります。

```
# 全画面表示
fullscreen = "true"
macos-non-native-fullscreen = "true"
```

全画面運用の場合はタイトルバーが見えないため、タイトルバーの設定自体が不要かもしれないと考えています。

### 起動方法

起動方法については、Quick Terminalという機能があるようですが、タブが使えないという制約があるため、Raycastで設定しました。ただし、結局どちらにしてもタブが使えなくなったので、Quick Terminalでも良かったかもしれません。

### SKK対応

後述のAquaSKKとの相性問題があったため、以下の設定を追加しました：

```
# AquaSKK対応
keybind = ctrl+j=ignore
```

### 最終的な設定ファイル

以上の設定をまとめた最終的な設定ファイルは以下のようになります：

```
# theme = cattppuccin-mocha
# theme = iceberg-dark
theme = nord

window-padding-x = 20
window-padding-y = 5
window-padding-balance = true
# window-theme = ghostty

background-opacity = 0.85
background-blur-radius = 20

# macos-titlebar-style = transparent

# リガチャを無効にする
font-feature = "-dlig"

# 全画面表示
fullscreen = "true"
macos-non-native-fullscreen = "true"

# AquaSKK対応
keybind = ctrl+j=ignore
```

## シェル環境の最適化

いろいろ開発環境見直してる流れでshell環境も見直そうと思い立ったので。

### シェルの選択

最初はzshのプラグインとかRust製のCLIツールとか入れるくらいに考えていたのですが、fishやnushellといった別のシェルも検討しました。

fishについては以下の記事が参考になり、ターミナルでの補完が効いているのを見て良いと思いました。fisherというパッケージマネージャーでfzfなどの有名なツールも導入でき、設定を作り込まなくてもすぐに導入できる点が魅力的でした。

https://qiita.com/hennin/items/33758226a0de8c963ddf

nushellについては以下の記事を参考にしましたが、shell環境が別物になりそうな気がしたのでやめました。

https://zenn.dev/minedia/articles/6a8321fc6504ec

fishいいなになったんですが、1点だけ気になったのがPOSIX非互換という点で、shell scriptがfishだと動かないなどの問題があると面倒だと考えました。実際にfishからzshに戻したという記事も多く見かけました。

最終的に、mizchiさんの以下のスクラップを見つけ、補完機能だけが欲しいならzshのままでも実現できることを知り、zshを使い続けることにしました。

https://zenn.dev/mizchi/scraps/8969fe29a27e21

### プラグイン管理ツール - sheldon

shellをfishではなくzshのままにすることにしたので、プラグインを導入するためにsheldonを使うことにしました。sheldonは「Fast, configurable, shell plugin manager」（高速で設定可能なシェルプラグインマネージャー）です。

https://github.com/rossmacarthur/sheldon

インストールと設定方法は以下の通りです：

```bash
# インストール
brew install sheldon

# 初期化（zsh用）
sheldon init --shell zsh

# .zshrcに追加する内容
eval "$(sheldon source)"
```

### シンタックスハイライト - F-Sy-H

zshのシンタックスハイライトするために、F-Sy-H（Feature-rich Syntax Highlighting for Zsh）を導入しました。

https://github.com/z-shell/F-Sy-H

似たようなプラグインに[fast-syntax-highlighting](https://github.com/zdharma/fast-syntax-highlighting)や[fast-syntax-highlighting continuum](https://github.com/zdharma-continuum/fast-syntax-highlighting)などがあってどれを使うのがいいのかよくわからなかったんですが[wiki](https://wiki.zshell.dev/ja/ecosystem/plugins/f-sy-h)っぽいところに記載があったのでこのプラグインを選びました。

```toml:~/.config/sheldon/plugins.toml
[plugins.F-Sy-H]
github = "z-shell/F-Sy-H"
```

zshellってなんか違うやつっぽいな、と思いましたが、まあ使えてるからいいかという感じです。

### 自動補完 - zsh-autosuggestions

fish風の自動補完機能を提供するzsh-autosuggestionsも導入しました。最初はAmazon Q Developer CLIのinline suggestionsを使おうと思ったのですが、ターミナルの背景色とサジェストされる文字の色が近くて見えにくかったため、代わりにzsh-autosuggestionsを使うことにしました。

https://github.com/zsh-users/zsh-autosuggestions

環境変数を使ってサジェストされる文字のスタイルを変更し、見やすくしています：

```bash:~/.zshrc
ZSH_AUTOSUGGEST_HIGHLIGHT_STYLE="fg=#767676"
```

### ディレクトリ移動支援 - zsh-z

最近訪れたディレクトリにすばやくジャンプできるzsh-zも導入しました：

https://github.com/agkozak/zsh-z

```toml:~/.config/sheldon/plugins.toml
[plugins.zsh-z]
github = "agkozak/zsh-z"
```

このプラグイン入れてみたものの使いこなせる自信は今のところなさそう

### その他の便利なツール

せっかくなので有名どころのツールも導入しました：

#### fzf（ファジーファインダー）

```bash
brew install fzf
```

参考

https://zenn.dev/nokogiri/articles/ec99e40df54555

#### bat（catコマンドの代替）

https://github.com/sharkdp/bat

```bash
brew install bat
alias cat="bat"
```

#### eza（lsコマンドの代替）

exaからezaに名前が変わったようです：

https://github.com/eza-community/eza

```bash
brew install eza
alias ls="eza"
alias la="eza -a --git -g -h --oneline --icons"
alias ll="eza -al --git --icons --time-style relative"
```

### プロンプトのカスタマイズ - Starship

fish調べているときに知ったStarshipも導入しました。導入も簡単そうだったので試してみることにしました：

https://starship.rs/ja-JP/

```bash
brew install starship
```

`.zshrc`に以下を追加します：

```bash
eval "$(starship init zsh)"
```

デフォルトでもいいですが、少し情報が多い気がするので設定ファイルを作成しました：

```bash
touch ~/.config/starship.toml
```

設定ファイルの内容は以下の通りです：

```toml
# エディターの補完を設定スキーマに合わせて取得
"$schema" = 'https://starship.rs/config-schema.json'

# シェルのプロンプトの間に空行を挿入する
add_newline = true

# ref https://github.com/starship/starship/discussions/1107?sort=top#discussioncomment-10214050
format = """
[╭{owo} ](bold green)$username$directory$battery$all$line_break$character"""

[character]
success_symbol = '[╰─>](bold green)'
error_symbol = '[x >](bold red)'

[git_branch]
format = '[$symbol$branch(:$remote_branch) ]($style)'

# ref https://zenn.dev/link/comments/ced6da507bf16f
[git_status]
format = '$all_status$ahead_behind '
conflicted = ''
ahead = ''
behind = ''
diverged = ''
up_to_date = '[✓](bold green)'
untracked = ''
stashed = ''
modified = '🔥'
staged = ''
renamed = ''
deleted = ''

[time]
disabled = false
format = '🕙 [$time]($style) '
time_format = '%m/%d %R'

[aws]
disabled = true

[gcloud]
disabled = true
```

starshipのGitHub repositoryでconfigを紹介してくれていたので適当によさそうなのを参考にしました。gitのstatusは表示するのが面倒くさかったのでmizchiさんの設定を参考にしました。AWSとGCPはデフォルトだと少し情報が多すぎるのでdisabledにしました。プロファイル情報は表示されていた方が嬉しい気もしますが、現在CLIでクラウド操作をしていないため、必要になったら再検討します。

https://github.com/starship/starship/discussions/1107

完成したterminalはこちら

![](https://storage.googleapis.com/zenn-user-upload/e1bcfa356972-20250328.png)

![](https://storage.googleapis.com/zenn-user-upload/7911adb04682-20250328.png)

## 効率的な日本語入力 - SKKの導入

### SKKとの出会いと導入の動機

Mac標準の日本語IMEにイラつくことが多くなってきたのがSKKを導入するきっかけでした。Google日本語入力を知ってそれでもいいのですが、SKKの存在を知り、Vimとの相性が良さそうだったこと（Vimmerではないですが）や、ユーザー辞書登録が非常に簡単そうだったことに惹かれました。また、正直なところ「なんか使ってるとかっこよさそう」という理由もありました。

SKKを使う主な目的は、日本語と英字を簡単に切り替えることです。ただし、SKK自体のカスタマイズはなるべく避けたいと思っていました（使う前に心折れそうなため）。コードを書くときだけでなく、ブラウザなど他の場所でも使うことを想定していました。また、Spaceではなく「;」で変換する設定もしたいと考えていました。

詳しくは以下の導入スクラップをご参照ください。

https://zenn.dev/jy8752/scraps/c0915fbde2711f

### SKKのインストールと基本設定

MacでSKKを使うには、AquaSKKをインストールします。以下の記事が参考になりました。

https://zenn.dev/happy663/articles/f120814ca16adf

キーバインドの設定も行いました。普通にインストールしたSKKをキーボード設定に追加しましたが、普段US配列のキーボードを使っているため言語切替はCtrl + Spaceで行っていました。しかし、入力中にCtrl + Spaceを押してもうまく切り替わらなかったため、Karabinerを使って設定することにしました。といっても、日本語向け設定をインストールして、Cmdキーで文字切替ができるように設定するだけの簡単な作業です。

Spaceを「;」に変更する設定は、confファイルに追記するだけで可能です。

### SKK Serverの導入

SKKではSKK Serverというものを起動して使うことができます。以下の記事でyaskksrv2の導入について紹介されていました。

https://zenn.dev/mirko_san/articles/2df537bfaef166

今からSKKを始めるなら何が良いのか迷いましたが、Rust製で作り直されたyaskksrv2を使い始めるのが良さそうな気もしました。ただ、起動までの手順が多くて少し面倒だなと思っていたところ、Ruby製のgoogle-ime-skkというgemがあることを知りました。こちらの方が手軽に始められそうだったので、使ってみることにしました。10年以上前に開発されたようでメンテナンスもされていないようですが、SKKを使い続けるかどうかもわからないので、とりあえず動けばいいかなという気持ちです。もし動かなかったり問題があれば、素直にyaskksrv2に切り替えようと思っていますが、普通に問題なく動きます。

### google-ime-skkのインストールと自動起動設定

https://blog.sushi.money/entry/20110421/1303274561

google-ime-skkのインストールは以下のコマンドで行いました：

```
ruby -v
ruby 3.4.2 (2025-02-15 revision d2930f8e7a) +PRISM [arm64-darwin23]

gem install google-ime-skk
Fetching google-ime-skk-1.4.0.gem
Successfully installed google-ime-skk-1.4.0
1 gem installed

google-ime-skk
```

ただ、最新のRubyのバージョンでは動作しなかったため、バージョンを下げて試したところうまくいきました(あんまり深く調べてないので上手くいかなかったらバージョンを下げて試してみてください。)

```
ruby -v
ruby 2.6.10p210 (2022-04-12 revision 67958) [arm64-darwin23]
```

正直なところ、SKKサーバーを起動してGoogle日本語入力と連携して雑に変換できるようにしないとSKKは使いづらいと感じています。逆に言えば、Google日本語入力があることで、ユーザー辞書登録を頻繁にしなくても入力できるようになりました。これがないと毎回辞書登録が必要になって使い物にならないと個人的には思いました。

また、毎回手動で起動するのも面倒なので、自動でバックグラウンド実行する設定も行いました。いろいろな方法がありそうですが、macのAutomatorでシェルスクリプトを実行するのが一番簡単そうだったのでそれを採用しました。以下の記事を参考。

https://qiita.com/kagerou_ts/items/2606703e70c5eb18fb37

Automatorを開き、シェルスクリプトを実行するアクションで以下のスクリプトを記述します：

```bash
export PATH=$PATH:$HOME/.rbenv/shims
google-ime-skk
```

Automatorでは実行環境を引き継がないため、コマンドのPATHが見つからないというエラーが発生します。そのため、最初にPATHを通す必要があります。Automator自体はバックグラウンドで実行されるので、通常通り起動するだけで大丈夫です。

このワークフローをアプリケーションとして保存し、ログイン時の起動アプリとして登録しておきます。これで、Mac起動時に自動的にSKK Serverが起動するようになります。

一旦この設定で様子を見て、google-ime-skkが動かなくなったり、yaskksrv2の方が速いと感じたら切り替えることも検討しています。

### 絵文字対応

AquaSKKは辞書を追加することができ、絵文字辞書を公開してくれてる人がいたりするので追加することで絵文字変換もある程度対応できる。

https://github.com/uasi/skk-emoji-jisyo

```bash
cd ~/Library/Application\ Support/AquaSKK
curl -O https://raw.githubusercontent.com/uasi/skk-emoji-jisyo/master/SKK-JISYO.emoji.utf8
```

https://github.com/ymrl/SKK-JISYO.emoji-ja

```bash
cd ~/Library/Application\ Support/AquaSKK
curl -O https://raw.githubusercontent.com/ymrl/SKK-JISYO.emoji-ja/master/SKK-JISYO.emoji-ja.utf8
```

辞書を手元に持ってこれたら以下の画像のようにAquaSKKの設定の辞書追加で辞書のパスを指定して追加すれば絵文字辞書を追加できる。

![](https://storage.googleapis.com/zenn-user-upload/f7c2f0fb4588-20250328.png)

😄(SKKのかなモードの時に/を押して```smile```とか打てば絵文字になる)

### カスタムルールの追加

カスタムルールについては情報が少なかったです。

とりあえず、かなモードのときに？と！が全角でだせないのと```-```をかなモードから出せないのを解決したい。調べたところ、```~/Library/Application Support/AquaSKK```配下に```.rule```拡張子のファイルを配置することで設定できそうでした。

```
###
### custom-symbols.rule
###

!,！,！,!
?,？,？,?
#~,?,?,~
(,（,（,(
),）,）,)

z-,-,-,-
```

これで以下の画像のようにAquaSKKの設定から作成したcustom-ruleを追加することでかなモードから全角の！や？が入力できるのと```z-```と入力することで半角のハイフンが入力できるようになりました。

![](https://storage.googleapis.com/zenn-user-upload/baa076c67626-20250328.png)

### Ctl + J 問題

これは予想してなかったのですがSKKのかなモードへの切替は```Ctl + J```なのですが、Terminal上でかなモードへの切替を行うと改行も実行されてしまうようになってしまいました。移行前のkittyでは起きなかったのですがghosttyではCtr + Jが改行にバインドされてしまうようです。そのため、ghostty側で以下の設定をすることで回避するようにしました。

```:~/.config/ghostty/config
# AquaSKK対応
keybind = ctrl+j=ignore
```

### Terminal上での L 問題

これも予想外だったのですがTerminal上で英数モードへの切替である```l```を入力するともれなく```l```も入力されてしまうのです。これはしょうが無いのでAquaAKK側の設定で英数モードへの切り替えのトリガーを以下のように設定しました。

```
SwitchToAscii           l||ctrl::hex::0x20
```

これは英数モードへの切り替えを```l```に加え、```Ctl + Space```で切り替えられるようにしています。もともとUSキーボードで```Ctl + Space```でIME切り替えを行っていたのでそうしましたが他のキーバインドでも問題ないです。

最終的なkeymapは以下のようなかんじです。

```:~/Library/Application Support/AquaSKK/keymap.conf
###
### keymap.conf
###

# ======================================================================
# event section
# ======================================================================

SKK_JMODE       ctrl::j
SKK_ENTER       group::hex::0x03,0x0a,0x0d||ctrl::m
SKK_CANCEL      ctrl::g||hex::0x1b
SKK_BACKSPACE       hex::0x08||ctrl::h
SKK_DELETE      hex::0x7f||ctrl::d
SKK_TAB         hex::0x09||ctrl::i
SKK_PASTE       ctrl::y
SKK_LEFT        hex::0x1c||ctrl::b||keycode::7b
SKK_RIGHT       hex::0x1d||ctrl::f||keycode::7c
SKK_UP          hex::0x1e||ctrl::a||keycode::7e
SKK_DOWN        hex::0x1f||ctrl::e||keycode::7d
SKK_PING        ctrl::l
SKK_UNDO                ctrl::/

# ======================================================================
# attribute section(for SKK_CHAR)
# ======================================================================

ToggleKana      q
ToggleJisx0201Kana  ctrl::q
# SwitchToAscii     l
SwitchToAscii       l||ctrl::hex::0x20
SwitchToJisx0208Latin   L

EnterAbbrev     /
EnterJapanese       Q
NextCompletion      .
PrevCompletion      ,
NextCandidate       hex::0x20||ctrl::n
PrevCandidate       x||ctrl::p
RemoveTrigger       X

UpperCases      group::A-K,M-P,R-Z
Direct          group::keycode::0x41,0x43,0x45,0x4b,0x4e,0x51-0x59,0x5b,0x5c,0x5f
InputChars              group::hex::0x20-0x7e

CompConversion      alt::hex::0x20||shift::hex::0x20

# ======================================================================
# handle option
# ======================================================================

AlwaysHandled           group::keycode::0x66,0x68
PseudoHandled           ctrl::l

StickyKey ;
```

## ランチャーアプリによる作業効率化 - Raycast

いい加減ちゃんと使っていきたいのでいろいろ見直しました。結果的に最高に便利なのとAI機能が気になって課金もしましたが正直使いこなせなさそうです。

### 便利な機能と設定

ざっと調べて使えそうな機能を以下にまとめました。

#### 基本機能

- **Floating Note / Raycast Notes**：ちょっとしたメモを取るのに便利です。ObsidianがVimで書けるただのノートとして使いこなせていない感があったので、これくらいがちょうどよいと感じています。バージョンアップでRaycast Notesに名前が変わりました。

- **Clipboard History**：これまであまり使えていませんでしたが、ちゃんと使っていきたいと思っています。コピーした内容の履歴を管理できるので非常に便利です。

- **絵文字機能**：絵文字を出すのが苦手なので、この機能を使って効率的に絵文字を入力できるようにしたいです。SKKのところで絵文字辞書追加しましたがRaycastうまく使えれば絵文字はこっちで対応できるかもなとか思ってます。

- **Window Management**：これまでは別のアプリを使っていましたが、アプリを減らしたいのでRaycastの機能を使うことにしました。

- **ちょっとした計算**：Raycastを開いて検索窓に適当に計算式を入れると計算してくれます。remとpxの変換などもできるので便利です。これまでは毎回AIに聞いていました。

#### スニペット機能

create snipetでスニペットが作成できます。よく使う文章などを登録しておくと便利です。例えば、SQLで毎回RLS(set_config)のクエリをコピペして貼り付けて使ってたのですがスニペットにしておくと便利そうだなとか思ってます。

#### 拡張機能

- **ray.so**：よく見るいい感じにコードなどの画像を生成してくれるツールです。便利なので導入しました。

- **MyIP**：自分のIPを確認できる機能です。たまに調べることがあったので導入しました。

- **Slack(Set Status)**：これまでSlackのステータス更新をあまりしていませんでしたが、リモートワーク環境では状況共有が重要なので使うようにしました。

- **cURL**：Raycastからcurlコマンドを実行できます。地味に便利そう。

- **Audio Device**：イヤホンの切り替えをよく行うので導入しました。

- **Typing Practice**：タイピング練習ができる機能だそうです。たぶん使わないと思いますが。

#### AI機能（課金）

Raycastは課金することで多くのAI拡張機能を使うことができます。基本的には以下のような機能があります：

- **Quick AI**：Raycastを開いてTabを押すと使えます。雑にAIに聞きたいときに便利です。

- **AI Chat**：Quick AIでCmd + Kを押しても使えます。ChatGPTのようにAIとチャットできます。

- **AI extentions**：様々な拡張機能とAIを連携できますが、まだ良い使い道が思いついていません。

- **AI Command**：独自でプロンプトを作成してRaycastから呼び出せます。

正直なところ、無料の範囲だけでもRaycastは十分に機能しますが、試しに課金してみました。Quick LinkでTranslatorを呼び出せばDeepLの代わりになりますし、Quick AIやサイトの要約機能などが便利そうです。しばらく使ってみて便利だと感じれば継続し、そうでなければ解約を検討します。

```
// Quick Linkで以下を設定することで選択箇所を翻訳できるようになるのでDeep LのChrome拡張がいらなくなる
raycast://extensions/raycast/translator/translate?fallbackText={selection}
```

### ホットキー設定

ホットキー設定は以下のように行いました：

- Clipboard History: Option + v
- Raycast Notes: Option + n
- emoji: Option + e
- volume up: Option + ↑
- volume down: Option + ↓
- Output Device: Option + i
- Arc: Option + a
- Cursor: Option + c
- Slack: Option + s
- 翻訳: Option + t

ざっと調べた感じ```Option```キーをホットキーに使っている人が多かったので。正直これ以上は覚えられる気がしないので他の機能が使いたくなったら素直にRaycast呼び出しから検索して使おうと思います。

## Gitワークフローの効率化

mizchiさんのXのポストで気になったので、Gitのconfigをちゃんと設定することにした。

https://x.com/mizchi/status/1896790925822861502

元記事はこちら

https://blog.gitbutler.com/how-git-core-devs-configure-git/

### Git configの設定

もともとあった設定に追加して、最終的に以下のような設定になりました：

```toml
[alias]
  # いい感じのグラフでログを表示
  graph = log --graph --date=short --decorate=short --pretty=format:'%Cgreen%h %Creset%cd %Cblue%cn %Cred%d %Creset%s'
  # 上の省略形
  gr = log --graph --date=short --decorate=short --pretty=format:'%Cgreen%h %Creset%cd %Cblue%cn %Cred%d %Creset%s'
  st = status
  cm = commit
  # Untracked filesを表示せず，not stagedと，stagedだけの状態を出力する
  stt = status -uno
  # 行ごとの差分じゃなくて，単語レベルでの差分を色付きで表示する
  difff = diff --word-diff
  # ref https://qiita.com/mizchi/items/dcdc57f748a1d6cc3648
  delete-merged-branches = !git branch --merged | grep -v \\* | xargs -I % git branch -d %
[column]
  ui = auto
[branch]
  sort = -committerdate
[tag]
  sort = version:refname
[init]
  defaultBranch = main
  templatedir = ~/.git_template
[diff]
  algorithm = histogram
  colorMoved = plain
  mnemonicPrefix = true
  renames = true
[push]
  default = simple
  autoSetupRemote = true
  followTags = true
[fetch]
  prune = true
  pruneTags = true
  all = true
[pull]
  rebase = true
[rerere]
  enabled = true
  autoupdate = true
[rebase]
  autoSquash = true
  autoStash = true
  updateRefs = true
[merge]
  conflictstyle = zdiff3 
```

### Git管理ツール

以下の記事にあるようなgit関連のツールについてもついでに検討することにしました。

https://zenn.dev/mozumasu/articles/mozumasu-lazy-git

#### GitHub CLI (gh)

ghはすでに使っていますが、あまり使っていないので機会があったら積極的に使っていきたいと思っています。

#### ghq（リポジトリ管理ツール）

ghqは認識していましたが、あまり必要性を感じていませんでした。しかし、試しに導入してみました：

```bash
brew install ghq
```

試しにプロフィールリポジトリをクローンしてみたところ、`~/ghq/github.com/JY8752/JY8752`にクローンされました。クローン先を変えたければ設定すればできそうです。これでどこにクローンするかを考えずにとりあえず`ghq get`しておけばよさそうです。

また、Ctrl + gで`ghq get`してきたリポジトリの一覧から選択してそのプロジェクトルートに移動できるようにしました。

#### git-cz / cz-git（コミットメッセージ作成支援ツール）

個人的にはあまり使っていませんが、仕事で使うことがあります。ちなみに、cz-gitというより軽量化されたものがあるらしいので、そちらを使うと良さそうです。

どちらかというと、cz-gitのai commitの機能が一番気になりました。

#### AIによるコミットメッセージ生成

aiによるコミットメッセージの自動生成が気になっています。もともと、英語もそんなに得意ではないし、日本語だとしてもコミットメッセージを考えるのは苦手だったので、ここは早々にAIにやってもらいたいと思っていました。

cz-git以外にもaicommits、OpenCommit、auto-commitといったツールが存在するようです。いずれも、OpenAIのAPIキーを設定して使う感じで、完全無料では使えなさそうです。とはいえ、大した額ではないのでケチるところではないのかもしれません。

https://zenn.dev/hayato94087/articles/8193b7f7fd6f76

ただ、気づいたらCursorにもコミットメッセージの自動生成機能は実装されていて、生成されたメッセージに満足できるかというと微妙ですが、それはそもそもコミットする変更の粒度などの問題が大きいにあると思っているのでCursorのせいではないかもしれません。

とりあえず、Cursor課金しているし、頻繁にコミットするような開発スタイルではない（よくない）ので、そこまで必須ではないというか、そこまで生産性的に変わらないだろうと思ったので、とりあえずCursorの自動コミットを使ってみることにしました。

#### lazy-git（TUIベースのGitクライアント）

悩みましたが、あまり新しく覚えることは多くしたくなかったので導入はやめました。基本的なGit操作はCLIでやることがほとんどですが、実行する操作は決まった操作が多いですし、zshの補完なども設定したのであまり困らなさそうです。実際、今のところ困っていません。

#### git-now

https://qiita.com/k_tada/items/4b7ae126e6c5df2cb33c

上記の記事を見て、昔頑張ってgit nowでコミットを積んで最後にgit rebase -iするみたいな運用をしてみようとしたことがありますが、早々に心折れたのでやめました。

コミットを綺麗にまとめるにはいい方法だと思うのですが、あちこち修正していると1コミットがまあまあの大きさになることが多いので、あまり自分には合っていない気がしています。小さく、細かくコミットを積んでいくスタイルには相性が良いのだろうと思います。

## そのほか見直したツール

- Docker Desktop for Mac -> OrbStack
- Google Chrome -> Arc
- CleanShot X 課金(スクショとgif作成が捗る)

## まとめ

- terminalをghosttyにしたら簡単にいい感じのterminalになった
- sheldon, starshipを使ってzshのままでもfishみたいないい感じのshell環境が手に入った
- AquaSKKを入れたことで日本語入力が快適になったかもしれない（速くはなっていない。むしろ遅くなった。でも満足はしている。）
- git環境を見直した。lazy-gitはいつかリベンジしたい。
- Raycastはもっとちゃんと使っておけばよかった。(とても便利)

一通り開発環境の見直しができました。本当はdotfilesとかまでできると再現性があって一番よいのでしょうがメンテし続けられる気がしなかったのと、それによってストレス増えそうだったのでNotionに開発環境まとめておくくらいに留めました。

しばらくはこの環境でやってみようかと思います。
今回は以上です🐼

## おわりのおわり

なんとかAIを使って1本記事を書くことができました。ただ、この記事を執筆するのに$42吹っ飛びました。これは明らかにAIへの指示が慣れておらず下手だったことと、コンテキストと編集内容がでかすぎてエラーを吐いて何度も作ったり消したりみたいな無駄な作業を繰り返してしまったのが原因のような気がしてます。

1記事10$くらいで1時間くらいで8割くらいの完成度の記事を作ってくれるならありかなとも思いますが今のままだと自分で書いたほうが早いし、無駄にお金がかかることはないなといった感想です。

もうすこしClineを使って記事を書いてみようかなと思います。

ただ、このAIエージェントの使い方には可能性は感じていて使い物になるようになれば企業のテックブログ運用が抱える問題(誰が書くの問題とかネタがないよね問題とか)とかの解決につながるんじゃないかなとか思ったりしてます。