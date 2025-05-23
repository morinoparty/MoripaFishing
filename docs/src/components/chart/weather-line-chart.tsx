import { WeatherType } from "@site/src/lib/weather-random";
import ReactECharts from "echarts-for-react";
import { LineChart } from "echarts/charts";
import * as echarts from "echarts/core";
import { weight } from "../core/weather/weather-noise";
type WeatherLineChartProps = {
    data: { x: number; y: number }[];
    size: number;
};

const colorDefinition = [
    { weather: WeatherType.SUNNY, color: "rgb(255, 100, 100)" }, // 晴れ: 赤
    { weather: WeatherType.CLOUDY, color: "rgb(150, 150, 150)" }, // 曇り: グレー
    { weather: WeatherType.RAIN, color: "rgb(100, 150, 255)" }, // 雨: 青
    { weather: WeatherType.THUNDER, color: "rgb(247, 182, 62)" }, // 雷: 黄色
];

const weatherColor = () => {
    // 重みの合計を計算
    const sum = weight.reduce((acc, w) => acc + w.x, 0);

    // 各天気の色と範囲を計算
    const colors = weight.map((w, index) => {
        const min =
            index === 0
                ? 0
                : (weight.slice(0, index).reduce((acc, w) => acc + w.x, 0) /
                      sum) *
                  100;
        const max =
            (weight.slice(0, index + 1).reduce((acc, w) => acc + w.x, 0) /
                sum) *
            100;
        const color =
            colorDefinition.find((c) => c.weather === w.weather)?.color ||
            "oklch(0.71 0.1598 116.47)";
        return { min, max, color };
    });

    return colors;
};

export const WeatherLineChart = ({ data, size }: WeatherLineChartProps) => {
    const option = {
        xAxis: {
            type: "category",
            data: data.map((d) => d.x.toString()),
        },
        yAxis: {
            type: "value",
        },
        visualMap: {
            pieces: weatherColor(),
        },
        series: [
            {
                data: data.map((d) => d.y),
                type: "line",
                smooth: true,
            },
        ],
    };

    echarts.use([LineChart]);

    return <ReactECharts echarts={echarts} option={option} />;
};
