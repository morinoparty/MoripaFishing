import defaultMdxComponents from "fumadocs-ui/mdx";
import type { MDXComponents } from "mdx/types";

import { NumberBarChart } from "./chart/number-bar-chart";
import { WeatherLineChart } from "./chart/weather-line-chart";
import { CommandLine } from "./commands/command-line";
import { CommandList } from "./commands/command-list";
import { FishMaker } from "./core/fish/fish-maker";
import { WeatherNoise } from "./core/weather/weather-noise";
import { Mermaid } from "./mdx/mermaid";
import { TranslateTable } from "./tables/translate-table";

export function getMDXComponents(components?: MDXComponents) {
    return {
        ...defaultMdxComponents,
        Mermaid,
        CommandList,
        CommandLine,
        FishMaker,
        WeatherNoise,
        NumberBarChart,
        WeatherLineChart,
        TranslateTable,
        ...components,
    } satisfies MDXComponents;
}

export const useMDXComponents = getMDXComponents;

declare global {
    type MDXProvidedComponents = ReturnType<typeof getMDXComponents>;
}
