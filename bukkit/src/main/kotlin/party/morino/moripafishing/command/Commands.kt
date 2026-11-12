package party.morino.moripafishing.command

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import org.incendo.cloud.CommandManager
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ParserDescriptor
import party.morino.moripafishing.MoripaFishingCore
import party.morino.moripafishing.api.core.world.FishingWorld

/**
 * `/mf` コマンドを登録する。ブートストラップ時に呼ぶ。
 *
 * コマンドはコアより先に登録されるため、コアは実行時に [core] から遅延参照する。
 * コマンドは `simpleCoordinator` によりディスパッチしたスレッド (メインスレッド) で実行される。
 *
 * @param context ブートストラップのコンテキスト
 * @param core 有効化中のコア
 */
internal fun registerCommands(
    context: BootstrapContext,
    core: () -> MoripaFishingCore,
) {
    val manager =
        PaperCommandManager
            .builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildBootstrapped(context)
    configureCommands(manager, core)
}

/**
 * [manager] に釣りワールドのパーサーと `/mf` コマンドを登録する。
 *
 * @param manager 登録先のコマンドマネージャー
 * @param core 有効化中のコア
 */
internal fun configureCommands(
    manager: CommandManager<Source>,
    core: () -> MoripaFishingCore,
) {
    val worlds = { core().getWorldManager() }
    manager.parserRegistry().registerParser(ParserDescriptor.of(FishingWorldParser(worlds), FishingWorld::class.java))
    AnnotationParser(manager, Source::class.java).parse(ReloadCommand(core), WorldCommand(worlds))
}
