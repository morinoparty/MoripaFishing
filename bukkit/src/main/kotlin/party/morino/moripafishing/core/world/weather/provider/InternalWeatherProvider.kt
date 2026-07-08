package party.morino.moripafishing.core.world.weather.provider

import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 内蔵 `WeatherRandomizer` の結果を返すプロバイダー。
 *
 * `WeatherRandomizer.drawWeather()` は時刻ハッシュに基づき決定的なため、
 * 呼び出しごとに現在の天候カレンダー値を返すだけでよい。
 * Bukkit ワールドへの適用と重複適用の抑制は `FishingWorldImpl.setWeather` 側が担う。
 */
class InternalWeatherProvider(
    private val weatherRandomizer: WeatherRandomizer,
) : WeatherProvider {
    override fun getCurrentWeather(worldId: FishingWorldId): WeatherType = weatherRandomizer.drawWeather()
}
