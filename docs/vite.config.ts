import pandacss from "@pandacss/dev/postcss";
import tailwindcss from "@tailwindcss/vite";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import react from "@vitejs/plugin-react";
import { fumadocsMdx } from "fumadocs-mdx/vite";
import { defineConfig, lazyPlugins, type Plugin } from "vite-plus";

// プレビュー環境では /PluginName/<sha>/docs のようなサブパスに配置される (CI が BASE_PATH を渡す)
// Vite の base から TanStack Router の basepath も導出される
const basePath = (process.env.BASE_PATH ?? "").replace(/\/+$/, "");

// 整形・lint の対象外 (依存・生成物)
const ignorePatterns = [
    "**/node_modules/**",
    "**/out/**",
    "**/dist/**",
    "**/public/**",
    "**/.source/**",
    "**/.tanstack/**",
    "**/styled-system/**",
    "**/routeTree.gen.ts",
];

// 開発サーバーでも public/dokka のディレクトリの URL (dokka/ など) で index.html を返す
// Vite は public/ のディレクトリを index.html に解決しないため、そのままだとドキュメントの splat ルートが 404 を返してしまう
function dokkaDirectoryIndex(): Plugin {
    return {
        name: "dokka-directory-index",
        apply: "serve",
        configureServer(server) {
            server.middlewares.use((req, _res, next) => {
                const url = new URL(req.url ?? "/", "http://localhost");
                const dokkaPrefix = `${basePath}/dokka/`;
                if (url.pathname.startsWith(dokkaPrefix) && url.pathname.endsWith("/")) {
                    req.url = `${url.pathname}index.html${url.search}`;
                }
                next();
            });
        },
    };
}

export default defineConfig({
    base: `${basePath}/`,
    fmt: {
        tabWidth: 4,
        printWidth: 120,
        // package.json のキー順は手で管理する
        sortPackageJson: false,
        // Chlorophyll (morinoparty/Chlorophyll) と同じ並び (プロトコル付き → パッケージ → エイリアス → 絶対パス → 遠い相対パス順)
        sortImports: {
            newlinesBetween: false,
            // 既存の空行による区切りを保ち、それをまたいで並べ替えない
            partitionByNewline: true,
            // コメントを境に並べ替えを区切る。順序に意味のある副作用 import (CSS など) の直前にはコメントを置く
            partitionByComment: true,
            customGroups: [
                // node:fs や fumadocs-mdx:collections/browser のようなプロトコル付きの指定子
                { groupName: "protocol", elementNamePattern: ["*:*", "*:*/**"] },
                { groupName: "absolute", elementNamePattern: ["/**"] },
                { groupName: "parent-2", elementNamePattern: ["../../**"] },
            ],
            groups: [
                ["builtin", "protocol"],
                "external",
                ["internal", "subpath"],
                "absolute",
                "parent-2",
                "parent",
                ["sibling", "index"],
                "unknown",
            ],
        },
        // content/ (ドキュメント本文と meta.json) は整形しない
        ignorePatterns: [...ignorePatterns, "content/**", "**/*.md", "**/*.mdx", "**/*.yml", "**/*.yaml"],
    },
    lint: {
        plugins: ["eslint", "typescript", "unicorn", "oxc", "react"],
        categories: { correctness: "error" },
        options: { reportUnusedDisableDirectives: "warn" },
        ignorePatterns,
    },
    css: {
        postcss: {
            // chlorophyll のデザイントークンを Panda CSS で生成する (src/styles/tokens.css の @layer 宣言に展開される)
            plugins: [pandacss()],
        },
    },
    resolve: {
        tsconfigPaths: true,
    },
    build: {
        // サーバー側の成果物 (プリレンダー用)。公開するのは client の出力のみ
        outDir: "dist",
    },
    environments: {
        // 静的サイトとして公開するディレクトリ (CI は docs/out をアップロードする)
        client: { build: { outDir: "out" } },
    },
    server: {
        port: 3000,
    },
    // lint / fmt など設定値だけを読むコマンドではプラグインを初期化しない
    plugins: lazyPlugins(() => [
        dokkaDirectoryIndex(),
        fumadocsMdx(),
        tailwindcss(),
        tanstackStart({
            prerender: {
                enabled: true,
                // サイドバーなどのリンクをたどってすべてのページを書き出す
                crawlLinks: true,
                // public/ からそのままコピーされる Dokka の出力は、アプリのページとして書き出さない
                // (ドキュメントの splat ルートで描画すると、コピーされた dokka/**/index.html を 404 ページで上書きしてしまう)
                // クロールで見つかるパスには base が付く場合があるため、base を外してから判定する
                filter: (page) => {
                    const path =
                        basePath && page.path.startsWith(`${basePath}/`) ? page.path.slice(basePath.length) : page.path;
                    return !/^\/dokka(\/|$)/.test(path);
                },
            },
            // リンクからたどれないルートは明示する
            pages: [
                // ドキュメントのトップ (サイトのルート)
                { path: "/" },
                { path: "/api/search.json" },
                { path: "/llms.txt" },
                { path: "/llms-full.txt" },
                // 静的ホスティングが存在しない URL で返すページ (404/index.html ではなく 404.html として出力する)
                { path: "/404", prerender: { autoSubfolderIndex: false } },
            ],
        }),
        react(),
    ]),
});
