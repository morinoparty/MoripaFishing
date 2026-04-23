package party.morino.moripafishing.core.world.weather.provider

import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 内蔵 `WeatherRandomizer` の結果をそのまま返すプロバイダー。
 *
 * MoripaFishing が定期的に `FishingWorld.setWeather` で Bukkit ワールドに適用するため、
 * キャッシュされた適用後の値（`currentWeatherSupplier`）を優先的に返し、
 * 未適用の場合のみ `WeatherRandomizer.drawWeather()` にフォールバックする。
 */
class InternalWeatherProvider(
    private val weatherRandomizer: WeatherRandomizer,
    private val currentWeatherSupplier: () -> WeatherType?,
) : WeatherProvider {
    override fun getCurrentWeather(worldId: FishingWorldId): WeatherType =
        currentWeatherSupplier() ?: weatherRandomizer.drawWeather()
}
