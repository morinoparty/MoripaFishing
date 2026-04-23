## このプロジェクトの概要

`MoripaFishing` は Minecraft サーバー向けの釣りプラグインです。
魚・レアリティ・天候・時間帯・ワールド条件などを JSON ベースで定義でき、運営者が独自の釣り体験を構築できます。

また、利用者向け API と Paper/Bukkit 実装、追加統合モジュール、Fumadocs ベースのドキュメントサイトを同じリポジトリで管理しています。

## 主な技術スタック

- Kotlin 2.0
- Gradle Kotlin DSL
- Java 21
- Paper / Bukkit API
- Koin
- Arrow
- kotlinx.serialization / KAML
- JUnit 5 / MockK / MockBukkit
- Next.js + Fumadocs + MDX

## モジュール概要

- `api`: 外部公開用 API、モデル、設定スキーマ、シリアライザ
- `bukkit`: メインプラグイン実装、コマンド、リスナー、マネージャー群
- `integrations/world-lifecycle`: ワールド生成連携用の追加プラグイン
- `docs`: Fumadocs ベースのドキュメントサイト
- `buildSrc`: Gradle の補助タスクやビルドロジック
