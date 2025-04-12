package party.morino.moripafishing

import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.GameEventKeys
import org.bukkit.plugin.java.JavaPlugin
import org.checkerframework.checker.units.qual.C
import org.incendo.cloud.CommandManager
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager

/**
 * MoripaFishingプラグインのブートストラップクラス
 * プラグインの初期化処理
 */
class MoripaFishingBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        val commandManager: CommandManager<CommandSourceStack> = PaperCommandManager.builder()
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildBootstrapped(context)

        val annotationParser = AnnotationParser<CommandSourceStack>(commandManager, CommandSourceStack::class.java)

        with(annotationParser) {
            parse(
                
            )
        }
        //https://jd.papermc.io/paper/1.21.4/io/papermc/paper/registry/event/RegistryEvents.html#GAME_EVENT

    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return MoripaFishing()
    }
} 