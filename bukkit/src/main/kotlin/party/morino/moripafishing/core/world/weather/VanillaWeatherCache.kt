package party.morino.moripafishing.core.world.weather

import net.kyori.adventure.key.Key
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.model.world.WeatherType.SUNNY
import party.morino.moripafishing.core.world.WorldKeys
import java.util.concurrent.ConcurrentHashMap

/**
 * バニラ天候のスナップショット (全ワールド共通の 1 Listener)。
 *
 * Bukkit の `World.hasStorm()` / `isThundering` はメインスレッド以外から安全に読めないため、
 * ワールドのロードと天候変化のイベントでスナップショットを更新し、[get] はそれだけを参照する。
 * [get] は任意スレッド、それ以外はメインスレッドから呼ばれる。
 *
 * バニラは SUNNY / RAINY / THUNDERSTORM のみ表現でき、未知のワールドは SUNNY とみなす。
 */
internal class VanillaWeatherCache : Listener {
    private val snapshots = ConcurrentHashMap<Key, WeatherType>()

    /**
     * ワールドのバニラ天候を返す。スナップショットがない場合は SUNNY。
     */
    fun get(worldKey: Key): WeatherType = snapshots[WorldKeys.normalize(worldKey)] ?: SUNNY

    /**
     * ロード済みのワールドからスナップショットを作り直す。メインスレッドから呼ぶこと。
     */
    fun primeAll(worlds: Collection<World>) {
        worlds.forEach(::prime)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onWorldLoad(event: WorldLoadEvent) {
        prime(event.world)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onWorldUnload(event: WorldUnloadEvent) {
        snapshots.remove(event.world.cacheKey())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onWeatherChange(event: WeatherChangeEvent) {
        snapshots[event.world.cacheKey()] = classify(event.toWeatherState(), event.world.isThundering)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onThunderChange(event: ThunderChangeEvent) {
        snapshots[event.world.cacheKey()] = classify(event.world.hasStorm(), event.toThunderState())
    }

    private fun prime(world: World) {
        snapshots[world.cacheKey()] = classify(world.hasStorm(), world.isThundering)
    }

    private fun World.cacheKey(): Key = WorldKeys.normalize(key)

    companion object {
        /**
         * バニラの天候フラグを [WeatherType] に分類する。
         * 雷はストーム中のみ描画されるため、`thundering` 単独では THUNDERSTORM にしない。
         */
        fun classify(
            storming: Boolean,
            thundering: Boolean,
        ): WeatherType =
            when {
                storming && thundering -> WeatherType.THUNDERSTORM
                storming -> WeatherType.RAINY
                else -> WeatherType.SUNNY
            }
    }
}
