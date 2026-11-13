import { createFileRoute } from "@tanstack/react-router";
import { createFromSource } from "fumadocs-core/search/server";

import { source } from "@/lib/source";

// 既定の multilingual モードは Unicode の単語分割を使うため、日本語もそのまま検索できる
const server = createFromSource(source);

// プリレンダー時に検索インデックスを api/search.json として書き出す
export const Route = createFileRoute("/api/search.json")({
    server: {
        handlers: {
            GET: () => server.staticGET(),
        },
    },
});
