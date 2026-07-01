package party.morino.moripafishing.core.world.weather.provider

import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 内蔵 `WeatherRandomizer` の結果を返すプロバイダー。
 *
 * MoripaFishing が定期的に `FishingWorld.setWeather` で Bukkit ワールドに適用するため、
 * 適用された天候（[applyWeather] で更新）を優先的に返し、未適用の場合のみ
 * `WeatherRandomizer.drawWeather()` にフォールバックする。
 *
 * 状態は `getCurrentWeather`（メイン/非同期の双方から呼ばれる）と [applyWeather] の間で
 * 共有されるため `@Volatile` で可視性を担保する。
 */
class InternalWeatherProvider(
    private val weatherRandomizer: WeatherRandomizer,
) : WeatherProvider {
    @Volatile
    private var appliedWeather: WeatherType? = null

    /**
     * MoripaFishing が Bukkit ワールドへ適用した天候をキャッシュする。
     *
     * @return 値が変化した場合は `true`、同一で更新不要な場合は `false`
     */
    fun applyWeather(weatherType: WeatherType): Boolean {
        if (appliedWeather == weatherType) return false
        appliedWeather = weatherType
        return true
    }

    override fun getCurrentWeather(worldId: FishingWorldId): WeatherType = appliedWeather ?: weatherRandomizer.drawWeather()
}
