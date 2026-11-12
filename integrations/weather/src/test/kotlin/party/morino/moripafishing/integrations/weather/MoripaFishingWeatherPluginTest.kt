package party.morino.moripafishing.integrations.weather

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier

class MoripaFishingWeatherPluginTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MoripaFishingWeatherPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        // plugin.yml の depend を満たすため、コアの代わりのプラグインを先に用意する
        MockBukkit.createMockPlugin("MoripaFishing")
        plugin = MockBukkit.load(MoripaFishingWeatherPlugin::class.java)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `registers a WorldStateApplier`() {
        val registrations = server.servicesManager.getRegistrations(WorldStateApplier::class.java)
        assertEquals(listOf(plugin), registrations.map { it.plugin })
        assertTrue(registrations.single().provider is WeatherApplier)
    }

    @Test
    fun `disabling the plugin removes the registration`() {
        server.pluginManager.disablePlugin(plugin)
        assertTrue(server.servicesManager.getRegistrations(WorldStateApplier::class.java).isEmpty())
    }
}
