"use client";

import { WeatherType } from "@/lib/weather-random";
import ReactECharts from "echarts-for-react";
import { LineChart } from "echarts/charts";
import * as echarts from "echarts/core";
import { weight } from "@/components/core/weather/weather-noise";
type WeatherLineChartProps = {
    data: { x: number; y: number }[];
    size: number;
};

const colorDefinition = [
    { weather: WeatherType.SUNNY, color: "rgb(255, 100, 100)" },
    { weather: WeatherType.CLOUDY, color: "rgb(150, 150, 150)" },
    { weather: WeatherType.RAIN, color: "rgb(100, 150, 255)" },
    { weather: WeatherType.THUNDER, color: "rgb(247, 182, 62)" },
];

const weatherColor = () => {
    const sum = weight.reduce((acc, w) => acc + w.x, 0);

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
