# 推奨される書き方

## DI

- DI コンテナは使いません。依存はコンストラクタで受け取ってください。
- コアの組み立ては `MoripaFishingCore` (コンポジションルート) だけで行います。新しいコンポーネントもここで生成して渡してください。
- コマンドはブートストラップ時に登録されるため、コアを `() -> T` の関数で遅延参照します。

## Test

- テストは主要な分岐と回帰しやすい挙動を優先して追加してください。
- テストは JUnit、MockBukkit と、`testing/` などに置いた `Fake*` クラスで書きます。モックライブラリは使いません。
- MockBukkit を使うテストは `@ExtendWith(MockBukkitExtension::class)` と `@MockBukkitInject` を使ってください。
- `@DisplayName` を付ける場合は、短い英語で意図が分かる名前にしてください。

## Minecraft

- `CommandSender` などへメッセージを送る既存箇所では、`sendRichMessage` と MiniMessage ベースの表現を優先してください。
- 翻訳は `Translations` が `GlobalTranslator` に登録します。表示文言は翻訳キーと Adventure の `Component.translatable` で扱ってください。
