# Git のルール

## Repository

- `https://github.com/morinoparty/MoripaFishing`

## ブランチ

- ブランチは最新の `main` から作成し、PR の base は `main` にしてください。
- ブランチ名に `feature/`・`fix/`・`docs/`・`chore/` を付けると、autolabeler がラベルを付けます。

## コミットメッセージ・PR タイトル

- 英語で、[gitmoji](https://gitmoji.dev/) から始めてください。形式は `<emoji> <imperative summary>` です。

```text
✨ Add weather provider registry
🐛 Fix world lookup on reload
```

- PR タイトルも同じ形式にしてください。

## ラベルとリリース

- リリースノートとバージョンは release-drafter が PR のラベルから決めます（`.github/release-drafter.yml`）。
- PR タイトル先頭の gitmoji から、主に次のラベルが自動で付きます。

| gitmoji | ラベル | バージョン |
| --- | --- | --- |
| 💥 | `breaking` | minor |
| ✨ 💄 ⚡️ | `enhancement` | minor |
| 🐛 🚑️ | `bug` | patch |
| 🔒️ | `security` | patch |
| 📝 | `document` | patch |
| ♻️ | `refactor` | patch |
| ✅ 🧪 | `test` | patch |
| 🔧 🔨 👷 💚 🙈 🔥 📦️ ➕ ➖ ⬆️ ⬇️ 🚚 | `chore` | patch |

- 1.0.0 未満の開発中はメジャーバージョンを上げません。破壊的変更でも minor として扱います。
- 1.0.0 以降は `.github/release-drafter.yml` の `version-resolver` で `breaking` を major に戻してください。
- 破壊的変更を含む PR では `💥` を使うか、`breaking` ラベルを付けてください。
- リリースノートに載せない PR には `skip-changelog` を付けてください。

## Issue

- 新しい機能や大きな振る舞い変更では、Issue を確認または作成してください。
- Issue は英語で記述してください。
- ラベルは `.github/labels.json` にある既存のものだけを使ってください。
