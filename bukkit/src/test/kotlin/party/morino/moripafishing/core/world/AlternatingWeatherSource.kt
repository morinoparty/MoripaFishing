package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.core.world.spi.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/** 呼ばれるたびに交互の天候を返し、呼び出し回数を数える [WeatherSource]。 */
internal class AlternatingWeatherSource(
    override val key: Key,
) : WeatherSource {
    override val appliesToWorld = true
    var calls = 0

    override fun getWeather(
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherType = if (calls++ % 2 == 0) WeatherType.RAINY else WeatherType.SUNNY
}
