## ディレクトリ配置規則

- ドキュメントは `docs/content/docs/` に配置します。
- `docs/content/docs/` のトップレベルは `core/`、`integration/`、`addons/` を基本構造とします。
- ドキュメント用の React コンポーネントは `docs/components/` に配置します。
- Next.js / Fumadocs のアプリ設定は `docs/app/` に配置します。
- メインプラグイン実装は `bukkit/src/main/kotlin/party/morino/moripafishing/` に配置します。
- API のインターフェース・モデル・設定スキーマは `api/src/main/kotlin/party/morino/moripafishing/api/` に配置します。
- 統合プラグインは `integrations/*/src/main/kotlin/party/morino/moripafishing/integrations/` に配置します。
- テストは各モジュールの `src/test/kotlin/` に配置します。
- data class は対応する責務の `model` や近接パッケージに配置します。
- 1 ファイル 1 クラスを原則とします。
- パッケージは責務単位で分割し、肥大化しすぎる場合は下位パッケージへ分けてください。
