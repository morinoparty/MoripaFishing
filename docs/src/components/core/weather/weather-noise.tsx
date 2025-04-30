import { useEffect, useState } from "react";
import { WeatherRandom, WeatherType } from "../../../lib/weather-random";
import { NumberBarChart } from "../../chart/number-bar-chart";
import { WeatherLineChart } from "../../chart/weather-line-chart";
import { Input } from "../../ui/input";
import { Label } from "../../ui/label";

export const defaultWeight: { x: number; weather: WeatherType }[] = [
    { x: 5, weather: WeatherType.SUNNY },
    { x: 1, weather: WeatherType.CLOUDY },
    { x: 2, weather: WeatherType.RAIN },
    { x: 1, weather: WeatherType.THUNDER },
];

export const weight = [...defaultWeight];

export const WeatherNoise = () => {
    const [data, setData] = useState<{ x: number; y: number }[]>([]);
    const [weatherData, setWeatherData] = useState<{ x: number; y: number }[]>(
        [],
    );
    const [world, setWorld] = useState<string>("default");
    const [pepper, setPepper] = useState<string>("pepper");
    const base: number = getTimeDiff();
    const [size, setSize] = useState<number>(400);
    const maxInclination = 30;
    const [weights, setWeights] = useState<{ x: number; weather: WeatherType }[]>(defaultWeight);

    useEffect(() => {
        const fetchData = async () => {
            // シード値を文字列から数値に変換する
            const weatherRandom: WeatherRandom = new WeatherRandom(
                world,
                pepper,
                weights,
                maxInclination,
            );
            const newData = await Promise.all(
                Array.from({ length: size }, async (_, i) => ({
                    x: i,
                    y: await weatherRandom.get(i + base),
                })),
            );
            setData(newData);
            const newWeatherData = await Promise.all(
                Array.from({ length: size }, async (_, i) => ({
                    x: i,
                    y: (await weatherRandom.getWeather(i + base)).toString(),
                })),
            );
            setWeatherData(newWeatherData);
        };

        fetchData();
    }, [world, pepper, size, weights]); // weightsを依存配列に追加

    const handleWeightChange = (index: number, value: number) => {
        const newWeights = [...weights];
        newWeights[index].x = value;
        setWeights(newWeights);
    };

    return (
        <>
            <div className="grid grid-cols-3 gap-4">
                <div className="flex flex-col gap-2">
                    <Label className="text-md">世界</Label>
                    <Input
                        placeholder="world"
                        onChange={(e) => setWorld(e.target.value)}
                        defaultValue={world}
                    />
                </div>
                <div className="flex flex-col gap-2">
                    <Label className="text-md">pepper</Label>
                    <Input
                        placeholder="pepper"
                        onChange={(e) => setPepper(e.target.value)}
                        defaultValue={pepper}
                    />
                </div>
                <div className="flex flex-col gap-2">
                    <Label className="text-md">
                        サイズ(1年でsize={365 * 3})
                    </Label>
                    <Input
                        placeholder="size"
                        type="number"
                        onChange={(e) =>
                            setSize(Number.parseInt(e.target.value))
                        }
                        defaultValue={size}
                    />
                </div>
            </div>

            <div className="mt-4 mb-4">
                <Label className="text-md block mb-2">天気の重み設定</Label>
                <div className="grid grid-cols-4 gap-4">
                    {weights.map((w, index) => (
                        <div key={index} className="flex flex-col gap-2">
                            <Label className="text-sm">{w.weather}</Label>
                            <Input
                                type="number"
                                min="0"
                                value={w.x}
                                onChange={(e) => handleWeightChange(index, Number(e.target.value))}
                            />
                        </div>
                    ))}
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
    return Math.floor(
        (nowInTokyo.getTime() - base.getTime()) / 1000 / 3600 / 8,
    );
};