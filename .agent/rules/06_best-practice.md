# 推奨される書き方

## Test

- テストは主要な分岐と回帰しやすい挙動を優先して追加してください。
- MockBukkit を使わずに済むロジック（決定処理など）は、純粋な関数として切り出してテストしてください。
- 既存テストは JUnit 5、MockK、MockBukkit、Koin Test に沿っています。
- `@DisplayName` を付ける場合は、短い英語で意図が分かる名前にしてください。

## Minecraft

- `CommandSender` などへメッセージを送る既存箇所では、`sendRichMessage` と MiniMessage ベースの表現を優先してください。
- 翻訳や表示文言に関わる処理は、既存の `TranslateManager` や Adventure ベースの実装に合わせてください。
