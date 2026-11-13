package party.morino.moripafishing.addons.chum

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.addons.chum.command.ChumCommands
import party.morino.moripafishing.addons.chum.command.chumCommand
import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.addons.chum.config.ChumConfigLoader
import party.morino.moripafishing.addons.chum.effect.ChumEffectApplier
import party.morino.moripafishing.addons.chum.effect.ChumEffectStore
import party.morino.moripafishing.addons.chum.item.ChumItems
import party.morino.moripafishing.addons.chum.listener.ChumDrawListener
import party.morino.moripafishing.addons.chum.listener.ChumUseListener
import party.morino.moripafishing.api.MoripaFishingAPIProvider
import java.io.File
import java.time.Clock

/**
 * MoripaFishing の撒き餌 Addon。
 *
 * コア (`MoripaFishing`) の公開 API (`:api`) のみを利用する。コアは必須の依存で、先に有効化される。
 * 撒き餌の効果はメモリ上にだけ保持し、サーバーを再起動すると失われる。
 */
class ChumPlugin : JavaPlugin() {
    private lateinit var chumConfig: ChumConfig

    override fun onEnable() {
        chumConfig = loadConfig()
        val api = MoripaFishingAPIProvider.get()
        val clock = Clock.systemUTC()
        val store = ChumEffectStore(clock)
        val items = ChumItems(NamespacedKey(this, "chum"))
        val currentConfig = { chumConfig }
        val applier = ChumEffectApplier(store, currentConfig, api, logger)
        server.pluginManager.registerEvents(ChumDrawListener(applier, store, currentConfig), this)
        server.pluginManager.registerEvents(ChumUseListener(api, store, items, currentConfig, clock), this)
        val commands =
            ChumCommands(api, applier, items, currentConfig, clock) {
                chumConfig = loadConfig()
                applier.clearWarnings()
            }
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(chumCommand(commands), "Give chum items and preview their effect on catch odds")
        }
    }

    /** `config.yml` を読み込む。無ければ同梱の例を書き出す。 */
    private fun loadConfig(): ChumConfig {
        val file = File(dataFolder, "config.yml")
        if (!file.exists()) saveResource("config.yml", false)
        return ChumConfigLoader.load(file.readText())
    }
}
