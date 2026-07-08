package party.morino.moripafishing.core.world.weather

import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.GlobalContext
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.core.world.FishingWorldImpl
import java.util.concurrent.ConcurrentHashMap

/**
 * `WeatherSource` を名前空間キーで保持するスレッドセーフなレジストリ。
 *
 * 組み込みソース（`moripafishing:internal` / `moripafishing:vanilla`）は起動時に登録され、
 * 外部プラグインは `MoripaFishingAPI.registerWeatherSource` で自前のソースを追加できる。
 * `FishingWorldImpl` が `ClimateConfig.weatherSource` のキーでこのレジストリを引いて解決する。
 *
 * 登録時にはソースクラスの提供元プラグインを記録し、プラグイン無効化時に
 * [unregisterOwnedBy] で自動解除できるようにする。解除・上書き時には該当キーを
 * 使用中のワールドのキャッシュを無効化し、旧プロバイダーを破棄させる。
 */
class WeatherSourceRegistry {
    private data class Registration(
        val source: WeatherSource,
        val owner: Plugin?,
    )

    private val sources: ConcurrentHashMap<Key, Registration> = ConcurrentHashMap()

    fun register(source: WeatherSource) {
        val owner = runCatching { JavaPlugin.getProvidingPlugin(source.javaClass) }.getOrNull()
        val previous = sources.put(source.key, Registration(source, owner))
        if (previous == null || previous.source === source) return
        if (isBuiltInKey(source.key)) {
            logger()?.warning(
                "Built-in weather source '${source.key}' is being overwritten by " +
                    "${source.javaClass.name} (plugin: ${owner?.name ?: "unknown"}).",
            )
        }
        // 旧ソースを掴んでいるワールドを即座に再解決させる
        invalidateWorlds(source.key)
    }

    fun unregister(key: Key) {
        sources.remove(key) ?: return
        invalidateWorlds(key)
    }

    /**
     * 指定プラグインが登録したソースをすべて解除する。プラグイン無効化時に呼ばれる。
     */
    fun unregisterOwnedBy(plugin: Plugin) {
        sources.entries
            .filter { it.value.owner === plugin }
            .forEach { (key, _) ->
                sources.remove(key)
                logger()?.info(
                    "Weather source '$key' was unregistered because plugin '${plugin.name}' was disabled.",
                )
                invalidateWorlds(key)
            }
    }

    fun get(key: Key): WeatherSource? = sources[key]?.source

    fun getKeys(): Set<Key> = sources.keys.toSet()

    private fun isBuiltInKey(key: Key): Boolean = key == WeatherSource.INTERNAL || key == WeatherSource.VANILLA

    /**
     * 指定キーのソースを使用中のワールドの解決キャッシュを破棄し、
     * 次回アクセス時に再解決（未登録なら `moripafishing:internal` へのフォールバック）させる。
     */
    private fun invalidateWorlds(key: Key) {
        val worldManager = GlobalContext.getOrNull()?.getOrNull<WorldManager>() ?: return
        worldManager.getWorldIdList().forEach { worldId ->
            (worldManager.getWorld(worldId) as? FishingWorldImpl)?.invalidateWeatherSource(key)
        }
    }

    private fun logger(): java.util.logging.Logger? = GlobalContext.getOrNull()?.getOrNull<MoripaFishing>()?.logger
}
