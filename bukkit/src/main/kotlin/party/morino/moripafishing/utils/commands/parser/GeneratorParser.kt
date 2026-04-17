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
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.integrations.worldlifecycle.api.GeneratorData

class GeneratorParser<C> :
    ArgumentParser<C, GeneratorData>,
    BlockingSuggestionProvider.Strings<CommandSender>,
    KoinComponent {
    private val plugin: MoripaFishing by inject()

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput,
    ): ArgumentParseResult<GeneratorData> {
        val generatorId = commandInput.readString()
        val provider =
            plugin.getWorldLifecycleProvider()
                ?: return ArgumentParseResult.failure(
                    Throwable("WorldLifecycle integration is not installed."),
                )
        val generator = provider.getGenerator(generatorId)
        return if (generator != null) {
            ArgumentParseResult.success(generator)
        } else {
            ArgumentParseResult.failure(Throwable("$generatorId not found"))
        }
    }

    override fun stringSuggestions(
        commandContext: CommandContext<CommandSender?>,
        input: CommandInput,
    ): Iterable<String> =
        plugin.getWorldLifecycleProvider()?.listGenerators()?.map { it.id } ?: emptyList()

    companion object {
        fun generatorParser(): ParserDescriptor<CommandSender, GeneratorData> =
            ParserDescriptor.of(GeneratorParser(), GeneratorData::class.java)
    }
}
