package party.morino.moripafishing.api.config.climate

/**
 * 釣りワールドにおける天候の決定ソースを表す列挙型。
 *
 * `ClimateConfig.weatherMode` で指定し、`WeatherProvider` の切り替えに用いる。
 */
enum class WeatherMode {
    /**
     * プラグイン内蔵の Perlin ノイズランダマイザーで天候を決定し、
     * Bukkit ワールドにも適用する（従来の `enableWeather = true` 相当）。
     */
    INTERNAL,

    /**
     * Bukkit のバニラ天候状態 (`hasStorm` / `isThundering`) を読み取り、
     * 魚の抽選条件にのみ反映する。
     *
     * バニラ天候は `SUNNY` / `RAINY` / `THUNDERSTORM` の3値しか表現できないため、
     * `CLOUDY` / `FOGGY` / `SNOWY` を条件に持つ魚はこのモードでは出現しない。
     */
    VANILLA,

    /**
     * 外部プラグインが `MoripaFishingAPI.registerWeatherProvider` で登録した
     * `WeatherProvider` に天候決定を委譲する。
     *
     * プラグイン側は Bukkit ワールドの天候を変更しない。
     */
    EXTERNAL,
}
