import React from "react";
import { Input } from "@site/src/components/ui/input";
import { Label } from "@site/src/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@site/src/components/ui/select";
import { atom, useAtom, useAtomValue } from "jotai";

type Rarity = "common" | "rare" | "epic" | "legendary" | "mythic" | "junk";

const sizeAtom = atom<[number, number]>([20, 60]);
const raretyExpressionAtom = atom<[Rarity, string][]>([
    ["common", "(<length_rate> ** 2) * 5 + 5"],
    ["rare", "(<length_rate> ** 2) * 10 + 10"],
    ["epic", "(<length_rate> ** 2) * 50 + 100"],
    ["legendary", "(<length_rate> ** 2) * 1000 + 500"],
    ["mythic", "(<length_rate> ** 2) * 2000 + 2000"],
    ["junk", "1"],
]);

const rarityAtom = atom<Rarity>("common");

export const FishMaker = () => {
    return (
        <>
            <RadioRarety />
            <SizeSelector />
            <DisplayScore />
        </>
    );
};

const RadioRarety = () => {
    const [rarity, setRarity] = useAtom(rarityAtom);
    return (
        <div className="pb-4">
            <Label className="text-md">レアリティ</Label>
            <Select onValueChange={(value) => {
                setRarity(value as Rarity)
                console.log(value)
            }}>
                <SelectTrigger className="w-[180px]">
                    <SelectValue
                        className="text-gray-500"
                        placeholder="Rarity"
                    />
                </SelectTrigger>
                <SelectContent  className="bg-white dark:bg-[var(--ifm-background-color)]">
                    {useAtomValue(raretyExpressionAtom).map(([rarity, value]) => (
                            <SelectItem key={rarity} value={rarity}>{rarity}</SelectItem>
                    ))}
                </SelectContent>
                </Select>
        </div>
    );
};

const SizeSelector = () => {
    const [size, setSize] = useAtom(sizeAtom);
    return (
        <div className="pb-4">
            <Label className="text-md">サイズ</Label>
            <div className="flex flex-row gap-6 pt-2">
                <Input type="number" value={size[0]} onChange={(e) => setSize([Number(e.target.value), size[1]])} />
                <Input type="number" value={size[1]} onChange={(e) => setSize([size[0], Number(e.target.value)])} />
            </div>
        </div>
    );
};

const DisplayScore = () => {
    const [min, max] = useAtomValue(sizeAtom);
    const rarity = useAtomValue(rarityAtom);
    const rarityExpressions = useAtomValue(raretyExpressionAtom);
    const list = [];
    const step = (max - min) / 10; // ステップサイズを計算
    for (let i = min; i <= max; i += step) {
        list.push(Number((i).toFixed(2))); // 小数点以下2桁に丸めてリストに追加
    }

    const mid = (min + max) / 2;

    const standardDeviation = (max - min) / 6;

    // エラーファンクション（erf）を計算する関数
    const erf = (x: number): number => {
        // Abramowitz and Stegun approximation
        const a1 = 0.254829592;
        const a2 = -0.284496736;
        const a3 = 1.421413741;
        const a4 = -1.453152027;
        const a5 = 1.061405429;
        const p = 0.3275911;

        const sign = x < 0 ? -1 : 1;
        x = Math.abs(x);

        const t = 1.0 / (1.0 + p * x);
        const y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    };

    // 正規分布の累積分布関数（CDF）を計算
    // xがN(mid, standardDeviation)に従うとき、xが下位何パーセントに入るかを返す
    const cdf = (x: number): number => {
        const z = (x - mid) / (standardDeviation * Math.sqrt(2));
        return (1 + erf(z)) / 2;
    };

    const expression = rarityExpressions.find(([r, expression]) => r === rarity)[1];
    const score : number[] = list.map((size) => {
        //evalで計算
        console.log(size, cdf(size))
        return eval(expression.replace("<length_rate>", cdf(size).toString()));
    });
    
    return (
        <>
        {expression}
        <table className="w-2/3 pt-4">
            <thead className="bg-gray-100">
                <tr>
                    <th className="w-xs">サイズ</th>
                    <th className="w-xs">スコア </th>
                    <th className="w-xs">CDF 累積分布関数</th>
                </tr>
            </thead>
            <tbody>
                {list.map((size, index) => {
                    const cdfValue = cdf(size)
                    return (
                        <tr key={size}>
                            <td className="w-xs text-right">{size.toFixed(2)} cm</td>
                            <td className="w-xs text-right">{score[index].toFixed(2)}</td>
                            <td className="w-xs text-right">{Number(cdfValue * 100).toFixed(2)} %</td>
                        </tr>
                    );
                })}
            </tbody>
        </table>
        </>
    );
};
