package party.morino.moripafishing.integrations.worldlifecycle

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import java.io.File

class MoripaFishingWorldLifecyclePluginTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MoripaFishingWorldLifecyclePlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        // paper-plugin.yml の必須依存 (MoripaFishing) を満たすため、コアの代わりのプラグインを先に用意する
        MockBukkit.createMockPlugin("MoripaFishing")
        plugin = MockBukkit.load(MoripaFishingWorldLifecyclePlugin::class.java)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `registers the lifecycle and the environment applier`() {
        val lifecycle = server.servicesManager.getRegistration(WorldLifecycle::class.java)
        assertEquals(plugin, lifecycle?.plugin)
        assertTrue(lifecycle?.provider is BukkitWorldLifecycle)
        val appliers = server.servicesManager.getRegistrations(WorldStateApplier::class.java)
        assertEquals(listOf(plugin), appliers.map { it.plugin })
        assertTrue(appliers.single().provider is EnvironmentApplier)
    }

    @Test
    fun `writes the default join-teleport config on enable`() {
        assertTrue(File(plugin.dataFolder, "join-teleport.yml").exists())
    }

    @Test
    fun `disabling the plugin removes the registrations`() {
        server.pluginManager.disablePlugin(plugin)
        assertTrue(server.servicesManager.getRegistrations(WorldLifecycle::class.java).isEmpty())
        assertTrue(server.servicesManager.getRegistrations(WorldStateApplier::class.java).isEmpty())
    }
}
