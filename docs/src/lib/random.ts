export class Random {
    x: number;
    y: number;
    z: number;
    w: number;

    constructor(seed = 88675123) {
        this.x = 123456789;
        this.y = 362436069;
        this.z = 521288629;
        this.w = seed;
    }

    // XorShift
    next() {
        const t = this.x ^ (this.x << 11);
        console.log("t: " + t);
        this.x = this.y;
        this.y = this.z;
        this.z = this.w;
        this.w = this.w ^ (this.w >>> 19) ^ (t ^ (t >>> 8));
        console.log(
            "x: " +
                this.x +
                " y: " +
                this.y +
                " z: " +
                this.z +
                " w: " +
                this.w,
        );
        return this.w;
    }

    // min以上max以下の乱数を生成する
    nextInt(min = 0, max = 1) {
        const r = Math.abs(this.next());
        return min + (r % (max + 1 - min));
    }
}
