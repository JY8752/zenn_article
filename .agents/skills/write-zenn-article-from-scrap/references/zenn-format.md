# Zenn Article Format

Use the current official Zenn documentation as the source of truth:

- [ZennのMarkdown記法一覧](https://zenn.dev/zenn/articles/markdown-guide)
- [Zenn CLIで記事・本を管理する方法](https://zenn.dev/zenn/articles/zenn-cli-guide)
- [Zennのスラッグ（slug）とは](https://zenn.dev/zenn/articles/what-is-slug)

Reopen the relevant official page before using unfamiliar or potentially changed
syntax. This file records only the conventions commonly needed in this repository.

## Article File

Place an article at `articles/<slug>.md`. A new slug must be unique and contain
12–50 lowercase letters, digits, hyphens, or underscores.

Use this frontmatter shape for a new draft:

```yaml
---
title: ""
emoji: "📝"
type: "tech"
topics: []
published: false
---
```

Choose `tech` or `idea` from the source material. Keep `published: false` until the
user explicitly handles publication.

## Markdown Conventions

- Start body headings at `##`; the article title supplies the first-level heading.
- Label fenced code blocks with the language.
- Show a filename with `` ```language:filename `` when the source path is known.
- Put a URL on its own line, with surrounding blank lines, when a link card is
  appropriate.
- Use `@[card](URL)` when a bare URL cannot be parsed correctly.
- Preserve useful alt text for images when it is available from the source.

## Zenn-Specific Blocks

Use a normal message only for a genuinely useful note:

```md
:::message
内容
:::
```

Use an alert only for a real warning:

```md
:::message alert
内容
:::
```

Use details for optional long material:

```md
:::details タイトル
内容
:::
```

Do not add custom blocks only to imitate the author's previous articles.

## Local Checks

- Preserve the local Markdown rules in `.markdownlint.json`.
- Run `node_modules/.bin/textlint <article-path>` when present; otherwise try
  `pnpm exec textlint <article-path>`.
- Preview with the installed Zenn CLI when visual rendering must be checked.
