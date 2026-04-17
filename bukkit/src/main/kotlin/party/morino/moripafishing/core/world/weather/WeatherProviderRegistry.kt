package party.morino.moripafishing.core.world.weather

import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.util.concurrent.ConcurrentHashMap

/**
 * 外部プラグインから登録された `WeatherProvider` を保持するスレッドセーフなレジストリ。
 *
 * `ExternalWeatherProvider` がこのレジストリから登録済みプロバイダーを引いて委譲する。
 */
class WeatherProviderRegistry {
    private val providers: ConcurrentHashMap<FishingWorldId, WeatherProvider> = ConcurrentHashMap()

    fun register(
        worldId: FishingWorldId,
        provider: WeatherProvider,
    ) {
        providers[worldId] = provider
    }

    fun unregister(worldId: FishingWorldId) {
        providers.remove(worldId)
    }

    fun get(worldId: FishingWorldId): WeatherProvider? = providers[worldId]
}
