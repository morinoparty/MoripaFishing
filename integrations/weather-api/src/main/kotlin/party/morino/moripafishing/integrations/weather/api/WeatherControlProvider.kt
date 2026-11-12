package party.morino.moripafishing.integrations.weather.api

/**
 * 釣りワールドの天候を実際に Bukkit ワールドへ適用する破壊的処理を提供する SPI。
 *
 * MoripaFishing 本体はこのプロバイダーが登録されている場合のみ天候をワールドに適用する。
 * 天候 (`setStorm` / `isThundering`) に加え、ゲームルール `DO_WEATHER_CYCLE` の変更も本 SPI が担う。
 * プロバイダー未登録時は天候・ゲームルールの適用はスキップされ、コアはワールドを改変しない
 * (天候の決定・参照は引き続きコアが担う)。
 *
 * 本 SPI は **Integration** パターンとして提供され、通常は別 jar
 * (`MoripaFishing-Integration-Weather` 等) の `JavaPlugin` がこれを実装し、
 * コア側は Bukkit の softdepend で実体を検出して利用する。
 *
 * ### 境界を跨ぐ型について
 *
 * 本 SPI は `:api` モジュールに一切依存しない。ワールドキーや天候種別は `String`
 * (ワールドは Bukkit ワールドのキー文字列 (`moripafishing:<worldId>`)、天候は `WeatherType` の enum 名)
 * としてやり取りし、classloader の class identity 問題が
 * 発生する余地をなくしている。
 *
 * ### CLOUDY の扱い
 *
 * `CLOUDY` は嵐 (`setStorm(true)`) として適用しつつ、各プレイヤーの頭上にクライアント側だけの
 * バリア天井を `sendBlockChange` で「見せる」ことで、実ブロックもバイオームも改変せずに
 * 「雨の当たらない曇り空」を表現する。草・水・空の色は一切変化しない。
 */
interface WeatherControlProvider {
    /**
     * 指定された釣りワールドへ天候を適用する。
     *
     * 既に同じ天候が適用済みかどうかの判定はコア側が行うため、本メソッドは
     * 呼ばれたら必ず適用してよい。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param weatherType 適用する天候 (`WeatherType` の enum 名)
     */
    fun applyWeather(
        worldKey: String,
        weatherType: String,
    )

    /**
     * 指定された釣りワールドに適用済みの天候効果を解除し、ワールドを晴れに戻す。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     */
    fun resetWeather(worldKey: String)

    /**
     * 指定された釣りワールドのバニラ天候サイクル (ゲームルール `DO_WEATHER_CYCLE`) を設定する。
     *
     * コアは `ClimateConfig.applyWeatherEffects` が有効な場合、天候ソースの
     * `usesVanillaWeatherCycle` に従った値を渡す。`applyWeatherEffects` を無効化した際は、
     * ワールドデータに強制値が残らないよう一度だけ `true` (バニラ既定値) を渡す。
     *
     * 既定実装は何もしない。本メソッドを持たない旧版の実装との互換性のためであり、
     * 実装側はゲームルールを変更する場合にオーバーライドする。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param enabled バニラの天候サイクルを有効にする場合 `true`
     */
    fun setWeatherCycle(
        worldKey: String,
        enabled: Boolean,
    ) {
        // 既定では何もしない
    }
}
