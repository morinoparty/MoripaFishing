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
import party.morino.moripafishing.api.core.world.GeneratorManager
import party.morino.moripafishing.api.model.world.generator.GeneratorData
import party.morino.moripafishing.api.model.world.generator.GeneratorId

class GeneratorParser<C> : ArgumentParser<C, GeneratorData>, BlockingSuggestionProvider.Strings<CommandSender>, KoinComponent {
    val generatorManager: GeneratorManager by inject()

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput,
    ): ArgumentParseResult<GeneratorData> {
        val generatorId = commandInput.readString()
        val generator = generatorManager.getGenerator(GeneratorId(generatorId))
        return if (generator != null) {
            ArgumentParseResult.success(generator)
        } else {
            ArgumentParseResult.failure(Throwable("$generatorId not found"))
        }
    }

    override fun stringSuggestions(
        commandContext: CommandContext<CommandSender?>,
        input: CommandInput,
    ): Iterable<String> {
        return generatorManager.getAllGenerators().map { it.id.value }
    }

    companion object {
        fun generatorParser(): ParserDescriptor<CommandSender, GeneratorData> {
            return ParserDescriptor.of(GeneratorParser(), GeneratorData::class.java)
        }
    }
}
