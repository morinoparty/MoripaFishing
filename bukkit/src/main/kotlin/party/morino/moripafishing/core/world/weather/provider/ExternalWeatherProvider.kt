package party.morino.moripafishing.core.world.weather.provider

import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.weather.WeatherProviderRegistry
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * `WeatherProviderRegistry` から外部プラグインが登録した `WeatherProvider` を引いて委譲するプロバイダー。
 *
 * プロバイダーが未登録の場合は SUNNY を返し、ワールドごとに1回だけ警告ログを出す。
 */
class ExternalWeatherProvider(
    private val registry: WeatherProviderRegistry,
    private val logger: Logger,
) : WeatherProvider {
    private val warnedWorlds: MutableSet<FishingWorldId> =
        ConcurrentHashMap.newKeySet()

    override fun getCurrentWeather(worldId: FishingWorldId): WeatherType {
        val provider = registry.get(worldId)
        if (provider == null) {
            if (warnedWorlds.add(worldId)) {
                logger.warning(
                    "[${worldId.value}] WeatherMode.EXTERNAL is configured but no WeatherProvider is registered. " +
                        "Falling back to SUNNY until a provider is registered via MoripaFishingAPI.registerWeatherProvider.",
                )
            }
            return WeatherType.SUNNY
        }
        warnedWorlds.remove(worldId)
        return provider.getCurrentWeather(worldId)
    }
}
