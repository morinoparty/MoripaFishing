package party.morino.moripafishing.core.world.weather.provider

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.plugin.Plugin
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * Bukkit バニラの天候状態を読み取る、単一ワールドにスコープされたプロバイダー。
 *
 * Bukkit の World API (`World.hasStorm()`, `World.isThundering()`) は
 * メインスレッド以外からの呼び出しが安全ではないため、本実装ではイベント駆動で
 * 値をスレッドセーフにキャッシュする:
 *
 * - `WeatherChangeEvent` / `ThunderChangeEvent` を購読し、対象ワールドのイベントのみ
 *   メインスレッドでスナップショットを保存する
 * - `getCurrentWeather` はスナップショットを参照するのみで Bukkit を叩かない
 * - MONITOR 優先度 + `ignoreCancelled = true` で、キャンセルされなかった遷移の
 *   確定後の状態を反映する
 *
 * バニラは SUNNY / RAINY / THUNDERSTORM の3状態のみ表現可能で、
 * `CLOUDY` / `FOGGY` / `SNOWY` にはマッピングできない。
 * これらを条件に持つ魚はこのモードでは出現しない。
 *
 * 対象ワールドが未ロードの場合は SUNNY を返す。
 */
class VanillaWeatherProvider(
    private val plugin: Plugin,
    private val worldId: FishingWorldId,
) : WeatherProvider,
    Listener {
    @Volatile
    private var snapshot: WeatherType = WeatherType.SUNNY

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        // 既にロード済みの場合は、メインスレッドから初期値を同期する
        if (Bukkit.isPrimaryThread()) {
            primeFromWorld()
        } else {
            Bukkit.getScheduler().runTask(plugin, Runnable { primeFromWorld() })
        }
    }

    private fun primeFromWorld() {
        val world = Bukkit.getWorld(worldId.value) ?: return
        snapshot = classify(world.hasStorm(), world.isThundering)
    }

    override fun getCurrentWeather(worldId: FishingWorldId): WeatherType = snapshot

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onWeatherChange(event: WeatherChangeEvent) {
        if (event.world.name != worldId.value) return
        snapshot = classify(event.toWeatherState(), event.world.isThundering)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onThunderChange(event: ThunderChangeEvent) {
        if (event.world.name != worldId.value) return
        snapshot = classify(event.world.hasStorm(), event.toThunderState())
    }

    override fun dispose() {
        HandlerList.unregisterAll(this as Listener)
    }

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
