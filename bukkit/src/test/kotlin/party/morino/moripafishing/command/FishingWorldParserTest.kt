package party.morino.moripafishing.command

import org.incendo.cloud.CommandManager
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.internal.CommandRegistrationHandler
import org.incendo.cloud.paper.util.sender.Source
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.testing.FakeFishingWorld
import party.morino.moripafishing.testing.FakeWorldManager

@ExtendWith(MockBukkitExtension::class)
class FishingWorldParserTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val worlds = FakeWorldManager(FakeFishingWorld(FishingWorldId("default")), FakeFishingWorld(FishingWorldId("sea")))
    private val parser = FishingWorldParser { worlds }

    private fun context(): CommandContext<Source> {
        val manager =
            object : CommandManager<Source>(
                ExecutionCoordinator.simpleCoordinator(),
                CommandRegistrationHandler.nullCommandRegistrationHandler(),
            ) {
                override fun hasPermission(
                    sender: Source,
                    permission: String,
                ): Boolean = true
            }
        return CommandContext(FakeSource(server.consoleSender), manager)
    }

    @Test
    fun `parses a defined world`() {
        val result = parser.parse(context(), CommandInput.of("sea"))
        assertEquals(FishingWorldId("sea"), result.parsedValue().orElseThrow().getId())
    }

    @Test
    fun `fails for an unknown world`() {
        val result = parser.parse(context(), CommandInput.of("lake"))
        assertEquals("Unknown fishing world: lake", result.failure().orElseThrow().message)
    }

    @Test
    fun `suggests world ids`() {
        assertEquals(listOf("default", "sea"), parser.stringSuggestions(context(), CommandInput.empty()).toList())
    }
}
