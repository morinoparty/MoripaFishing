package party.morino.moripafishing.core.world.weather.source

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.world.weather.provider.InternalWeatherProvider

/**
 * 内蔵ランダマイザーで天候を決定し、Bukkit ワールドにも適用する組み込みソース（`moripafishing:internal`）。
 */
class InternalWeatherSource(
    private val randomizeManager: RandomizeManager,
) : WeatherSource {
    override val key: Key = WeatherSource.INTERNAL
    override val managesWorldWeather: Boolean = true
    override val usesVanillaWeatherCycle: Boolean = false

    override fun createProvider(worldId: FishingWorldId): WeatherProvider =
        InternalWeatherProvider(randomizeManager.getWeatherRandomizer(worldId))
}
