package party.morino.moripafishing

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.api.MoripaFishingAPIProvider
import party.morino.moripafishing.api.event.MoripaFishingReloadedEvent
import party.morino.moripafishing.testing.loadMoripaFishing

@ExtendWith(MockBukkitExtension::class)
class MoripaFishingPluginTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private lateinit var plugin: MoripaFishing

    @BeforeEach
    fun setUp() {
        plugin = server.loadMoripaFishing()
    }

    @Test
    fun `the API is available only while enabled`() {
        assertNotNull(MoripaFishingAPIProvider.getOrNull())
        server.pluginManager.disablePlugin(plugin)
        assertNull(MoripaFishingAPIProvider.getOrNull())
    }

    @Test
    fun `fixture fish and rarities are loaded`() {
        val api = MoripaFishingAPIProvider.get()
        assertEquals(12, api.getFishManager().getAll().size)
        assertEquals(6, api.getRarityManager().getAll().size)
        assertEquals(listOf("default"), api.getWorldManager().getWorlds().map { it.getId().value })
    }

    @Test
    fun `reload fires the reloaded event exactly once`() {
        val events = mutableListOf<MoripaFishingReloadedEvent>()
        server.pluginManager.registerEvents(
            object : Listener {
                @EventHandler
                fun onReloaded(event: MoripaFishingReloadedEvent) {
                    events += event
                }
            },
            plugin,
        )
        plugin.core().reload()
        assertEquals(1, events.size)
        assertEquals(
            12,
            MoripaFishingAPIProvider
                .get()
                .getFishManager()
                .getAll()
                .size,
        )
    }
}
