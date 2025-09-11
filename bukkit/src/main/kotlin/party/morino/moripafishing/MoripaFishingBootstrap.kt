package party.morino.moripafishing

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.CommandManager
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import party.morino.moripafishing.ui.commands.DefaultCommand
import party.morino.moripafishing.ui.commands.WorldCommand
import party.morino.moripafishing.utils.commands.CommandSenderMapper
import party.morino.moripafishing.utils.commands.parser.FishingWorldParser
import party.morino.moripafishing.utils.commands.parser.GeneratorParser

/**
 * MoripaFishingプラグインのブートストラップクラス
 * プラグインの初期化処理
 */
@Suppress("unused")
class MoripaFishingBootstrap : PluginBootstrap {
    /**
     * プラグインのブートストラップ処理を行うメソッド
     * @param context ブートストラップコンテキスト
     */
    override fun bootstrap(context: BootstrapContext) {
        // コマンドマネージャーのインスタンスを作成
        val commandManager: CommandManager<CommandSender> =
            PaperCommandManager
                .builder(CommandSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator()) // 非同期実行コーディネーターを設定
                .buildBootstrapped(context) // ブートストラップされたコマンドマネージャーを構築

        commandManager.parserRegistry().registerParser(FishingWorldParser.fishingIdParser())
        commandManager.parserRegistry().registerParser(GeneratorParser.generatorParser())
        // アノテーションパーサーのインスタンスを作成
        val annotationParser = AnnotationParser(commandManager, CommandSender::class.java)
        annotationParser.installCoroutineSupport()

        // アノテーションを解析
        with(annotationParser) {
            parse(
                // ここにコマンドのアノテーションを追加する
                WorldCommand(),
                DefaultCommand(),
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
