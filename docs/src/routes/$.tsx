import { createFileRoute, notFound } from "@tanstack/react-router";
import { DocsLayout } from "fumadocs-ui/layouts/docs";
import { DocsBody, DocsDescription, DocsPage, DocsTitle } from "fumadocs-ui/layouts/docs/page";
import { Suspense, use } from "react";

import { useMDXComponents } from "@/components/mdx";
import { baseOptions } from "@/lib/layout.shared";
import { appName } from "@/lib/shared";
import { docs, source } from "@/lib/source";

// ドキュメントはサイトのルート (BASE_PATH 直下) に置く。トップページは空の splat (index.mdx) になる
// /api/search.json・/llms.txt・/404 などの個別のルートは、TanStack Router のルートの順位付けによりこの splat より優先される
// 静的サイトとして配信するため、サーバー関数は使わない
// fumadocs-mdx/macro のコレクションはブラウザでも読めるので、ローダーはサーバー (プリレンダー) とクライアントの両方で動く
export const Route = createFileRoute("/$")({
    component: Page,
    loader: async ({ params }) => {
        const slugs = params._splat?.split("/").filter((v) => v.length > 0) ?? [];
        const page = source.getPage(slugs);
        if (!page) throw notFound();

        await docs.getPage(page.path)?.preload();
        return {
            path: page.path,
            title: page.data.title,
            description: page.data.description,
        };
    },
    head: ({ loaderData }) => ({
        meta: loaderData
            ? [
                  { title: `${loaderData.title} | ${appName}` },
                  ...(loaderData.description ? [{ name: "description", content: loaderData.description }] : []),
              ]
            : [],
    }),
});

function Content({ path }: { path: string }) {
    const page = docs.getPage(path);
    if (!page) throw new Error(`unknown page: ${path}`);

    const { toc } = use(page.load());
    const MDX = page.body;

    return (
        <DocsPage toc={toc} tableOfContent={{ style: "clerk" }}>
            <DocsTitle>{page.title}</DocsTitle>
            <DocsDescription>{page.description}</DocsDescription>
            <DocsBody>
                <MDX components={useMDXComponents()} />
            </DocsBody>
        </DocsPage>
    );
}

function Page() {
    const { path } = Route.useLoaderData();

    return (
        <DocsLayout {...baseOptions()} tree={source.getPageTree()}>
            <Suspense>
                <Content path={path} />
            </Suspense>
        </DocsLayout>
    );
}
