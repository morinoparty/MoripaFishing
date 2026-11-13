import { use, useId, useSyncExternalStore } from "react";

// fumadocs のドキュメント (Markdown > Mermaid) にある公式レンダラーの実装
export function Mermaid({ chart }: { chart: string }) {
    // サーバーとハイドレーション中は false
    const isClient = useSyncExternalStore(
        () => () => {},
        () => true,
        () => false,
    );

    if (!isClient) return;
    return <MermaidContent chart={chart} />;
}

const cache = new Map<string, Promise<unknown>>();

function cachePromise<T>(key: string, setPromise: () => Promise<T>): Promise<T> {
    const cached = cache.get(key);
    if (cached) return cached as Promise<T>;

    const promise = setPromise();
    cache.set(key, promise);
    return promise;
}

// <html> の class (dark) の変化を監視してテーマに追従する
function subscribeTheme(onChange: () => void) {
    const observer = new MutationObserver(onChange);
    observer.observe(document.documentElement, { attributeFilter: ["class"] });
    return () => observer.disconnect();
}

function MermaidContent({ chart }: { chart: string }) {
    const id = useId();
    const theme = useSyncExternalStore(subscribeTheme, () =>
        document.documentElement.classList.contains("dark") ? "dark" : "default",
    );
    const { default: mermaid } = use(cachePromise("mermaid", () => import("mermaid")));

    mermaid.initialize({
        startOnLoad: false,
        securityLevel: "loose",
        fontFamily: "inherit",
        themeCSS: "margin: 1.5rem auto 0;",
        theme,
    });

    const { svg, bindFunctions } = use(
        cachePromise(`${chart}-${theme}`, () => {
            return mermaid.render(id, chart.replaceAll("\\n", "\n"));
        }),
    );

    return (
        <div
            ref={(container) => {
                if (container) bindFunctions?.(container);
            }}
            dangerouslySetInnerHTML={{ __html: svg }}
        />
    );
}
