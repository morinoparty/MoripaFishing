# 🎣 MoripaFishing - Minecraft 釣りプラグイン 🎣

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Minecraftサーバーで、より豊かでカスタマイズ可能な釣り体験を提供するプラグインです。🐟✨

## 🌟 プロジェクト概要

MoripaFishingは、サーバー管理者が独自の魚、レアリティ、そして釣りの条件（ワールド、バイオーム、天気など）を自由に設定できるMinecraftプラグインです。プレイヤー（Angler）の釣り記録を管理し、拡張可能なAPIを提供します。

## ⚙️ 主な機能

*   **🐠 カスタム魚定義:** JSONファイルで名前、説明、レアリティ、釣れる条件（ワールド、バイオーム、天気、時間帯など）を自由に設定可能。
*   **💎 カスタムレアリティ:** 独自のレアリティ段階（例: Common, Rare, Epic, Legendary, Mythic）を作成し、それぞれに色や確率を設定。
*   **🌍 条件ベースの釣り:** 特定のワールド、バイオーム、天気、時間帯でのみ釣れる魚を設定。
*   **🎣 プレイヤー記録:** プレイヤーごとの釣り上げた魚の記録・統計（将来的な拡張）。
*   **🔧 簡単な設定:** `plugins/MoripaFishing/` ディレクトリ内のJSONファイルで直感的に設定可能。
*   **💻 開発者向けAPI:** `api`モジュールを通じて、他のプラグインとの連携や機能拡張が可能。
*   **🛠️ 管理コマンド:** 天気やワールド設定を変更するコマンドを提供（例: `/mf weather`, `/mf world`）。

## 📦 モジュール構成

*   **`api`**: プラグインのコアとなるインターフェース、データモデル、イベントを提供します。他の開発者がMoripaFishingと連携する際に利用します。
*   **`app`**: `api`モジュールを実装した、Minecraftサーバーで動作するメインのプラグインです。コマンド処理や設定ファイルの読み込みなども担当します。

## 🚀 導入方法

1.  **前提条件:**
    *   Java 17 以降
    *   PaperMC または互換性のあるMinecraftサーバー (Spigot, Bukkitなど)
2.  **ビルド:**
    ```bash
    ./gradlew build
    ```
    ビルドが成功すると、`app/build/libs/` ディレクトリにプラグインのJARファイル (`app.jar`) が生成されます。
3.  **インストール:**
    *   生成されたJARファイルを、お使いのMinecraftサーバーの `plugins/` ディレクトリに配置します。
    *   サーバーを起動（または再起動）します。初回起動時に `plugins/MoripaFishing/` ディレクトリとデフォルトの設定ファイルが生成されます。
4.  **設定:**
    *   `plugins/MoripaFishing/` 内の `config.json`, `rarity/`, `fish/` ディレクトリにあるJSONファイルを編集して、プラグインの動作、レアリティ、魚をカスタマイズします。
    *   設定例は `app/src/test/resources/plugins/moripa_fishing/` を参考にしてください。

## ⚙️ 設定ファイル

*   **`plugins/MoripaFishing/config.json`**: プラグイン全体の基本設定。
*   **`plugins/MoripaFishing/rarity/*.json`**: 魚のレアリティを定義します。ID、表示名、色コード、重み（出現確率）などを設定できます。
*   **`plugins/MoripaFishing/fish/<rarity_id>/*.json`**: 各レアリティに属する魚を定義します。ID、表示名、説明文、アイコン（Minecraft Item ID）、釣れる条件（ワールド、バイオーム、天気、時間、高さなど）を設定できます。

## ⌨️ コマンド

*   `/mf weather <world> <weather_type>`: 指定したワールドの天気を変更します (例: `/mf weather world SUNNY`)。
*   `/mf world ...`: (詳細なサブコマンドは実装によります) ワールド関連の操作を行います。
*   `/mf reload`: 設定ファイルを再読み込みします。

## 🤝 コントリビューション

バグ報告や機能提案は、GitHub Issuesまでお気軽にどうぞ！プルリクエストも歓迎します。