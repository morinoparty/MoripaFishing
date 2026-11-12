package party.morino.moripafishing.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.incendo.cloud.paper.util.sender.Source

/**
 * [sender] を返す [Source]。`stack()` はテストで使わない。
 */
internal class FakeSource(
    private val sender: CommandSender,
) : Source {
    override fun stack(): CommandSourceStack = throw UnsupportedOperationException("not used in tests")

    override fun source(): CommandSender = sender
}
