import { createPreset, stone } from "@morinoparty/chlorophyll-react/preset";
import { defineConfig, defineGlobalStyles } from "@pandacss/dev";
import type { Preset } from "@pandacss/types";

// chlorophyll のプリセットからデザイントークン (CSS 変数) だけを使う
// コンポーネントは fumadocs-ui のものを使うため、レシピは出力しない
const chlorophyll = createPreset({ brandColor: "mori", grayColor: stone, radius: "md" });
const preset: Preset = {
    ...chlorophyll,
    theme: {
        ...chlorophyll.theme,
        extend: Object.fromEntries(
            Object.entries(chlorophyll.theme?.extend ?? {}).filter(
                ([key]) => key !== "recipes" && key !== "slotRecipes",
            ),
        ),
    },
};

// colorPalette.* (--mpc-colors-color-palette-*) を mori に向ける。data-color-palette で切り替えられるようにする (MoriPath と同じ)
const globalCss = defineGlobalStyles({
    ":root": {
        colorPalette: "mori",
    },
    ':root[data-color-palette="umi"]': {
        colorPalette: "umi",
    },
});

export default defineConfig({
    // リセット CSS は Tailwind (fumadocs-ui) 側のものを使う
    preflight: false,
    // ユーティリティは使わないため、ソースを走査しない
    include: [],
    // CSS 変数名を --mpc-colors-mori-* にする
    prefix: "mpc",
    presets: [preset],
    globalCss,
    outdir: "styled-system",
});
