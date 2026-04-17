package party.morino.moripafishing.core.world.weather.provider

import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

class VanillaWeatherProviderTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun addWorld(name: String): WorldMock {
        val world = WorldMock()
        world.name = name
        server.addWorld(world)
        return world
    }

    @Test
    fun `returns SUNNY when world is not loaded`() {
        val provider = VanillaWeatherProvider(plugin)
        assertEquals(
            WeatherType.SUNNY,
            provider.getCurrentWeather(FishingWorldId("missing_world")),
        )
    }

    @Test
    fun `primes state from already-loaded worlds`() {
        val world = addWorld("vanilla_primed")
        world.setStorm(true)
        world.isThundering = false

        val provider = VanillaWeatherProvider(plugin)
        server.scheduler.performOneTick()

        assertEquals(
            WeatherType.RAINY,
            provider.getCurrentWeather(FishingWorldId("vanilla_primed")),
        )
    }

    @Test
    fun `reacts to WeatherChangeEvent`() {
        val world = addWorld("vanilla_rain_event")
        world.setStorm(false)
        world.isThundering = false

        val provider = VanillaWeatherProvider(plugin)
        server.scheduler.performOneTick()

        server.pluginManager.callEvent(WeatherChangeEvent(world, true))
        assertEquals(
            WeatherType.RAINY,
            provider.getCurrentWeather(FishingWorldId("vanilla_rain_event")),
        )
    }

    @Test
    fun `reacts to ThunderChangeEvent`() {
        val world = addWorld("vanilla_thunder_event")
        world.setStorm(true)
        world.isThundering = false

        val provider = VanillaWeatherProvider(plugin)
        server.scheduler.performOneTick()

        server.pluginManager.callEvent(ThunderChangeEvent(world, true))
        assertEquals(
            WeatherType.THUNDERSTORM,
            provider.getCurrentWeather(FishingWorldId("vanilla_thunder_event")),
        )
    }
}
