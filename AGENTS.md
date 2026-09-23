# Repository Guidelines

This file provides shared guidance for coding agents working in this repository.

## Repository Overview

This is a Zenn publication repository for technical writing in Japanese. Zenn is a Japanese technical content publishing platform where articles and books are written in Markdown and published through Git commits.

## Common Commands

### Article Management

```bash
# Create a new article
npx zenn new:article

# Preview articles and books locally (opens http://localhost:8000)
npx zenn preview

# Lint Japanese text for technical writing standards
npx textlint articles/*.md

# Lint all Markdown files
npx textlint "**/*.md"
```

### Package Management

This project uses pnpm as the package manager:

```bash
pnpm install
```

## Repository Structure

- `/articles/` - Individual technical articles in Markdown format with YAML frontmatter
- `/books/` - Technical books organized in directories with chapters and `config.yaml`
- `/demo/` - Example code and projects referenced in articles (Go, Kotlin, Rust, etc.)
- `/docs/` - Writing style guidelines
- `/references/` - Reference materials for articles
- `/vhs/` - Demo recordings and GIFs

## Article Format

Articles use YAML frontmatter:

```yaml
---
title: "Article Title"
emoji: "🐼"
type: "tech" # or "idea"
topics: ["go", "grpc", "testing"]
published: true # or false
---
```

## Book Structure

Each book directory contains:

- `config.yaml` - Book metadata and chapter ordering
- `cover.png` - Book cover image
- Numbered Markdown files (`1.md`, `2.md`, etc.) - Chapters

## Writing Guidelines

Follow the author's established style:

- Focus primarily on Go backend development, gRPC, testing, and clean architecture.
- Prefer practical, step-by-step explanations with clear objectives.
- Use the 🐼 emoji where appropriate and maintain a humble, approachable tone.
- Articles typically include objectives, target audience, prerequisites, and summaries.

## Textlint Configuration

The repository uses Japanese technical writing linters:

- `textlint-rule-preset-ja-technical-writing` - Technical writing standards
- `textlint-rule-preset-jtf-style` - Japan Technical Communicators Forum style guide

Use these rules to keep Japanese technical documentation consistent and professional.
