package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.core.world.spi.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/** 常に例外を投げる [WeatherSource]。 */
internal class ThrowingWeatherSource(
    override val key: Key,
) : WeatherSource {
    override val appliesToWorld = true

    override fun getWeather(
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherType = error("weather backend is down")
}
