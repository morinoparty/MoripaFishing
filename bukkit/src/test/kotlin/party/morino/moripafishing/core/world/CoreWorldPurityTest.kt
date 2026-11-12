package party.morino.moripafishing.core.world

import org.bukkit.GameRule
import org.bukkit.Location
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.api.config.world.BorderConfig
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import java.io.File
import java.time.Instant

/**
 * Integration (サービス) が 1 件も登録されていないとき、コアが Bukkit ワールドの状態を一切変更しないことを確認する。
 */
class CoreWorldPurityTest {
    @TempDir
    lateinit var directory: File

    private lateinit var server: ServerMock
    private lateinit var world: WorldMock
    private lateinit var store: WorldConfigStore
    private lateinit var manager: WorldManagerImpl
    private val worldId = FishingWorldId("core_purity_test")

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        val plugin = MockBukkit.createMockPlugin()
        store = WorldConfigStore(directory, plugin.logger)
        store.save(FishingWorldConfig(worldId))
        world = mockWorld(WorldKeys.of(worldId))
        server.addWorld(world)
        // 適用されていれば変わるはずの値を事前に設定しておく
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true)
        world.time = 1234L
        world.setStorm(true)
        world.isThundering = true
        world.spawnLocation = Location(world, 100.0, 80.0, -100.0)
        world.worldBorder.size = 500.0
        world.worldBorder.setCenter(-20.0, 40.0)
        manager =
            WorldManagerImpl(plugin, FakeConfigManager(), store, MutableClock(Instant.parse("2026-11-12T00:00:00Z")))
        manager.enable()
        manager.start()
    }

    @AfterEach
    fun tearDown() {
        manager.disable()
        MockBukkit.unmock()
    }

    private fun assertWorldUntouched() {
        assertEquals(true, world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))
        assertEquals(true, world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))
        assertEquals(1234L, world.time)
        assertTrue(world.hasStorm())
        assertTrue(world.isThundering)
        assertEquals(100.0, world.spawnLocation.x)
        assertEquals(80.0, world.spawnLocation.y)
        assertEquals(-100.0, world.spawnLocation.z)
        assertEquals(500.0, world.worldBorder.size)
        assertEquals(-20.0, world.worldBorder.center.x)
        assertEquals(40.0, world.worldBorder.center.z)
    }

    @Test
    fun `start, refresh and config updates do not modify the bukkit world`() {
        manager.refresh()
        val config = manager.getWorld(worldId)!!.getConfig()
        assertTrue(
            manager.updateWorldConfig(
                config.copy(spawn = SpawnPoint(5.0, 70.0, 5.0, 90f, -10f), border = BorderConfig(10.0, 10.0, 30.0)),
            ),
        )
        assertWorldUntouched()
    }

    @Test
    fun `createWorld fails without a lifecycle and writes no file`() {
        assertFalse(manager.createWorld(FishingWorldId("new_world"), "void"))
        assertFalse(File(directory, "new_world.json").exists())
    }

    @Test
    fun `deleteWorld of a loaded world fails without a lifecycle and keeps the config`() {
        assertFalse(manager.deleteWorld(worldId))
        assertTrue(File(directory, "${worldId.value}.json").exists())
        assertNotNull(server.getWorld(WorldKeys.of(worldId)))
        assertWorldUntouched()
    }

    @Test
    fun `weather is still decided for fish conditions`() {
        assertNotNull(manager.getWorld(worldId)!!.getWeather())
    }
}
