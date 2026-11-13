export const appName = "MoripaFishing";
// ドキュメントはサイトのルートに置く (トップページがドキュメントの index になる)
export const docsRoute = "/";

export const gitConfig = {
    user: "morinoparty",
    repo: "MoripaFishing",
    branch: "main",
};

export const modrinthUrl = "https://modrinth.com/plugin/moripafishing";

// BASE_URL は Vite の base (例: /moripafishing/<sha>/docs/、GitHub Pages のカスタムドメインなら /)。静的ファイルの URL には base を付ける
export const searchIndexUrl = `${import.meta.env.BASE_URL}api/search.json`;
