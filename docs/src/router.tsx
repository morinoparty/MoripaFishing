import { createRouter as createTanStackRouter } from "@tanstack/react-router";

import { NotFound } from "@/components/not-found";

import { routeTree } from "./routeTree.gen";

export function getRouter() {
    return createTanStackRouter({
        routeTree,
        defaultPreload: "intent",
        scrollRestoration: true,
        defaultNotFoundComponent: NotFound,
        // 静的ホスティングで dir/index.html に解決されるよう、URL は末尾スラッシュ付きにそろえる
        trailingSlash: "always",
        // PR コメントなどから dir/index.html を直接開いた場合も、dir/ と同じルートとして解釈する
        rewrite: {
            input: ({ url }) => {
                if (!url.pathname.endsWith("/index.html")) return undefined;
                url.pathname = url.pathname.slice(0, -"index.html".length);
                return url;
            },
        },
    });
}
