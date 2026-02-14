import { Random } from "./random";

export class WeatherType {
    static readonly SUNNY = "晴れ";
    static readonly CLOUDY = "曇り";
    static readonly RAIN = "雨";
    static readonly THUNDER = "雷";
}

export class WeatherRandom {
    fishingWorldId: string;
    pepper: string;
    weight: { x: number; weather: WeatherType }[];
    maxInclination: number;

    constructor(
        fishingWorldId: string,
        pepper: string,
        weight: { x: number; weather: WeatherType }[],
        maxInclination: number,
    ) {
        this.fishingWorldId = fishingWorldId;
        this.pepper = pepper;
        this.weight = weight;
        this.maxInclination = maxInclination;
    }

    async get(x: number): Promise<number> {
        const check = Math.floor(100 / this.maxInclination);
        if (x % check === 0) {
            const random = new Random(await this.getHash(x));
            return random.nextInt(0, 100);
        }
        const small = new Random(await this.getHash(x - (x % check))).nextInt(
            0,
            100,
        );
        const large = new Random(
            await this.getHash(x - (x % check) + check),
        ).nextInt(0, 100);
        return ((large - small) / check) * (x % check) + small;
    }

    async getWeather(x: number): Promise<WeatherType> {
        const random = await this.get(x);
        const totalWeight = this.weight.reduce((sum, w) => sum + w.x, 0);
        const normalizedRandom = (random / 100) * totalWeight;

        let acc = 0;
        for (const w of this.weight) {
            acc += w.x;
            if (normalizedRandom < acc) {
                return w.weather;
            }
        }
        return this.weight[this.weight.length - 1].weather;
    }

    private async getHash(x: number) {
        const hash = await this.sha256(
            this.fishingWorldId + this.pepper + x.toString(),
        );
        return Number.parseInt(hash.slice(0, 8), 16);
    }

    private async sha256(message: string) {
        if (typeof globalThis.crypto?.subtle !== "undefined") {
            const encoder = new TextEncoder();
            const msgUint8 = encoder.encode(message);
            const hashBuffer = await crypto.subtle.digest("SHA-256", msgUint8);
            const hashArray = Array.from(new Uint8Array(hashBuffer));
            return hashArray
                .map((b) => b.toString(16).padStart(2, "0"))
                .join("");
        }
        const { createHash } = await import("node:crypto");
        return createHash("sha256").update(message).digest("hex");
    }
}
