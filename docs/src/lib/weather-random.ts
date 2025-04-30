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

    // 0-100の乱数を返す
    async get(x: number): Promise<number> {
        //example)check = 100 / 20 = 5
        //input x = 6
        const check = Math.floor(100 / this.maxInclination);
        if (x % check === 0) {
            const random = new Random(await this.getHash(x));
            return random.nextInt(0, 100);
        }
        //違ったら小さい方と大きい方の間で乱数を生成する
        //ex. x = 6, check = 5
        //small = 6 - 6 % 5 = 5
        //large = 6 + (5 - 6 % 5) + 1 = 10
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
        // 重みの合計を計算
        const totalWeight = this.weight.reduce((sum, w) => sum + w.x, 0);
        // 0-100の乱数を重みの合計で割って、0-1の範囲に正規化
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
        // 文字列をUint8Arrayにエンコードする
        const encoder = new TextEncoder();
        const msgUint8 = encoder.encode(message);

        // エンコードされたメッセージのSHA-256ハッシュを計算する
        // ここでWeb Crypto APIを使用している
        const hashBuffer = await crypto.subtle.digest("SHA-256", msgUint8);

        // SHA-256ハッシュの計算結果(hashBuffer)はArrayBufferなので、
        // それをUint8Arrayに変換し、16進数の文字列に変換する
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const hashHex = hashArray
            .map((b) => b.toString(16).padStart(2, "0"))
            .join("");

        // SHA-256ハッシュの16進数の文字列を返す
        return hashHex;
    }
}
