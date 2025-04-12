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

    /**
     * プラグインのブートストラップ処理を行うメソッド
     * @param context ブートストラップコンテキスト
     */
    override fun bootstrap(context: BootstrapContext) {
        // コマンドマネージャーのインスタンスを作成
        val commandManager: CommandManager<CommandSourceStack> = PaperCommandManager.builder()
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator()) // 非同期実行コーディネーターを設定
            .buildBootstrapped(context) // ブートストラップされたコマンドマネージャーを構築

        // アノテーションパーサーのインスタンスを作成
        val annotationParser = AnnotationParser<CommandSourceStack>(commandManager, CommandSourceStack::class.java)

        // アノテーションを解析
        with(annotationParser) {
            parse(
                // ここにコマンドのアノテーションを追加する
            )
        }
        // ゲームイベントのレジストリに関する情報へのリンク
        // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/registry/event/RegistryEvents.html#GAME_EVENT
    }

    /**
     * プラグインのインスタンスを作成するメソッド
     * @param context プラグインプロバイダコンテキスト
     * @return MoripaFishingプラグインのインスタンス
     */
    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return MoripaFishing() // MoripaFishingプラグインのインスタンスを返す
    }
} 