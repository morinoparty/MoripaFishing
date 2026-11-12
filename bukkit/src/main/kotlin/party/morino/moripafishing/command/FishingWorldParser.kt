package party.morino.moripafishing.command

import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りワールド ID を [FishingWorld] に解決する引数パーサー。
 *
 * @property worlds 釣りワールドの参照先
 */
internal class FishingWorldParser(
    private val worlds: () -> WorldManager,
) : ArgumentParser<Source, FishingWorld>,
    BlockingSuggestionProvider.Strings<Source> {
    override fun parse(
        commandContext: CommandContext<Source>,
        commandInput: CommandInput,
    ): ArgumentParseResult<FishingWorld> {
        val id = commandInput.readString()
        val world = worlds().getWorld(FishingWorldId(id))
        return if (world != null) {
            ArgumentParseResult.success(world)
        } else {
            ArgumentParseResult.failure(IllegalArgumentException("Unknown fishing world: $id"))
        }
    }

    override fun stringSuggestions(
        commandContext: CommandContext<Source>,
        input: CommandInput,
    ): Iterable<String> = worlds().getWorlds().map { it.getId().value }
}
