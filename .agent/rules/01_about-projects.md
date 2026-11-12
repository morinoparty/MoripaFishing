## このプロジェクトの概要

`MoripaFishing` は Minecraft サーバー向けの釣りプラグインです。
魚・レアリティ・天候・時間帯・ワールド条件などを JSON ベースで定義でき、運営者が独自の釣り体験を構築できます。

利用者向け API、Paper 実装（Core）、オプショナルな Integration と Addon、Fumadocs ベースのドキュメントサイトを同じリポジトリで管理しています。

## 主な技術スタック

- Kotlin 2.4 / Java 25
- Gradle Kotlin DSL（バージョンは `gradle/libs.versions.toml` で管理）
- Paper API
- Koin
- Arrow
- kotlinx.serialization / KAML
- JUnit 5 / MockK / MockBukkit
- Next.js + Fumadocs + MDX

## モジュール概要

- `api`: 外部公開用 API、モデル、設定スキーマ、イベント、シリアライザ
- `bukkit`: Core プラグイン（`MoripaFishing`）。コマンド、リスナー、マネージャー群
- `integrations/*-api`: Core と Integration の境界となる SPI。`api` には依存しない
- `integrations/weather`: 天候をワールドへ適用する Integration プラグイン
- `integrations/world-lifecycle`: ワールドの生成・境界・時刻などを扱う Integration プラグイン
- `addons/*`: `api` だけに依存する拡張プラグイン（例: `catch-announce`）
- `docs`: Fumadocs ベースのドキュメントサイト
- `buildSrc`: Gradle の補助タスクやビルドロジック

## Core / Integration / Addon の役割

- Core は釣りの判定と各種の決定（天候・時刻・魚の抽選など）だけを行い、Bukkit のワールド状態（天候・ゲームルール・時刻・スポーン・ボーダー・ワールドの生成や破棄）を自分では変更しません。
- ワールドを変更する処理は Integration が担当し、Core は softdepend で Integration を検出して利用します。
- Core と無関係な付加機能は Addon として切り出し、`api` だけに依存させます。
