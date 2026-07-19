package party.morino.moripafishing.integrations.weather

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock

class MoripaFishingWeatherPluginTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MoripaFishingWeatherPlugin
    private lateinit var world: WorldMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(MoripaFishingWeatherPlugin::class.java)
        world = server.addSimpleWorld("fishing")
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun tick() {
        server.scheduler.performOneTick()
    }

    @Test
    fun `rain applies storm without thunder`() {
        plugin.applyWeather("fishing", "RAINY")
        tick()
        assertTrue(world.hasStorm())
        assertFalse(world.isThundering)
    }

    @Test
    fun `thunderstorm applies storm with thunder`() {
        plugin.applyWeather("fishing", "THUNDERSTORM")
        tick()
        assertTrue(world.hasStorm())
        assertTrue(world.isThundering)
    }

    @Test
    fun `cloudy applies storm without touching the world blocks or biome`() {
        val biomeBefore = world.getBiome(0, world.seaLevel, 0)
        plugin.applyWeather("fishing", "CLOUDY")
        tick()
        assertTrue(world.hasStorm())
        assertFalse(world.isThundering)
        // 実ブロックもバイオームも変更しない (天井はクライアント側のみ)
        assertEquals(biomeBefore, world.getBiome(0, world.seaLevel, 0))
    }

    @Test
    fun `sunny clears the sky`() {
        plugin.applyWeather("fishing", "THUNDERSTORM")
        tick()
        plugin.applyWeather("fishing", "SUNNY")
        tick()
        assertFalse(world.hasStorm())
        assertFalse(world.isThundering)
    }

    @Test
    fun `unknown weather value falls back to clear`() {
        plugin.applyWeather("fishing", "THUNDERSTORM")
        tick()
        plugin.applyWeather("fishing", "NOT_A_WEATHER")
        tick()
        assertFalse(world.hasStorm())
        assertFalse(world.isThundering)
    }

    @Test
    fun `reset clears the sky`() {
        plugin.applyWeather("fishing", "THUNDERSTORM")
        tick()
        plugin.resetWeather("fishing")
        tick()
        assertFalse(world.hasStorm())
        assertFalse(world.isThundering)
    }
}
