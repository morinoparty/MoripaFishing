package party.morino.moripafishing.core.world.weather

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import java.util.concurrent.ConcurrentHashMap

/**
 * `WeatherSource` を名前空間キーで保持するスレッドセーフなレジストリ。
 *
 * 組み込みソース（`moripafishing:internal` / `moripafishing:vanilla`）は起動時に登録され、
 * 外部プラグインは `MoripaFishingAPI.registerWeatherSource` で自前のソースを追加できる。
 * `FishingWorldImpl` が `ClimateConfig.weatherSource` のキーでこのレジストリを引いて解決する。
 */
class WeatherSourceRegistry {
    private val sources: ConcurrentHashMap<Key, WeatherSource> = ConcurrentHashMap()

    fun register(source: WeatherSource) {
        sources[source.key] = source
    }

    fun unregister(key: Key) {
        sources.remove(key)
    }

    fun get(key: Key): WeatherSource? = sources[key]
}
