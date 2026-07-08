package party.morino.moripafishing.core.world.weather.source

import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.world.weather.provider.VanillaWeatherProvider

/**
 * Bukkit のバニラ天候状態を読み取り、魚の抽選条件にのみ反映する組み込みソース（`moripafishing:vanilla`）。
 *
 * ワールドの天候は変更せず、`DO_WEATHER_CYCLE` を有効に保つ。
 * プロバイダーはワールドごとにスコープされ、対象ワールドのイベントのみ購読する。
 */
class VanillaWeatherSource(
    private val plugin: Plugin,
) : WeatherSource {
    override val key: Key = WeatherSource.VANILLA
    override val managesWorldWeather: Boolean = false
    override val usesVanillaWeatherCycle: Boolean = true

    override fun createProvider(worldId: FishingWorldId): WeatherProvider = VanillaWeatherProvider(plugin, worldId)
}
