package party.morino.moripafishing

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.command.registerCommands

/**
 * MoripaFishing のブートストラップ。コマンドはブートストラップ時に登録し、実行時にコアを遅延参照する。
 */
@Suppress("unused") // paper-plugin.yml の bootstrapper から参照される
class MoripaFishingBootstrap : PluginBootstrap {
    private lateinit var plugin: MoripaFishing

    override fun bootstrap(context: BootstrapContext) = registerCommands(context) { plugin.core() }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin = MoripaFishing().also { plugin = it }
}
