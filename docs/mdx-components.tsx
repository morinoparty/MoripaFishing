import type { MDXComponents } from "mdx/types";
import defaultComponents from "fumadocs-ui/mdx";
import { Mermaid } from "./components/mdx/mermaind";
import { CommandList } from "./components/commands/command-list";
import { CommandLine } from "./components/commands/command-line";
import { FishMaker } from "./components/core/fish/fish-maker";
import { WeatherNoise } from "./components/core/weather/weather-noise";
import { NumberBarChart } from "./components/chart/number-bar-chart";
import { WeatherLineChart } from "./components/chart/weather-line-chart";
import { AccountsTable, FishingLogsTable, TableDefinition } from "./components/tables";
import { TranslateTable } from "./components/tables/translate-table";
import { TimeStamp } from "./components/faker/timestamp";
import { UUIDV4, UUIDV7 } from "./components/faker/uuid";

const customComponents = {
    CommandList,
    CommandLine,
    FishMaker,
    WeatherNoise,
    NumberBarChart,
    WeatherLineChart,
    AccountsTable,
    FishingLogsTable,
    TableDefinition,
    TranslateTable,
    TimeStamp,
    UUIDV4,
    UUIDV7,
};

export function getMDXComponents(components?: MDXComponents): MDXComponents {
    return {
        ...defaultComponents,
        Mermaid,
        ...customComponents,
        ...components,
    };
}

export function useMDXComponents(components: MDXComponents): MDXComponents {
    return {
        ...defaultComponents,
        Mermaid,
        ...customComponents,
        ...components,
    };
}
