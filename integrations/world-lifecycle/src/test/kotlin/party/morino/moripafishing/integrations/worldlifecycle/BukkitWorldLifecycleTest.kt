package party.morino.moripafishing.integrations.worldlifecycle

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.integrations.worldlifecycle.generator.GeneratorRepository
import java.io.File
import java.util.logging.Logger

class BukkitWorldLifecycleTest {
    @TempDir
    lateinit var directory: File

    private lateinit var server: ServerMock
    private lateinit var lifecycle: BukkitWorldLifecycle
    private val worldKey = Key.key("moripafishing", "fishing")

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        val logger = Logger.getLogger("BukkitWorldLifecycleTest")
        val repository = GeneratorRepository(directory, logger)
        File(directory, "broken.json").writeText("{ not json")
        repository.load()
        lifecycle = BukkitWorldLifecycle(repository, logger)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun addWorld() {
        server.addWorld(WorldMock(WorldCreator(NamespacedKey("moripafishing", "fishing"))))
    }

    @Test
    fun `bundled generators are available and broken files are skipped`() {
        assertEquals(setOf("terra", "void", "normal"), lifecycle.getGeneratorIds())
        assertTrue(File(directory, "void.json").exists())
    }

    @Test
    fun `an unknown generator is rejected`() {
        assertFalse(lifecycle.createWorld(worldKey, "nonexistent"))
    }

    @Test
    fun `an existing world is not created again`() {
        addWorld()
        assertFalse(lifecycle.createWorld(worldKey, "void"))
    }

    @Test
    fun `unloadWorld unloads a loaded world`() {
        addWorld()
        assertTrue(lifecycle.unloadWorld(worldKey))
        assertNull(server.getWorld(worldKey))
    }

    @Test
    fun `unloadWorld returns false for a missing world`() {
        assertFalse(lifecycle.unloadWorld(Key.key("moripafishing", "missing")))
    }
}
