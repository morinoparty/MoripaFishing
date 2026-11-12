package party.morino.moripafishing.command

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.paper.util.sender.Source
import party.morino.moripafishing.MoripaFishingCore
import java.util.logging.Level

/**
 * `/mf reload`: 設定・ワールド・レアリティ・魚・翻訳を読み直す。
 *
 * @property core 有効化中のコア
 */
internal class ReloadCommand(
    private val core: () -> MoripaFishingCore,
) {
    @Command("mf reload")
    @Permission("moripafishing.command.reload")
    fun reload(source: Source) {
        val sender = source.source()
        try {
            core().reload()
            sender.sendRichMessage("<green>Reloaded MoripaFishing.")
        } catch (e: IllegalArgumentException) {
            // SerializationException を含む。config.json が不正な場合は以前の設定のまま
            sender.sendRichMessage("<red>Reload failed: <message>", Placeholder.unparsed("message", e.message.orEmpty()))
            core().logger.log(Level.WARNING, "Reload failed", e)
        }
    }
}
