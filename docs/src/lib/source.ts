import { llms, loader } from "fumadocs-core/source";
import { defineDocs } from "fumadocs-mdx/macro";
import {
    BookOpen,
    Calculator,
    CloudRain,
    Code,
    Database,
    Ellipsis,
    FileCog,
    Fish,
    FishSymbol,
    FolderTree,
    Gem,
    Globe,
    Languages,
    Layers,
    Megaphone,
    Plug,
    Puzzle,
    Receipt,
    Rocket,
    Scale,
    Settings,
    User,
    Wrench,
} from "lucide-react";
import { createElement } from "react";

import { docsRoute } from "./shared";

// meta.json / frontmatter の icon で使う Lucide アイコン
// source はブラウザでも読み込まれるため、全アイコンを含む lucideIconsPlugin は使わず、使うものだけをここに追加する
const icons = {
    BookOpen,
    Calculator,
    CloudRain,
    Code,
    Database,
    Ellipsis,
    FileCog,
    Fish,
    FishSymbol,
    FolderTree,
    Gem,
    Globe,
    Languages,
    Layers,
    Megaphone,
    Plug,
    Puzzle,
    Receipt,
    Rocket,
    Scale,
    Settings,
    User,
    Wrench,
};

export const docs = defineDocs({
    dir: "content/docs",
    docs: {
        async: true,
        postprocess: {
            // llms-full.txt 用に処理済みの Markdown を残す
            includeProcessedMarkdown: true,
        },
    },
});

export const source = loader({
    source: docs.toFumadocsSource(),
    baseUrl: docsRoute,
    icon(icon) {
        if (icon && icon in icons) return createElement(icons[icon as keyof typeof icons]);
    },
});

// page.url は baseUrl (/) から始まり、Vite の base (BASE_PATH) を含まない
// llms.txt はルーターを通らないため、配置先のサブパスと末尾スラッシュをここで付ける
function toPublicUrl(url: string) {
    return `${import.meta.env.BASE_URL.replace(/\/+$/, "")}${url.replace(/\/+$/, "")}/`;
}

export const docsLlms = llms(source, {
    renderUrl: (node) => toPublicUrl(node.url),
    renderPage: async (page) => `# ${page.data.title} (${toPublicUrl(page.url)})

${await page.data.getText("processed")}`,
});
