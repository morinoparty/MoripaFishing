export class WeatherType {
    static readonly SUNNY = "晴れ";
    static readonly CLOUDY = "曇り";
    static readonly RAIN = "雨";
    static readonly THUNDER = "雷";
}

export class WeatherRandom {
    fishingWorldId: string;
    seed: string;
    weight: { x: number; weather: WeatherType }[];
    maxInclination: number;

    constructor(
        fishingWorldId: string,
        seed: string,
        weight: { x: number; weather: WeatherType }[],
        maxInclination: number,
    ) {
        this.fishingWorldId = fishingWorldId;
        this.seed = seed;
        this.weight = weight;
        this.maxInclination = maxInclination;
    }

    // コアの WeatherCalendar.noise と同じ計算 (Kotlin の Long 演算に合わせて整数除算・floorMod を使う)
    async get(x: number): Promise<number> {
        const check = Math.floor(100 / this.maxInclination);
        const offset = ((x % check) + check) % check;
        if (offset === 0) {
            return this.random(x);
        }
        const small = await this.random(x - offset);
        const large = await this.random(x - offset + check);
        return Math.trunc((large - small) / check) * offset + small;
    }

    // スロットの乱数 (0..100)。SHA-256 の先頭 8 桁を 101 で割った余り
    private async random(x: number): Promise<number> {
        return (await this.getHash(x)) % 101;
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
            this.fishingWorldId + this.seed + x.toString(),
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
