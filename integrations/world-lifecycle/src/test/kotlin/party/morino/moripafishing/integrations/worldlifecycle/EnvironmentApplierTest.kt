package party.morino.moripafishing.integrations.worldlifecycle

import net.kyori.adventure.key.Key
import org.bukkit.GameRules
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.api.config.world.BorderConfig
import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint

class EnvironmentApplierTest {
    private lateinit var server: ServerMock
    private lateinit var world: WorldMock
    private val applier = EnvironmentApplier()
    private val worldKey = Key.key("moripafishing", "fishing")

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        world = WorldMock(WorldCreator(NamespacedKey("moripafishing", "fishing")))
        server.addWorld(world)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun state(
        time: Long? = 6000L,
        border: BorderConfig? = BorderConfig(10.0, -10.0, 50.0),
        spawn: SpawnPoint = SpawnPoint(10.0, 70.0, -3.0, 45f, 10f),
        key: Key = worldKey,
    ) = WorldState(
        worldId = FishingWorldId(key.value()),
        worldKey = key,
        weather = null,
        doWeatherCycle = true,
        time = time,
        doDaylightCycle = time == null,
        border = border,
        spawn = spawn,
    )

    @Test
    fun `daylight cycle gamerule follows the state`() {
        applier.apply(state(time = 6000L))
        assertEquals(false, world.getGameRuleValue(GameRules.ADVANCE_TIME))
        applier.apply(state(time = null))
        assertEquals(true, world.getGameRuleValue(GameRules.ADVANCE_TIME))
    }

    @Test
    fun `time is set only when present`() {
        world.time = 1234L
        applier.apply(state(time = null))
        assertEquals(1234L, world.time)
        applier.apply(state(time = 6000L))
        assertEquals(6000L, world.time)
    }

    @Test
    fun `border is set only when present`() {
        applier.apply(state(border = BorderConfig(10.0, -10.0, 50.0)))
        assertEquals(50.0, world.worldBorder.size)
        assertEquals(10.0, world.worldBorder.center.x)
        assertEquals(-10.0, world.worldBorder.center.z)

        world.worldBorder.size = 500.0
        applier.apply(state(border = null))
        assertEquals(500.0, world.worldBorder.size)
    }

    @Test
    fun `spawn is set from the state`() {
        applier.apply(state())
        val spawn = world.spawnLocation
        assertEquals(10.0, spawn.x)
        assertEquals(70.0, spawn.y)
        assertEquals(-3.0, spawn.z)
    }

    @Test
    fun `a spawn read back at block precision matches the fractional spawn`() {
        // Paper が読み戻すスポーン (ブロック座標・yaw のみ、pitch は 0) と、設定の端数付きスポーン
        val readBack = Location(world, 10.0, 70.0, -3.0, 45f, 0f)
        assertTrue(EnvironmentApplier.isSameSpawn(readBack, Location(world, 10.4, 70.2, -2.6, 45f, 10f)))
    }

    @Test
    fun `a different block or yaw is a different spawn`() {
        val readBack = Location(world, 10.0, 70.0, -3.0, 45f, 0f)
        assertFalse(EnvironmentApplier.isSameSpawn(readBack, Location(world, 10.0, 70.0, -3.0, 90f, 0f)))
        assertFalse(EnvironmentApplier.isSameSpawn(readBack, Location(world, 11.0, 70.0, -3.0, 45f, 0f)))
        assertFalse(EnvironmentApplier.isSameSpawn(readBack, Location(world, 10.0, 70.0, -3.5, 45f, 0f)))
    }

    @Test
    fun `an unknown world is ignored`() {
        world.spawnLocation = Location(world, 1.0, 2.0, 3.0)
        applier.apply(state(key = Key.key("moripafishing", "missing")))
        assertEquals(1.0, world.spawnLocation.x)
    }
}
