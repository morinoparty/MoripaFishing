package party.morino.moripafishing.command

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.internal.CommandRegistrationHandler
import org.incendo.cloud.paper.util.sender.Source
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.testing.loadMoripaFishing

/**
 * アノテーションで定義したコマンドを cloud に登録し、コンソールから実行できることを確認する。
 * Paper のブートストラップは MockBukkit で動かないため、本番と同じ [configureCommands] で素の `CommandManager` に登録する。
 */
@ExtendWith(MockBukkitExtension::class)
class CommandsTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private lateinit var plugin: MoripaFishing
    private lateinit var manager: CommandManager<Source>

    @BeforeEach
    fun setUp() {
        plugin = server.loadMoripaFishing()
        manager =
            object : CommandManager<Source>(
                ExecutionCoordinator.simpleCoordinator(),
                CommandRegistrationHandler.nullCommandRegistrationHandler(),
            ) {
                override fun hasPermission(
                    sender: Source,
                    permission: String,
                ): Boolean = true
            }
        // PaperCommandManager が標準で登録するパーサーのうち、コマンドが使うもの
        manager.parserRegistry().registerParser(PlayerParser.playerParser<Source>())
        configureCommands(manager) { plugin.core() }
    }

    private fun execute(command: String): List<String> {
        manager.commandExecutor().executeCommand(FakeSource(server.consoleSender), command).join()
        return generateSequence { server.consoleSender.nextComponentMessage() }
            .map { PlainTextComponentSerializer.plainText().serialize(it) }
            .toList()
    }

    @Test
    fun `all commands are registered`() {
        val commands = manager.commands().map { it.toString() }
        assertEquals(10, commands.size, commands.toString())
    }

    @Test
    fun `reload succeeds from the console`() {
        assertEquals(listOf("Reloaded MoripaFishing."), execute("mf reload"))
    }

    @Test
    fun `set center requires coordinates from the console`() {
        assertEquals(listOf("x and z are required from the console."), execute("mf world config set center default"))
    }

    @Test
    fun `set center with coordinates updates the border`() {
        assertEquals(listOf("Updated config of world default."), execute("mf world config set center default 10 20"))
        val border =
            plugin
                .core()
                .getWorldManager()
                .getWorld(FishingWorldId("default"))
                ?.getConfig()
                ?.border
        assertEquals(10.0 to 20.0, border?.centerX to border?.centerZ)
    }

    @Test
    fun `generator ids are suggested`() {
        val suggestions = manager.suggestionFactory().suggestImmediately(FakeSource(server.consoleSender), "mf world create test ")
        assertEquals(emptyList<String>(), suggestions.list().map { it.suggestion() })
    }

    @Test
    fun `the player argument resolves online players`() {
        val player: Player = server.addPlayer("Alex")
        assertEquals(listOf("World default is not loaded."), execute("mf world transfer default ${player.name}"))
    }

    @Test
    fun `a world id is shown literally instead of as MiniMessage`() {
        assertEquals(
            listOf("Failed to create world <obf>x. See the server log."),
            execute("mf world create <obf>x void"),
        )
    }
}
