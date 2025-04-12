import { Input } from "@site/src/components/ui/input";
import { Label } from "@site/src/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@site/src/components/ui/select";
import { useState } from "react";

const defaultFish = {
    caughtCommands: [],
    rarity: "common",
    displayName: {
        en_US: "",
        ja_JP: "",
    },
    worthExpression: "",
    scoreExpression: "",
    icon: "",
    itemStack: {
        itemMeta: {
            customModelData: 0,
        },
    },
    key: "",
    lengthMax: 0,
    lengthMin: 0,
    quality: 0,
    rarityKey: "",
    weight: 0,
};

export const FishMaker = () => {
    const [fish, setFish] = useState(defaultFish);
    return (
        <>
            <RadioRarety />
            <div className="grid w-full max-w-md items-center gap-1.5">
                <Label className="text-md">魚を捕まえたときのコマンド</Label>
                {fish.caughtCommands.map((command, index) => (
                    <Input
                        key={index}
                        type="name"
                        placeholder="moripa_fishing add-fatigue <angler> 10"
                        value={command}
                        onChange={(e) => {
                            setFish({
                                ...fish,
                                caughtCommands: [e.target.value],
                            });
                        }}
                    />
                ))}
                <Input
                    type="name"
                    placeholder="moripa_fishing add-fatigue <angler> 10 (クリックで追加)"
                    onClick={() => {
                        setFish({
                            ...fish,
                            caughtCommands: [...fish.caughtCommands, ""],
                        });
                    }}
                />
            </div>
        </>
    );
};

const RadioRarety = () => {
    const rarity = ["common", "rare", "epic", "legendary", "mythic", "junk"];
    return (
        <div className="pb-4">
            <Label className="text-md">レアリティ</Label>
            <Select>
                <SelectTrigger className="w-[180px]">
                    <SelectValue
                        className="text-gray-500"
                        placeholder="Rarity"
                    />
                </SelectTrigger>
                <SelectContent className="bg-white dark:bg-[var(--ifm-background-color)]">
                    {rarity.map((rarity) => (
                        <SelectItem value={rarity}>{rarity}</SelectItem>
                    ))}
                </SelectContent>
            </Select>
        </div>
    );
};
