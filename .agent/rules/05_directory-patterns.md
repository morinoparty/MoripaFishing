## ディレクトリ配置規則

- Core の実装は `bukkit/src/main/kotlin/party/morino/moripafishing/` に配置します。
- API のインターフェース・モデル・設定スキーマ・イベントは `api/src/main/kotlin/party/morino/moripafishing/` に配置します。
- Integration は `integrations/<name>/src/main/kotlin/party/morino/moripafishing/integrations/<name>/` に、その SPI は `integrations/<name>-api/` に配置します。
- Addon は `addons/<name>/` に配置します。
- テストは各モジュールの `src/test/kotlin/` に配置します。
- data class は対応する責務の `model` や近接パッケージに配置します。
- パッケージは責務単位で分割し、肥大化しすぎる場合は下位パッケージへ分けてください。
- ドキュメントの配置は `03_documentation.md` に従ってください。
