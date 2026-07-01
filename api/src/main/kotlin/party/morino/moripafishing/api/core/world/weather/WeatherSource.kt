package party.morino.moripafishing.api.core.world.weather

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りワールドの天候を決定するソースを表す SPI。
 *
 * `ClimateConfig.weatherSource` の [Key] で選択され、`WeatherSourceRegistry` に登録される。
 * MoripaFishing 本体が [INTERNAL] / [VANILLA] を提供し、外部プラグインは
 * `MoripaFishingAPI.registerWeatherSource` で独自の [Key] を持つ実装を登録できる。
 *
 * 天候の「消費」（魚の抽選条件）はコアが担い、天候の「決定」と「適用」だけがこの SPI で差し替わる。
 */
interface WeatherSource {
    /**
     * このソースを一意に識別する名前空間キー。`ClimateConfig.weatherSource` と突き合わせて解決される。
     */
    val key: Key

    /**
     * MoripaFishing がこのソースの天候を Bukkit ワールドへ適用・駆動するかどうか。
     *
     * `true` の場合、プラグインが定期的に天候を決定して `FishingWorld.setWeather` で適用し、
     * バニラの天候サイクル (`DO_WEATHER_CYCLE`) を無効化する（内蔵ランダマイザー相当）。
     * `false` の場合、プラグインはワールドの天候を変更しない。
     */
    val managesWorldWeather: Boolean

    /**
     * バニラの天候サイクル (`DO_WEATHER_CYCLE`) を有効に保つかどうか。
     *
     * `true` の場合、Bukkit のバニラ天候を読み取ることを前提とするためサイクルを止めない。
     */
    val usesVanillaWeatherCycle: Boolean

    /**
     * 指定された釣りワールドの現在天候を返す [WeatherProvider] を生成する。
     *
     * ワールドごとに1度だけ呼ばれることを想定する（[WeatherProvider] は再利用される）。
     */
    fun createProvider(worldId: FishingWorldId): WeatherProvider

    companion object {
        /**
         * 内蔵ランダマイザーで天候を決定し Bukkit ワールドにも適用する組み込みソースのキー。
         */
        val INTERNAL: Key = Key.key("moripafishing", "internal")

        /**
         * Bukkit のバニラ天候状態を読み取り、魚の抽選条件にのみ反映する組み込みソースのキー。
         */
        val VANILLA: Key = Key.key("moripafishing", "vanilla")
    }
}
