---
title: "noteの執筆体験をzennに近づけるためにGoでCLIツールを開発した話~はじめてのOSS~"
emoji: "📓"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: ["Go", "CLI", "headless", "rod"]
published: true
---

noteで記事を執筆、管理するためのCLIツール[note-cli](https://github.com/JY8752/note-cli)をGoで開発しました。本記事ではnote-cliの説明および初めてのOSS開発となったためそこで得た知見というか感想的なものを備忘録的に紹介できればと思います。

## 対象読者

- GoでCLIツールを作ることに興味がある方
- OSS開発をまだしたことがないが自作のOSSを公開することに興味がある方
- noteで記事を書いているエンジニアの方
- Goでヘッドレスブラウザを使用することに興味がある方(画像生成に使用してます)

## なぜ作ったか

2023年4月に第一子が産まれ、初めての育児は大変ながらもだいぶ慣れてきました。そこで[note](https://note.com/)で育児記事を投稿したいなーと思うようになったもののzennの執筆体験が良過ぎてnoteの執筆体験もできるだけzennに近づけたいと思ったためです。Qiitaでも記事を書くことがありますが[Qiita CLI](https://github.com/increments/qiita-cli)がリリースされたので技術記事は全てローカルの環境で執筆、バージョン管理ができるようにもなったのでnoteでも同じようにしたいというのがモチベーションです。

## 開発する機能

今回開発するCLIに最低限求めた機能は以下の通りです。

- 記事を執筆するためのmarkdownファイルをコマンドで作成する。
- ファイル名は自動で設定したい。
- noteで記事を投稿する際に設定する画像をコマンドで生成したい。

## Cobraを使用してCLIツールとしてプログラムを動かす

今回はCLIツールの作成にGoを使用しているため、[Cobra](https://github.com/spf13/cobra)というライブラリを使用して開発を行いました。基本的には以下の記事を全面的に参考にさせていただきました🙇

https://zenn.dev/kou_pg_0131/articles/go-cli-packages

Cobraの詳細な使い方はこちらの記事をご参照ください。

### setup

```
mkdir note-cli
cd note-cli
go mod init github.com/JY8752/note-cli
```

#### Cobra

```
go get -u github.com/spf13/cobra@latest
```

#### cobra cli

Cobraには開発ツールとしてCLIも用意されているようなのでこちらもinstall。

```
go install github.com/spf13/cobra-cli@latest
```

以下のコマンドで雛形を作成。

```
cobra-cli init
```

```
tree .
.
├── LICENSE
├── cmd
│   └── root.go
├── go.mod
├── go.sum
└── main.go
```

## 記事ファイルを作成する

想定するコマンドは以下のような感じで実装を始めました。

```
note-cli create article
```

```article```をサブコマンドにしたのはこの後実装予定の画像生成のコマンドを```create image```みたいにしたかったからです。

このコマンドを実施することで記事を書くためのmarkdownファイルを作成できればいいのですが、フラグの指定でファイル名をユニークなランダム値だけでなく日付や指定の名前で作成をしたいと思います。

### create コマンド

とりあえず、cobra-cliでコマンドを追加します。

```
cobra-cli add create
```

追加されたファイルを以下のように修正します。

```go:cmd/create.go
package cmd

import (
	"errors"

	"github.com/spf13/cobra"
)

var createCmd = &cobra.Command{
	Use:   "create",
	Short: "Create a new article directory.",
	Long: `
Create a new article directory.
If you want to specify directory and file names, specify them as --name(-n) options.
You can also specify the -t(--time) option to make the current timestamp the file name.
If nothing is specified, the file is created with a unique file name by UUID.
	`,
	RunE: func(cmd *cobra.Command, args []string) error { return errors.New("not found command") },
}

func init() {
	rootCmd.AddCommand(createCmd)
}
```

コマンドの説明と```init()```でrootコマンドへのコマンド登録をしてます。```*cobra.Command```の```RunE```にはコマンドを実行したときの処理を書きますが、今回はこの```create```コマンド単体で実行されることは想定していないのでエラーを返すだけにしています。

### article コマンド

次に、```create```コマンドのサブコマンドとして```article```コマンドを追加します。

```
cobra-cli add article
```

追加されたファイルを以下のように修正します。

```go:cmd/article.go
package cmd

import (
	"github.com/JY8752/note-cli/internal/run"
	"github.com/spf13/cobra"
)

var articleCmd = &cobra.Command{
	Use:   "article",
	Short: "Create a new article directory.",
	Long: `
Create a new article directory.
If you want to specify directory and file names, specify them as --name(-n) options.
You can also specify the -t(--time) option to make the current timestamp the file name.
If nothing is specified, the file is created with a unique file name by UUID.
	`,
	Args: cobra.NoArgs,
	RunE: run.CreateArticleFunc(&timeFlag, &name),
	Example: `note-cli create article
note-cli create article --name article-a
note-cli create article -t`,
}

var (
	timeFlag bool
	name     string
)

func init() {
	articleCmd.Flags().BoolVarP(&timeFlag, "time", "t", false, "Create directory and file names with the current timestamp")
	articleCmd.Flags().StringVarP(&name, "name", "n", "", "Create a directory with the specified name")

	articleCmd.MarkFlagsMutuallyExclusive("time", "name")

	createCmd.AddCommand(articleCmd)
}
```

このコマンドはフラグの指定でファイル名を変更できるようにしたいですが、引数は不要のため```*cobra.Command.Args```には```cobra.NoArgs```を指定しています。また、今回はフラグとして```--time(-t)```と```--name(-n)```というフラグを追加しています。この二つのフラグはどちらかひとつだけ指定できるようにしたかったので```*cobra.Command.MarkFlagsMutuallyExclusive()```を使用してフラグをグループ化してどちらか片方した指定できなくしています。

肝心のコマンドを実行したときの処理は別のパッケージに切り出しています。関数の引数にフラグ変数を渡していますが、初期化のタイミングでそのまま渡してしまうとフラグ値の更新が反映されないのでポインタとして渡しています。

切り出した関数は以下のような感じで```*cobra.Command.RunE```に登録できるような形式の関数を返すようにします。引数のフラグはポインタ型なので実際の値を変数に格納しなおしています。

```go
func CreateArticleFunc(timeFlag *bool, name *string, options ...Option) RunEFunc {
	return func(cmd *cobra.Command, args []string) error {
		t := *timeFlag
		n := *name
    
    ...
  }
}
```

また、後述の画像生成のコマンドを実装する際にconfigファイルのようなものを合わせて配置したかったため作成するのは記事ファイルだけでなくディレクトリとconfigファイルの作成も行うようにしました。イメージは以下のような感じです。

```
.
├── 2023-09-12
│   ├── 2023-09-12.md
│   ├── config.yaml
```

このディレクトリ名とファイル名を指定されたフラグに応じて変えるような実装にしました。

```go
		// create directory name
		var dirName string

		// set timestamp in directory name
		if t {
			dirName = clock.Now().Format("2006-01-02")

			counter := 1
			for {
				if !file.Exist(filepath.Join(op.BasePath, dirName)) {
					break
				}
				counter++
				dirName = clock.Now().Format("2006-01-02") + "-" + strconv.Itoa(counter)
			}
		}

		// set specify directory name
		if n != "" {
			dirName = n
		}

		// random value since nothing was specified
		if !t && n == "" {
			if op.DefaultDirName != "" {
				dirName = op.DefaultDirName
			} else {
				dirName = uuid.NewString()
			}
		}
```

作成するベースのパスとフラグ指定がなかった時のデフォルトのディレクトリ名の値はテストしやすいようにオプションとして指定できるように実装しました。

```go
type RunEFunc func(cmd *cobra.Command, args []string) error

type Options struct {
	BasePath       string
	DefaultDirName string
}

type Option func(*Options)

...

		// set option
		var op Options
		for _, option := range options {
			option(&op)
		
```

あとは決定したディレクトリ名でディレクトリとファイルを作成するだけです。完成した関数は以下のようになりました。

:::details CreateArticleFunc()
```go:internal/run/create.go
type RunEFunc func(cmd *cobra.Command, args []string) error

type Options struct {
	BasePath       string
	DefaultDirName string
}

type Option func(*Options)

func CreateArticleFunc(timeFlag *bool, name *string, options ...Option) RunEFunc {
	return func(cmd *cobra.Command, args []string) error {
		t := *timeFlag
		n := *name

		// set option
		var op Options
		for _, option := range options {
			option(&op)
		}

		// create directory name
		var dirName string

		// set timestamp in directory name
		if t {
			dirName = clock.Now().Format("2006-01-02")

			counter := 1
			for {
				if !file.Exist(filepath.Join(op.BasePath, dirName)) {
					break
				}
				counter++
				dirName = clock.Now().Format("2006-01-02") + "-" + strconv.Itoa(counter)
			}
		}

		// set specify directory name
		if n != "" {
			dirName = n
		}

		// random value since nothing was specified
		if !t && n == "" {
			if op.DefaultDirName != "" {
				dirName = op.DefaultDirName
			} else {
				dirName = uuid.NewString()
			}
		}

		// mkdir
		targetDir := filepath.Join(op.BasePath, dirName)
		if err := os.Mkdir(targetDir, 0744); err != nil {
			return err
		}

		fmt.Printf("Create directory. %s\n", targetDir)

		// create markdown file
		filePath := filepath.Join(targetDir, fmt.Sprintf("%s.md", dirName))
		if _, err := os.OpenFile(filePath, os.O_CREATE, 0644); err != nil {
			return err
		}

		fmt.Printf("Create file. %s\n", filePath)

		// create config.yaml
		configFilePath := filepath.Join(targetDir, ConfigFile)
		if err := os.WriteFile(configFilePath, []byte("title: article title\nauthor: your name"), 0644); err != nil {
			return err
		}

		fmt.Print("Create file. ", configFilePath, "\n")

		return nil
	}
}
```
:::

実際にコマンドを実行すると以下のようにディレクトリが作成されます。

```
.
├── 2023-09-14 // -tフラグを指定したとき
│   ├── 2023-09-14.md
│   └── config.yaml
├── 6e5fa681-d5de-42ef-9523-6ff77be583de フラグを指定しない時はUUID
│   ├── 6e5fa681-d5de-42ef-9523-6ff77be583de.md
│   └── config.yaml
├── article-A // -n article-Aのように任意の名前を指定
│   ├── article-A.md
│   └── config.yaml
```

## 記事画像を生成する

次に記事画像を生成するコマンドのイメージは以下のような感じです。

```
note-cli create image
```

今回作成したい画像は、zennのOGP画像のような形式で記事タイトルと作者とアイコンからなる以下のような画像を想定しました。

![](https://storage.googleapis.com/zenn-user-upload/9afea26ae0b7-20230914.png)

これを実現するために、動的にwebサイトのOGP画像を生成する方法として使われるヘッドレスブラウザでレンダリングしたページのスクリーンショットを撮り出力する方法を採用しました。

### image コマンド

画像を生成するために```image```コマンドを追加します。

```
cobra-cli add image
```

生成したファイルは以下のように修正します。

```go:cmd/image.go
package cmd

import (
	"github.com/JY8752/note-cli/internal/run"
	"github.com/spf13/cobra"
)

var imageCmd = &cobra.Command{
	Use:   "image",
	Short: "Create title image.",
	Long:  `Create title image`,
	RunE:  run.CreateImageFunc(&templateNo, &iconPath, &outputPath),
}

var (
	templateNo int16
	iconPath   string
	outputPath string
)

func init() {
	createCmd.AddCommand(imageCmd)

	imageCmd.Flags().Int16Var(&templateNo, "template", 1, "Template files can be specified by number")
	imageCmd.Flags().StringVarP(&iconPath, "icon", "i", "", "Icons can be included in the generated image by specifying the path where the icon is located")
	imageCmd.Flags().StringVarP(&outputPath, "output", "o", "", "You can specify the path to output the generated images")
}
```

フラグはテンプレートの画像を指定する```--template```フラグ、アイコン画像のパスを指定する```--icon(-i)```フラグ、出力ファイルのパスを指定する```--output(-o)```フラグをそれぞれ指定できるようにしました。

コマンドの中身の処理は```article```コマンドの時と同様、別のパッケージに切り出しました。まず、テンプレート画像を読み込むために```embed```でテンプレート画像を扱えるようにします。

```go
//go:embed templates/*
var templateFiles embed.FS

func CreateImageFunc(templateNo *int16, iconPath, outputPath *string, options ...Option) RunEFunc {
	return func(cmd *cobra.Command, args []string) error {
		var (
			tmpl *template.Template
			err  error
		)
    
    if file.Exist(filepath.Join(op.BasePath, CustomTemplateFile)) {
			// use custom template html
			tmpl, err = template.ParseFiles(filepath.Join(op.BasePath, CustomTemplateFile))
		} else {
			// use template html
			tmpl, err = template.ParseFS(templateFiles, fmt.Sprintf("templates/%d.tmpl", *templateNo))
		}
```

一応、ユーザー側でカスタムのテンプレートを用意して使用できるようにしています。カスタムテンプレートがなければ事前に用意してあるテンプレート画像を使用するようにしています。テンプレート画像は整数値で連番で用意しているので```--template```フラグの後には使用したいテンプレート画像の番号を指定するような実装にしています。テンプレート画像は以下のような形式になっています。

:::details テンプレートHTML
```html:internal/run/templates/1.tmpl
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Document</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@600&display=swap" rel="stylesheet">
  <style>
    html, body {
      width: 1200px;
      height: 600px;
    }

    body {
      display: flex;
      justify-content: center;
      align-items: center;
      background-color: rgb(144, 250, 250);
    }

    #wrapper {
      padding: 5rem;
      margin: 5rem;
      background-color: whitesmoke;
      border-radius: 30px;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1), 0 6px 20px rgba(0, 0, 0, 0.05);
      width: 800px;
      height: 300px;
    }

    #content {
      font-size: 40px;
      font-family: 'Noto Sans JP', sans-serif;
      font-weight: bold;
      word-break: break-word;
      height: 250px;
      display: flex;
      justify-content: center;
      align-items: center;
      text-align: center;
    }

    #sub-content {
      display: flex;
      justify-content: end;
      align-items: center;
      font-size: 30px;
      font-family: 'Noto Sans JP', sans-serif;
    }

    #icon {
      margin-right: 1rem;
      width: 75px;
      height: 75px;
      border-radius: 50%;
    }
  </style>
</head>
<body>
  <div id="wrapper">
    <div id="content">
      {{.Title}}
    </div>
    <div id="sub-content">
      {{if .IconPath}}
        <img id="icon" src="data:image/png;base64,{{.IconPath}}" />
      {{end}}
      <div>{{.Author}}</div>
    </div>
  </div>
</body>
</html>
```
:::

あとは```config.yaml```を読み込んで記事タイトルと著者名を取得したら、テンプレートファイルをヘッドレスブラウザを使用してレンダリングします。Goでヘッドレスブラウザを検索すると[agouti](https://github.com/sclevine/agouti)というモジュールがヒットしたりしますが、執筆時点で開発が止まっているようなので今回は[go-rod/rod](https://github.com/go-rod/rod)というモジュールを使用しました。他のモジュールとの比較をし、なぜrodを開発したのかが[ドキュメント](https://go-rod.github.io/#/why-rod)に書かれているので興味がある方はご参照ください。

rodを使用したレンダリングとスクリーンショットの撮影処理は以下のような感じです。

```go
		html := buf.String()

		// Open tabs in headless browser
		page, err := rod.New().MustConnect().Page(proto.TargetCreateTarget{})
		if err != nil {
			return err
		}

		// set template html
		if err = page.SetDocumentContent(html); err != nil {
			return err
		}

		// take screenshot
		img, err := page.MustWaitStable().Screenshot(true, &proto.PageCaptureScreenshot{
			Format: proto.PageCaptureScreenshotFormatPng,
			Clip: &proto.PageViewport{
				X:      0,
				Y:      0,
				Width:  1200,
				Height: 600,
				Scale:  1,
			},
			FromSurface: true,
		})

		if err != nil {
			return err
		}
```

完成した関数は以下のような感じです。

:::details CreateImageFunc()
```go:internal/run/create.go
//go:embed templates/*
var templateFiles embed.FS

const (
	// custom template file name
	CustomTemplateFile = "template.tmpl"
	// config file name
	ConfigFile = "config.yaml"
	// output file name
	DefaultOutputFileName = "output.png"
)

// Information on images to be generated
type Ogp struct {
	Title    string
	IconPath string
	Author   string
}

// config schema
type Config struct {
	Title  string `yaml:"title"`
	Author string `yaml:"author"`
}

func CreateImageFunc(templateNo *int16, iconPath, outputPath *string, options ...Option) RunEFunc {
	return func(cmd *cobra.Command, args []string) error {
		var (
			tmpl *template.Template
			err  error
		)

		var op Options
		for _, option := range options {
			option(&op)
		}

		if file.Exist(filepath.Join(op.BasePath, CustomTemplateFile)) {
			// use custom template html
			tmpl, err = template.ParseFiles(filepath.Join(op.BasePath, CustomTemplateFile))
		} else {
			// use template html
			tmpl, err = template.ParseFS(templateFiles, fmt.Sprintf("templates/%d.tmpl", *templateNo))
		}

		if err != nil {
			return err
		}

		// if use icon, base64 encode icon
		var encoded string
		if *iconPath != "" {
			b, err := os.ReadFile(*iconPath)
			if err != nil {
				return err
			}
			encoded = base64.StdEncoding.EncodeToString(b)
		}

		// read config yaml
		var config Config
		b, err := os.ReadFile(filepath.Join(op.BasePath, ConfigFile))
		if err != nil {
			return err
		}
		yaml.Unmarshal(b, &config)

		var buf bytes.Buffer
		tmpl.Execute(&buf, Ogp{
			Title:    config.Title,
			Author:   config.Author,
			IconPath: encoded,
		})

		html := buf.String()

		// Open tabs in headless browser
		page, err := rod.New().MustConnect().Page(proto.TargetCreateTarget{})
		if err != nil {
			return err
		}

		// set template html
		if err = page.SetDocumentContent(html); err != nil {
			return err
		}

		// take screenshot
		img, err := page.MustWaitStable().Screenshot(true, &proto.PageCaptureScreenshot{
			Format: proto.PageCaptureScreenshotFormatPng,
			Clip: &proto.PageViewport{
				X:      0,
				Y:      0,
				Width:  1200,
				Height: 600,
				Scale:  1,
			},
			FromSurface: true,
		})

		if err != nil {
			return err
		}

		// output
		if *outputPath != "" {
			err = utils.OutputFile(*outputPath, img)
		} else {
			err = utils.OutputFile(filepath.Join(op.BasePath, DefaultOutputFileName), img)
		}

		if err != nil {
			return err
		}

		fmt.Println("Complete generate OGP image")

		return nil
	}
}
```
:::

実際にコマンドで画像出力すると以下のような感じです。

```yaml:config.yaml
title: noteの執筆体験をzennに近づけるためにGoでCLIツールを開発した話~はじめてのOSS~
author: Junichi.Y
```

```
note-cli create image -i ./icon.png --template 1 -o ./output.png
```

![](https://storage.googleapis.com/zenn-user-upload/85e99cc82649-20230914.png)

## CLIツールを公開する

こちらのコマンドはGoで作成されているので```go install```を実行してインストールすることで使用することができます。また、macであればHomebrewでインストールできるようにリリースしました。やり方は以下の記事そのままです

https://zenn.dev/kou_pg_0131/articles/goreleaser-usage

## OSSプロジェクトにするための最低限の設定

note-cliはOSSとして公開しようとしたものの、筆者はOSSとして自作のライブラリを公開したこともコントリビュートしたこともなかったため手探りですが最低限やったほうが良さそうなことはやりました。

### 画像を設定する

イケてるOSSはREADMEに画像を添えるとどこかでみた気がしたのでまずは画像の用意をしました。ただし、筆者はこの手のイケてる画像を作成する自身がなかったのでAIの力を借りて、今回は[ideogram](https://ideogram.ai/)というサイトを使用しました。

あまり生成AI系はそこまで詳しくないのですがこちらのサイトは今での画像生成AIではできなかった**プロンプトに設定した文字列を画像に反映する**ことができるらしくプロンプトに```note-cli```という文字を含めて画像を生成しました。生成した画像とプロンプトはこちらです

https://ideogram.ai/g/17S7u69SRBmiJ-1PntPHHA/0

### READMEを書く

筆者は英語で文書を書くことに慣れていないのでこちらもAIの力を借ります。最初に日本語で作成し、DeepLで翻訳し、パッと見おかしなところをなおしていきました。文法的におかしなところがあるかもしれませんが、たぶん十分意味は通じそうなのでこれでよしとしました。

### LICENSE

OSSとして公開するわけなので安心して使えるようLICENSEを配置します。LICENSEまわりもあまり詳しくないのでとりあえずMITライセンスで配置しました。

### CI

静的解析およびテストを実行するCIを作成する。今回はGithub Actionsで作成しました。

### リポジトリ設定

最低限以下の内容だけ設定しました。

- mainブランチへの直pushを禁止。
- マージするのにPull Requestの作成を必須にする。
- マージするのにCIが成功してなければできないようにする。

## まとめ

CLIツールを作成してOSSとして公開するのにだいぶハードルを感じていたのですがCobraを使用することでかなり簡単に実装できましたし、リリースに関しては[goreleaser](https://github.com/goreleaser/goreleaser)が便利すぎました。CLI作るならGoって言われるのがわかった気がします。まじで便利

実際に作成したCLIツールを使用してnoteの記事を作成してみましたが、最初に想定していなかったことなどもあり完全に満足いくものにはならなかったのですが最低限求めていた機能は実装できたため、個人的にnoteの記事の管理に使っていこうと思います。

もし、noteで記事書いてるよという方がいましたらぜひリポジトリだけでも見てみてください。そして良ければスターください😅
noteの記事管理だけでなく個人で作成した個人ブログの記事管理にももしかしたら使えるかもしれません。

できるかわからないのですが今度はヘッドレスブラウザを使用してコマンドから記事のドラフトを登録するところまでできればいいなと思っています。(ヘッドレスブラウザでログインしたあと、DOM操作すればいけそう？？)

今回は以上です🐼

## 反省点

考えが甘かったところや反省点です。

### 画像を貼り付ける時にmarkdownで管理できない

zennではmarkdownで画像URLを指定するだけで表示できるのですが、noteでは表示することができませんでした。そのため、コピペで記事を貼り付けた後、画像を差し込みたいところに手動で差し込む必要がありました。webで表示するエディタの仕様など詳しくはないのですがマウス操作からでないと画像の差し込みは難しそうだったのでここは諦めました。

### 画像生成のコマンドを作成したがそもそもnoteで画像作成できた

noteは[Canva](https://www.canva.com/)というサービスと連携していて雛形のテンプレート画像を選択してテキストを挿入して画像を作成できる。自分で用意したオリジナルの画像をアップロードしてそこにテキスト挿入することもできる。

コマンドいらなかったんじゃ...まあ、コマンドで画像生成できたほうが早いかもしれないし、HTMLでテンプレート作れるし、まあ...
