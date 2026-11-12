package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.core.world.spi.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/** 固定の天候を返す [WeatherSource]。 */
internal class FakeWeatherSource(
    override val key: Key,
    override val appliesToWorld: Boolean,
    var weather: WeatherType,
) : WeatherSource {
    override fun getWeather(
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherType = weather
}
