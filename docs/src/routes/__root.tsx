import { createRootRoute, HeadContent, Outlet, Scripts } from "@tanstack/react-router";
import { RootProvider } from "fumadocs-ui/provider/tanstack";

import SearchDialog from "@/components/search";

import appCss from "@/styles/app.css?url";
import tokensCss from "@/styles/tokens.css?url";

export const Route = createRootRoute({
    head: () => ({
        meta: [
            { charSet: "utf-8" },
            { name: "viewport", content: "width=device-width, initial-scale=1" },
            { title: "MoripaFishing Documentation" },
            {
                name: "description",
                content: "Minecraft fishing plugin with weather system, custom fish, and rarity management",
            },
        ],
        links: [
            { rel: "icon", type: "image/svg+xml", href: `${import.meta.env.BASE_URL}favicon.svg` },
            // Tailwind のレイヤー順を先に確定させるため、app.css を tokens.css より前に読み込む
            { rel: "stylesheet", href: appCss },
            { rel: "stylesheet", href: tokensCss },
            // Satoshi (欧文) と源柔ゴシック (和文)
            { rel: "stylesheet", href: "https://api.fontshare.com/v2/css?f[]=satoshi@1&display=swap" },
            {
                rel: "stylesheet",
                href: "https://shogo82148.github.io/genjyuugothic-subsets/GenJyuuGothicL-P-Medium/GenJyuuGothicL-P-Medium.css",
            },
            {
                rel: "stylesheet",
                href: "https://shogo82148.github.io/genjyuugothic-subsets/GenJyuuGothicL-P-Bold/GenJyuuGothicL-P-Bold.css",
            },
        ],
    }),
    component: RootComponent,
});

function RootComponent() {
    return (
        <html lang="ja" data-color-palette="umi" suppressHydrationWarning>
            <head>
                <HeadContent />
            </head>
            <body className="flex flex-col min-h-screen">
                {/* ダークモードは提供しないため、テーマ (next-themes 相当) を無効にしてライト固定にする */}
                <RootProvider search={{ SearchDialog }} theme={{ enabled: false }}>
                    <Outlet />
                </RootProvider>
                <Scripts />
            </body>
        </html>
    );
}
