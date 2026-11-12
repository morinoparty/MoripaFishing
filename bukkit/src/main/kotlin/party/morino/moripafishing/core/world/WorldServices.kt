package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin
import party.morino.moripafishing.api.core.world.spi.WeatherSource
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle
import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

/**
 * `ServicesManager` に登録された Integration の SPI 実装への窓口。
 *
 * 登録は呼び出しのたびに引き直し、キャッシュしない。そのため後から登録・解除されたサービスも次の呼び出しから反映される。
 * Integration のコードが投げた例外はすべてここで受け止め、登録元プラグイン名付きで警告したうえで失敗の値に置き換える。
 * そのため呼び出し側 ([WorldManagerImpl] / [FishingWorldImpl]) は Integration の例外を考慮しなくてよい。
 *
 * @property plugin コアプラグイン
 */
internal class WorldServices(
    private val plugin: Plugin,
) {
    private val services get() = plugin.server.servicesManager

    /** 直近の呼び出しで警告済みの外部天候ソースのキー。毎回の魚の抽選で警告が繰り返されないようにする。 */
    private val warnedSources = ConcurrentHashMap.newKeySet<Key>()

    /**
     * 登録されたすべての [WorldStateApplier] に状態を送る。メインスレッドから呼ぶこと。
     * 1 件が例外を投げても他の適用は続ける。
     */
    fun apply(state: WorldState) {
        services.getRegistrations(WorldStateApplier::class.java).forEach { registration ->
            guarded(registration.plugin, "WorldStateApplier.apply(${state.worldKey.asString()})", Unit) {
                registration.provider.apply(state)
            }
        }
    }

    /**
     * 最優先で登録された [WorldLifecycle]。未登録の場合は `null`。
     * 返す実装は例外を失敗の値 (空集合 / `false`) に置き換える。
     */
    fun lifecycle(): WorldLifecycle? =
        services.getRegistration(WorldLifecycle::class.java)?.let { GuardedLifecycle(it.provider, it.plugin) }

    /**
     * 優先度順の登録のうち、キーが [source] の最初の [WeatherSource] で天候を決める。任意スレッド。
     * 未登録・例外の場合は `null` を返し、同じキーについては成功するまで警告を一度だけ出す。
     */
    @Suppress("TooGenericExceptionCaught") // Integration の任意の失敗を天候の決定に波及させないため
    fun externalWeather(
        source: Key,
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherDecision? {
        val wanted = WorldKeys.normalize(source)
        return try {
            val provider =
                services
                    .getRegistrations(WeatherSource::class.java)
                    .firstOrNull { WorldKeys.normalize(it.provider.key) == wanted }
                    ?.provider
            if (provider == null) {
                warnOnce(
                    wanted,
                    "weather source '${wanted.asString()}' is not registered; using the built-in random calendar",
                )
                null
            } else {
                WeatherDecision(provider.getWeather(worldId, worldKey), provider.appliesToWorld)
                    .also { warnedSources.remove(wanted) }
            }
        } catch (e: Exception) {
            warnOnce(wanted, "weather source '${wanted.asString()}' failed; using the built-in random calendar", e)
            null
        }
    }

    private fun warnOnce(
        source: Key,
        message: String,
        error: Throwable? = null,
    ) {
        if (warnedSources.add(source)) plugin.logger.log(Level.WARNING, message, error)
    }

    @Suppress("TooGenericExceptionCaught") // Integration の任意の失敗をコアの処理に波及させないため
    private inline fun <T> guarded(
        owner: Plugin,
        operation: String,
        fallback: T,
        block: () -> T,
    ): T =
        try {
            block()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "$operation from ${owner.name} failed", e)
            fallback
        }

    /**
     * 例外を失敗の値に置き換える [WorldLifecycle] のラッパー。
     */
    private inner class GuardedLifecycle(
        private val delegate: WorldLifecycle,
        private val owner: Plugin,
    ) : WorldLifecycle {
        override fun getGeneratorIds(): Set<String> =
            guarded(owner, "WorldLifecycle.getGeneratorIds", emptySet()) { delegate.getGeneratorIds() }

        override fun createWorld(
            worldKey: Key,
            generatorId: String,
        ): Boolean =
            guarded(owner, "WorldLifecycle.createWorld(${worldKey.asString()})", false) {
                delegate.createWorld(worldKey, generatorId)
            }

        override fun unloadWorld(worldKey: Key): Boolean =
            guarded(owner, "WorldLifecycle.unloadWorld(${worldKey.asString()})", false) {
                delegate.unloadWorld(worldKey)
            }
    }
}
