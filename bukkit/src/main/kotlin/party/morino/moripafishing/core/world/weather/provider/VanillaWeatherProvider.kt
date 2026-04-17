package party.morino.moripafishing.core.world.weather.provider

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.plugin.Plugin
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.util.concurrent.ConcurrentHashMap

/**
 * Bukkit バニラの天候状態を読み取るプロバイダー。
 *
 * Bukkit の World API (`World.hasStorm()`, `World.isThundering()`) は
 * メインスレッド以外からの呼び出しが安全ではないため、本実装ではイベント駆動で
 * 値をスレッドセーフにキャッシュする:
 *
 * - `WeatherChangeEvent` / `ThunderChangeEvent` を購読し、メインスレッドで
 *   `ConcurrentHashMap` にスナップショットを保存する
 * - `getCurrentWeather` はマップを参照するのみで Bukkit を叩かない
 *
 * バニラは SUNNY / RAINY / THUNDERSTORM の3状態のみ表現可能で、
 * `CLOUDY` / `FOGGY` / `SNOWY` にはマッピングできない。
 * これらを条件に持つ魚はこのモードでは出現しない。
 *
 * 監視対象ワールドが未ロードの場合は SUNNY を返す。
 */
class VanillaWeatherProvider(
    private val plugin: Plugin,
) : WeatherProvider,
    Listener {
    private val snapshot: ConcurrentHashMap<String, WeatherType> = ConcurrentHashMap()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        // 既にロード済みのワールドについては、メインスレッドから初期値を同期する
        if (Bukkit.isPrimaryThread()) {
            primeFromLoadedWorlds()
        } else {
            Bukkit.getScheduler().runTask(plugin, Runnable { primeFromLoadedWorlds() })
        }
    }

    private fun primeFromLoadedWorlds() {
        for (world in Bukkit.getWorlds()) {
            snapshot[world.name] = classify(world.hasStorm(), world.isThundering)
        }
    }

    override fun getCurrentWeather(worldId: FishingWorldId): WeatherType =
        snapshot[worldId.value] ?: WeatherType.SUNNY

    @EventHandler
    fun onWeatherChange(event: WeatherChangeEvent) {
        val world = event.world
        val wasThundering = world.isThundering
        snapshot[world.name] = classify(event.toWeatherState(), wasThundering)
    }

    @EventHandler
    fun onThunderChange(event: ThunderChangeEvent) {
        val world = event.world
        val hasStorm = world.hasStorm()
        snapshot[world.name] = classify(hasStorm, event.toThunderState())
    }

    private fun classify(
        storming: Boolean,
        thundering: Boolean,
    ): WeatherType =
        when {
            thundering -> WeatherType.THUNDERSTORM
            storming -> WeatherType.RAINY
            else -> WeatherType.SUNNY
        }
}
