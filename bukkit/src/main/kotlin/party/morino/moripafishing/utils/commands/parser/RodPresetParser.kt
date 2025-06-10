package party.morino.moripafishing.utils.commands.parser

import kotlinx.coroutines.runBlocking
import org.bukkit.command.CommandSender
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.model.rod.RodConfiguration

/**
 * ロッドプリセットのコマンド引数パーサー
 * プリセット名の自動補完とバリデーションを提供
 */
class RodPresetParser<C> :
    ArgumentParser<C, RodConfiguration>,
    BlockingSuggestionProvider.Strings<CommandSender>,
    KoinComponent {
    // FishingManagerを通じてRodPresetManagerにアクセス
    private val fishingManager: FishingManager by inject()
    private val rodPresetManager by lazy { fishingManager.getRodPresetManager() }

    /**
     * プリセット名を解析してRodConfigurationを取得
     */
    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput,
    ): ArgumentParseResult<RodConfiguration> {
        val presetName = commandInput.readString()
        val rodConfig = runBlocking { rodPresetManager.getPreset(presetName) }

        return if (rodConfig != null) {
            ArgumentParseResult.success(rodConfig)
        } else {
            ArgumentParseResult.failure(Throwable("Preset '$presetName' not found"))
        }
    }

    /**
     * 利用可能なプリセット名のサジェストを提供
     */
    override fun stringSuggestions(
        commandContext: CommandContext<CommandSender?>,
        input: CommandInput,
    ): Iterable<String> {
        return runBlocking { rodPresetManager.getAllPresetNames() }
    }

    companion object {
        /**
         * RodPresetParser用のParserDescriptorを作成
         */
        fun rodPresetParser(): ParserDescriptor<CommandSender, RodConfiguration> {
            return ParserDescriptor.of(RodPresetParser(), RodConfiguration::class.java)
        }
    }
}
