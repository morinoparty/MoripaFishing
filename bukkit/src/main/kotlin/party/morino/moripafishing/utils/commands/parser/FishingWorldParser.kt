package party.morino.moripafishing.utils.commands.parser

import org.bukkit.command.CommandSender
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId

class FishingWorldParser<C> : ArgumentParser<C, FishingWorld>, BlockingSuggestionProvider.Strings<CommandSender>, KoinComponent {
    val worldManager: WorldManager by inject()

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput,
    ): ArgumentParseResult<FishingWorld> {
        val worldId = commandInput.readString()
        val fishingWorldId = FishingWorldId(worldId)
        return if (worldManager.getWorldIdList().contains(fishingWorldId)) {
            ArgumentParseResult.success(worldManager.getWorld(fishingWorldId))
        } else {
            ArgumentParseResult.failure(Throwable("$fishingWorldId not found"))
        }
    }

    override fun stringSuggestions(
        commandContext: CommandContext<CommandSender?>,
        input: CommandInput,
    ): Iterable<String> {
        return worldManager.getWorldIdList().map { it.value }
    }

    companion object {
        fun fishingIdParser(): ParserDescriptor<CommandSender, FishingWorld> {
            return ParserDescriptor.of(FishingWorldParser(), FishingWorld::class.java)
        }
    }
}
