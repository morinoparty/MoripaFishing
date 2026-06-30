package party.morino.moripafishing.integrations.worldlifecycle

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.integrations.worldlifecycle.api.GeneratorData

class MoripaFishingWorldLifecyclePluginTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MoripaFishingWorldLifecyclePlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(MoripaFishingWorldLifecyclePlugin::class.java)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `loads bundled default generators on enable`() {
        val ids = plugin.listGenerators().map { it.id }.toSet()
        assertTrue(ids.containsAll(setOf("terra", "void", "normal")), "missing default generators: $ids")
    }

    @Test
    fun `getGenerator returns bundled void definition`() {
        val data = plugin.getGenerator("void")
        assertNotNull(data)
    }

    @Test
    fun `getGenerator returns null for unknown id`() {
        assertNull(plugin.getGenerator("nonexistent"))
    }

    @Test
    fun `addGenerator persists and becomes discoverable`() {
        val custom =
            GeneratorData(
                id = "custom_test_gen",
                generator = null,
                type = "NORMAL",
                biomeProvider = null,
                generatorSetting = null,
            )
        plugin.addGenerator(custom)
        val found = plugin.getGenerator("custom_test_gen")
        assertNotNull(found)
        assertEquals("NORMAL", found!!.type)
    }
}
