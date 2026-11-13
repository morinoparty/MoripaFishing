import { createFileRoute } from "@tanstack/react-router";

import { NotFound } from "@/components/not-found";
import { appName } from "@/lib/shared";

// 静的ホスティング (GitHub Pages) が存在しない URL で返す 404.html を書き出すためのルート
// vite.config.ts のプリレンダー設定で /404.html として出力する
export const Route = createFileRoute("/404")({
    component: NotFound,
    head: () => ({
        meta: [{ title: `Not Found | ${appName}` }, { name: "robots", content: "noindex" }],
    }),
});
