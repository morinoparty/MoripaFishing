import { remarkDirectiveAdmonition, remarkMdxMermaid } from "fumadocs-core/mdx-plugins";
import { defineConfig } from "fumadocs-mdx/config";
import remarkDirective from "remark-directive";

// コレクションは src/lib/source.ts で fumadocs-mdx/macro を使って定義する
// ここではすべての MDX に共通する設定だけを置く
export default defineConfig({
    mdxOptions: {
        // ```mermaid のコードブロックと :::note などのディレクティブを使えるようにする
        remarkPlugins: [remarkMdxMermaid, remarkDirective, remarkDirectiveAdmonition],
    },
});
