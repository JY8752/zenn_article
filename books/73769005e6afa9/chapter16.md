---
"title": "質の良いテストとテストのグルーピング"
---

この章では**質の良いテストの4つの性質**について触れ、作成したテストを実行単位でグループ分けしていきたいと思います。

## 質の良いテストの４つの性質

「単体テストの考え方/使い方」において質の良いテストは以下のような４つの性質で構成されていると述べられています。

- 退行(regression)に対する保護
- リファクタリングへの耐性
- 迅速なフィードバック
- 保守のしやすさ

これらの性質は足し算ではなく**掛け算**のため、**一つでも性質の指標値が0になればテストの質も0**となってしまいます。そして、これらの性質による分析は単体テストだけでなく結合テスト、E2Eテストに対しても有効です。

しかし、これらの性質を**全て完全に満たすことは不可能**であり、それぞれの性質はトレードオフの関係となっておりその性質をどの程度頑張るかでテストの種別が変わってきます。

### 退行(regression)に対する保護

退行に対する保護とはテストをすることで退行(もしくはバグ)の存在をいかに検出できるのかを示す性質のことです。この性質はプロダクションコードをどれだけ実際に使用するかに比例します。つまり、単体テストよりも結合テスト、結合テストよりもE2Eテストの方が**退行に対する保護を備えている**と言えます。

テストがなぜ単体テストだけではだめかというと、**単体テストは実際に使用するプロダクションコードが少なく退行に対する保護の性質をあまり備えていない**ため、結合テストやE2Eテストでそこを補う必要があるためです。

### リファクタリングへの耐性

この性質はいかに**偽陽性**を生み出すことなく、プロダクションコードに対してリファクタリングを行えるかを示す性質のことです。偽陽性とは機能自体は問題ないはずなのにテストが失敗してしまう**嘘の警告**のことです。

偽陽性がテストに生まれるとテストへの信頼が失われることとなり、リファクタリングをすることが容易ではなくなります。そのため、偽陽性は徹底的に排除しなければなりません。

「単体テストの考え方/使い方」がモックの使用を避け古典学派の考え方を推奨するのは**モックを使用することでプロダクションコードとの結びつきが強くなり偽陽性を生みやすくなる**からです。

そして、このリファクタリングへの耐性は**0か1**で備えているか全く備えていないかのどちらかなのでリファクタリングへの耐性は最大限頑張らなければなりません。

### 迅速なフィードバック

この性質は**テストの実行時間**のことです。単体テストが実行時間が1番短くなるため、最もこの性質を備えていると言えます。逆に結合テスト、E2Eテストは実行時間が長くなるのでこの性質をあまり備えていないということになります。

この性質は**退行に対する保護**とのトレードオフになり、E2Eテストは退行に対する保護の性質を備えている代わりに迅速なフィードバックの性質を犠牲にしていると言えます。

### 保守のしやすさ

この性質はテストケースがどれほど**簡潔で読みやすく短いか**が関係してきます。この性質はテスト作成者が**どれだけ意識できるか**が大事になってくる性質です。

上記4つの性質の関係は以下のような図で表すことができます。

![](https://storage.googleapis.com/zenn-user-upload/4817e90817e1-20231109.png)

また、これらの性質の極端な例でアンチパターンを示した図が以下の図です。

![](https://storage.googleapis.com/zenn-user-upload/42aed4a297a9-20231109.png)

**壊れやすいテスト**は退行に対する保護も迅速なフィードバックの性質も備えていますが、このようなテストはプロダクションコードと深く結びつき**偽陽性**を多く含みます。

**取るに足らないテスト**は例えばある構造体のプロパティを確認するような些細なテストのことです。これは実行時間も短く、偽陽性が持ち込まれることも少ないためリファクタリング耐性も十分に備えています。しかし、このようなテストが**退行を検知することはほとんどありません**。このような取るにたらないテストは**プロダクションコードと同じことを別の書き方で表現している**だけで**何も検証していない**テストです。

## テストのグルーピング

前述したようにテストの種類により質の良いテストの性質をどれくらい備えているかが変わり、結合テストとE2Eテストは**迅速なフィードバックの性質をあまり備えていません**。実際の機能開発において**コードの変更とテストの実行**はセットになり、なるべく早くこのサイクルを回したいのが理想です。

そのため、本書の最後に各テストをテスト種別で分類しそれぞれを種別ごとに実行できるようにしたいと思います。これを実現するためにGoのビルドタグを使用します。

例えば、ドメイン層の単体テストのファイルの先頭に```//go:build unit```と記載すると```go test -tags=unit ./...```というコマンドを実行することで、記載したテストファイルのテストケースのみを実行できます。結合テスト、E2Eテストも同様にそれぞれ```integration```、```e2e```というタグを付けることでそれぞれで実行することができます。

グルーピングしたテストの実行計画はチームや組織でよく検討すべき内容だと思いますが、例えばローカル環境で開発を進めている間は時間のかかる結合テスト、E2Eテストは飛ばして単体テストのみを実行しながら開発を進めるなどが考えられます。

結合テストやE2Eテストをどのタイミングでどのように実行するかはよく検討する必要があります。

最後にテストの実行コマンドをタスク化して実行しやすいようにして本書の内容を以上とさせていただきます。

```yaml:Taskfile.yml
  test:unit:
    cmds:
      - go test -tags=unit -v -count=1 ./...
    desc: 'execute unit tests.'
  test:integration:
    cmds:
      - go test -tags=integration -v -count=1 ./...
    desc: 'execute integration tests.'
  test:e2e:
    cmds:
      - go test -tags=e2e -v -count=1 ./...
    desc: 'execute e2e tests.'
  test:all:
    cmds:
      - go test -tags=unit,integration,e2e -v -count=1 ./...
    desc: 'execute all tests.'
```

```
% task test:unit // 単体テストのみを実行
% task test:integration // 結合テストのみを実行
% task test:e2e // E2Eテストのみを実行
% task test:all // 全てのテストを実行
```

## まとめ

- 質の良いテストは以下の4つの性質で分析することができる。
  - 退行に対する保護
  - リファクタリングへの耐性
  - 迅速なフィードバック
  - 保守のしやすさ
- **リファクタリングへの耐性**と**保守のしやすさ**は**最大限頑張る必要がある**。
- **退行に対する保護**と**迅速なフィードバック**は**トレードオフの関係でその割合でテストの種別が変わる**。
- 各テストに**ビルドタグ**を使用してタグ付けすることで**指定の種別のテストのみを実行する**ことができる。