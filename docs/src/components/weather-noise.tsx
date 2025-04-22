import { WeatherRandom, WeatherType } from "../lib/weather-random";
import { NumberBarChart } from "./chart/number-bar-chart";
import { WeatherLineChart } from "./chart/weather-line-chart";
import React, { useEffect, useState } from "react";
import { Input } from "./ui/input";
import { Label } from "./ui/label";

export const weight: { x: number, weather: WeatherType }[] = [
    { x: 4, weather: WeatherType.SUNNY },
    { x: 2, weather: WeatherType.CLOUDY },
    { x: 2, weather: WeatherType.RAIN },
    { x: 1, weather: WeatherType.THUNDER },
];

export const WeatherNoise = () => {
    const [data, setData] = useState<{ x: number, y: number }[]>([]);
    const [weatherData, setWeatherData] = useState<{ x: number, y: number }[]>([]);
    const [world, setWorld] = useState<string>("default");
    const [papper, setPapper] = useState<string>("pepper");
    const base : number = getTimeDiff()
    console.log(base)
    const [size, setSize] = useState<number>(100);
    const maxInclination = 30;


    useEffect(() => {
        const fetchData = async () => {
            // シード値を文字列から数値に変換する
            const weatherRandom: WeatherRandom = new WeatherRandom(world , papper, weight, maxInclination);
            const newData = await Promise.all(
                Array.from({ length: size }, async (_, i) => ({
                    x: i,
                    y: await weatherRandom.get(i + base)
                }))
            );
            setData(newData);
            const newWeatherData = await Promise.all(
                Array.from({ length: size }, async (_, i) => ({
                    x: i,
                    y: (await weatherRandom.getWeather(i + base)).toString()
                }))
            );
            setWeatherData(newWeatherData);
        };

        fetchData();
    }, [world, papper, size]); // sizeを依存配列に追加

    return (
        <>
            <div className="flex flex-row gap-4">
                <div className="flex flex-col gap-2">
                    <Label className="text-md">世界</Label>
                    <Input placeholder="world" onChange={(e) => setWorld(e.target.value)} defaultValue={world} />
                </div>
                <div className="flex flex-col gap-2">
                    <Label className="text-md">pepper</Label>
                    <Input placeholder="pepper" onChange={(e) => setPapper(e.target.value)} defaultValue={papper} />
                </div>
                <div className="flex flex-col gap-2">
                    <Label className="text-md">サイズ(1年でsize={365 * 3})</Label>
                    <Input placeholder="size" type="number" onChange={(e) => setSize(parseInt(e.target.value))} defaultValue={size} />
                </div>
            </div>
            <NumberBarChart data={weatherData} size={size} />
            <WeatherLineChart data={data} size={size} />
            <NumberBarChart data={data} size={size} />
        </>
    );
};

const getTimeDiff = () => {
    const timeZone = "Asia/Tokyo";
    const now = new Date();
    const base = new Date("2023-10-01T00:00:00+09:00");
    const nowInTokyo = new Date(now.toLocaleString("en-US", { timeZone }));
    return Math.floor((nowInTokyo.getTime() - base.getTime()) / 1000 / 3600 / 8);
}