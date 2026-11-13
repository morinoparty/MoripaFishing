import ReactEChartsModule from "echarts-for-react";

// プリレンダー (Node) では CommonJS 版が読み込まれ、default import が { default } のオブジェクトになるため取り出す
export const ReactECharts =
    (ReactEChartsModule as unknown as { default?: typeof ReactEChartsModule }).default ?? ReactEChartsModule;
