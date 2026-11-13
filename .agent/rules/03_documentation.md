# ドキュメント

## ドキュメントの作成

- ドキュメントは `docs/content/docs/` 配下の `.mdx` に配置してください。
- トップレベルの構成は `(core)`、`integration`、`addons` の 3 系統を基本としてください（`(core)` は URL に現れない Fumadocs のグループです）。
- `core` には本体機能の説明、`integration` には外部連携、`addons` には Core API を利用する拡張の説明を配置してください。
- 近い機能は既存ページへ追記し、独立した概念や利用フローは新規ページに分けてください。
- 新しいカテゴリや階層を追加する場合は、同じディレクトリの `meta.json` も更新してください。
- トップレベルの 3 セクションは `meta.json` に `root: true` を付けて扱ってください。
- Docusaurus 前提ではなく、Fumadocs の構成に従ってください。
- 文体は説明文として自然な文章を使い、コードコメントのような断片的表現を避けてください。
- 既存ドキュメントが絵文字付きタイトルを使っている場合は、そのトーンに合わせてください。
- ドキュメントはサイトのルートに配置されます。ページ間のリンクは `/format/data/world` のように `/docs` を付けずに書いてください。
- icon は Lucide のアイコン名で指定します。新しいアイコンを使う場合は、`docs/src/lib/source.ts` の `icons` にも追加してください。

## コンポーネントの利用

- ドキュメントサイトは PluginTemplateKt と同じ構成 (Fumadocs + TanStack Start + Vite+) です。
- ドキュメント用コンポーネントは `docs/src/components/` 配下に配置してください。UI 部品は `docs/src/components/ui/`、ドメイン固有の表示は既存の分類 (`commands`・`core`・`chart`・`tables`) に合わせてください。
- MDX から使うコンポーネントは `docs/src/components/mdx.tsx` の `getMDXComponents` に登録してください。MDX ファイルからは import しないでください。
- 既存 UI を再利用できる場合は新規追加より再利用を優先してください。
- `docs/src/routes/`・`docs/src/lib/`・`docs/vite.config.ts` などの基盤は PluginTemplateKt に合わせています。構成を読んだ上で最小限の変更に留めてください。
- 変更後は `docs/` で `pnpm run check` と `pnpm run build` を実行してください。
