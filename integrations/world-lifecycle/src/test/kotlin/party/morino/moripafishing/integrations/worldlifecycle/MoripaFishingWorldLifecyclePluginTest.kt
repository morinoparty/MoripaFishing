package party.morino.moripafishing.integrations.worldlifecycle

import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.integrations.worldlifecycle.api.GeneratorData
import java.io.File

class MoripaFishingWorldLifecyclePluginTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MoripaFishingWorldLifecyclePlugin
    private val worldKey = "moripafishing:fishing"

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

    @Test
    fun `creates default join-teleport config on enable`() {
        val file = File(plugin.dataFolder, "join-teleport.yml")
        assertTrue(file.exists(), "join-teleport.yml should be created with defaults")
    }

    private fun addWorld(): WorldMock {
        val world = WorldMock(WorldCreator(NamespacedKey.fromString(worldKey)!!))
        server.addWorld(world)
        return world
    }

    private fun tick() {
        server.scheduler.performOneTick()
    }

    @Test
    fun `setDaylightCycle updates the daylight cycle gamerule`() {
        val world = addWorld()
        plugin.setDaylightCycle(worldKey, false)
        tick()
        assertEquals(false, world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))

        plugin.setDaylightCycle(worldKey, true)
        tick()
        assertEquals(true, world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))
    }

    @Test
    fun `setTime updates the world time`() {
        val world = addWorld()
        world.time = 0L
        plugin.setTime(worldKey, 6000L)
        tick()
        assertEquals(6000L, world.time)
    }

    @Test
    fun `setSpawnLocation updates the world spawn`() {
        val world = addWorld()
        plugin.setSpawnLocation(worldKey, 10.0, 70.0, -3.0, 45f, 10f)
        tick()
        val spawn = world.spawnLocation
        assertEquals(10.0, spawn.x)
        assertEquals(70.0, spawn.y)
        assertEquals(-3.0, spawn.z)
    }

    @Test
    fun `unloadBukkitWorld unloads the world`() {
        addWorld()
        assertTrue(plugin.unloadBukkitWorld(worldKey, false))
        assertNull(Bukkit.getWorld(NamespacedKey.fromString(worldKey)!!))
    }

    @Test
    fun `unloadBukkitWorld returns false for unknown worlds`() {
        assertFalse(plugin.unloadBukkitWorld("moripafishing:missing", false))
    }
}
