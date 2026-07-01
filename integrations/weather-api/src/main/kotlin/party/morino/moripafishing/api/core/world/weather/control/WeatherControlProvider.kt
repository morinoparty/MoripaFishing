package party.morino.moripafishing.api.core.world.weather.control

/**
 * 釣りワールドの天候を実際に Bukkit ワールドへ適用する破壊的処理を提供する SPI。
 *
 * MoripaFishing 本体はこのプロバイダーが登録されている場合のみ天候をワールドに適用する。
 * プロバイダー未登録時は天候の適用はスキップされ、コアはワールドを改変しない
 * (天候の決定・参照は引き続きコアが担う)。
 *
 * 本 SPI は **Integration** パターンとして提供され、通常は別 jar
 * (`MoripaFishing-Integration-Weather` 等) の `JavaPlugin` がこれを実装し、
 * コア側は Bukkit の softdepend で実体を検出して利用する。
 *
 * ### 境界を跨ぐ型について
 *
 * 本 SPI は `:api` モジュールに一切依存しない。ワールド ID や天候種別は `String`
 * (天候は `WeatherType` の enum 名) としてやり取りし、classloader の class identity 問題が
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
     * @param worldId 対象ワールドの ID (Bukkit ワールド名と同一)
     * @param weatherType 適用する天候 (`WeatherType` の enum 名)
     */
    fun applyWeather(
        worldId: String,
        weatherType: String,
    )

    /**
     * 指定された釣りワールドに適用済みの天候効果を解除し、ワールドを晴れに戻す。
     *
     * @param worldId 対象ワールドの ID (Bukkit ワールド名と同一)
     */
    fun resetWeather(worldId: String)
}
