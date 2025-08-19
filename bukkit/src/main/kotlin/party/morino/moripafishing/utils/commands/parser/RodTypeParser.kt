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

/**
 * ロッドタイプのコマンド引数パーサー
 * 既存のプリセットIDを基にサジェストを提供しつつ、任意の文字列も受け入れる
 */
class RodTypeParser<C> : ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<CommandSender>, KoinComponent {
    // FishingManagerを通じてRodPresetManagerにアクセス
    private val fishingManager: FishingManager by inject()
    private val rodPresetManager by lazy { fishingManager.getRodPresetManager() }

    /**
     * ロッドタイプの文字列を解析（任意の文字列を受け入れる）
     */
    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput,
    ): ArgumentParseResult<String> {
        val rodType = commandInput.readString()
        // 任意の文字列を受け入れる（バリデーションはしない）
        return ArgumentParseResult.success(rodType)
    }

    /**
     * 既存のプリセットIDを基にサジェストを提供
     */
    override fun stringSuggestions(
        commandContext: CommandContext<CommandSender?>,
        input: CommandInput,
    ): Iterable<String> {
        // 既存のプリセットIDを取得
        return runBlocking {
            rodPresetManager.getAllPresetIds()
                .map { presetId -> presetId.value }
                .distinct() // 重複を除去
                .sorted() // アルファベット順にソート
        }
    }

    companion object {
        /**
         * RodTypeParser用のParserDescriptorを作成
         */
        fun rodTypeParser(): ParserDescriptor<CommandSender, String> {
            return ParserDescriptor.of(RodTypeParser(), String::class.java)
        }
    }
}
