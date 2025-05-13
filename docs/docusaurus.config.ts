import type * as Preset from "@docusaurus/preset-classic";
import type { Config } from "@docusaurus/types";
import { themes as prismThemes } from "prism-react-renderer";

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
    title: "MoripaFishing Documentation",
    // favicon: "img/favicon.svg",
    staticDirectories: ["static"],
    trailingSlash: true,

    url: "https://fishing.plugin.morino.party",
    baseUrl: "/",

    organizationName: "morinoparty",
    projectName: "MoripaFishing",

    onBrokenLinks: "throw",
    onBrokenMarkdownLinks: "warn",

    i18n: {
        defaultLocale: "ja",
        locales: ["ja"],
    },
    markdown: {
        mermaid: true,
    },
    themes: ["@docusaurus/theme-mermaid"],
    presets: [
        [
            "classic",
            {
                docs: {
                    sidebarPath: "./sidebars.ts",
                    sidebarCollapsed: true,
                    routeBasePath: "",
                    editUrl:
                        "https://github.com/morinoparty/moripafishing/tree/master/docs/",
                },
                theme: {
                    customCss: "./src/css/custom.css",
                },
            } satisfies Preset.Options,
        ],
    ],
    plugins: [
        ["./src/plugins/tailwind-config.js", {}],
        ["./src/plugins/llms-txt.ts", {}],
        [
            require.resolve("@easyops-cn/docusaurus-search-local"),
            {
                indexDocs: true,
                language: "ja",
                docsRouteBasePath: "/",
            },
        ],
    ],
    themeConfig: {
        image: "img/docusaurus-social-card.jpg",
        mermaid: {
            theme: { light: "forest", dark: "dark" },
        },
        navbar: {
            title: "MoripaFishing",
            // logo: {
            //     alt: "MoripaFishing Logo",
            //     src: "img/favicon.svg",
            // },
            items: [
                {
                    href: "https://github.com/morinoparty/moripafishing",
                    label: "GitHub",
                    position: "right",
                },
                {
                    href: "https://modrinth.com/project/moripafishing",
                    label: "Modrinth",
                    position: "right",
                },
                {
                    href: "/dokka",
                    label: "Dokka",
                    position: "left",
                    target: "_blank",
                },
                // {
                //     href: "/javadoc",
                //     label: "Javadoc",
                //     position: "left",
                //     target: "_blank",
                // },
            ],
        },
        footer: {
            style: "dark",
            links: [
                {
                    title: "ドキュメント",
                    items: [
                        {
                            label: "はじめに",
                            to: "/intro",
                        },
                    ],
                },
                {
                    title: "コミュニティ",
                    items: [
                        {
                            label: "ホームページ",
                            href: "https://morino.party",
                        },
                        {
                            label: "Discord",
                            href: "https://discord.com/invite/9HdanPM",
                        },
                        {
                            label: "X",
                            href: "https://x.com/morinoparty",
                        },
                    ],
                },
                {
                    title: "その他",
                    items: [
                        {
                            label: "GitHub",
                            href: "https://github.com/morinoparty/moripafishing",
                        },
                    ],
                },
            ],
            copyright: `No right reserved. This docs under CC0. Built with Docusaurus.`,
        },
        prism: {
            additionalLanguages: [
                "java",
                "groovy",
                "diff",
                "toml",
                "yaml",
                "kotlin",
            ],
            theme: prismThemes.github,
            darkTheme: prismThemes.dracula,
        },
    } satisfies Preset.ThemeConfig,
    future: {
        experimental_faster: true,
    },
};

export default config;
