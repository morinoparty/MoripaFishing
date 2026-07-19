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
    fun `classify maps vanilla flags correctly`() {
        assertEquals(WeatherType.SUNNY, VanillaWeatherProvider.classify(storming = false, thundering = false))
        assertEquals(WeatherType.RAINY, VanillaWeatherProvider.classify(storming = true, thundering = false))
        assertEquals(WeatherType.THUNDERSTORM, VanillaWeatherProvider.classify(storming = true, thundering = true))
        // 雷フラグのみではバニラは雨を描画しないため SUNNY 扱い
        assertEquals(WeatherType.SUNNY, VanillaWeatherProvider.classify(storming = false, thundering = true))
    }

    @Test
    fun `returns SUNNY when world is not loaded`() {
        val provider = VanillaWeatherProvider(plugin, FishingWorldId("missing_world"))
        assertEquals(
            WeatherType.SUNNY,
            provider.getCurrentWeather(FishingWorldId("missing_world")),
        )
    }

    @Test
    fun `primes state from the target world`() {
        val world = addWorld("vanilla_primed")
        world.setStorm(true)
        world.isThundering = false

        val provider = VanillaWeatherProvider(plugin, FishingWorldId("vanilla_primed"))
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

        val provider = VanillaWeatherProvider(plugin, FishingWorldId("vanilla_rain_event"))
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

        val provider = VanillaWeatherProvider(plugin, FishingWorldId("vanilla_thunder_event"))
        server.scheduler.performOneTick()

        server.pluginManager.callEvent(ThunderChangeEvent(world, true))
        assertEquals(
            WeatherType.THUNDERSTORM,
            provider.getCurrentWeather(FishingWorldId("vanilla_thunder_event")),
        )
    }

    @Test
    fun `storm ending clears thunderstorm even while the thunder flag is stale`() {
        val world = addWorld("vanilla_storm_end")
        world.setStorm(true)
        world.isThundering = true

        val provider = VanillaWeatherProvider(plugin, FishingWorldId("vanilla_storm_end"))
        server.scheduler.performOneTick()
        assertEquals(
            WeatherType.THUNDERSTORM,
            provider.getCurrentWeather(FishingWorldId("vanilla_storm_end")),
        )

        // 雨が止んだ時点で thunder フラグが残っていても、描画上は晴れになる
        server.pluginManager.callEvent(WeatherChangeEvent(world, false))
        assertEquals(
            WeatherType.SUNNY,
            provider.getCurrentWeather(FishingWorldId("vanilla_storm_end")),
        )
    }

    @Test
    fun `ignores events for other worlds`() {
        val target = addWorld("vanilla_target")
        target.setStorm(false)
        target.isThundering = false
        val other = addWorld("vanilla_other")

        val provider = VanillaWeatherProvider(plugin, FishingWorldId("vanilla_target"))
        server.scheduler.performOneTick()

        server.pluginManager.callEvent(WeatherChangeEvent(other, true))
        assertEquals(
            WeatherType.SUNNY,
            provider.getCurrentWeather(FishingWorldId("vanilla_target")),
        )
    }

    @Test
    fun `dispose stops tracking events`() {
        val world = addWorld("vanilla_disposed")
        world.setStorm(false)
        world.isThundering = false

        val provider = VanillaWeatherProvider(plugin, FishingWorldId("vanilla_disposed"))
        server.scheduler.performOneTick()
        provider.dispose()

        server.pluginManager.callEvent(WeatherChangeEvent(world, true))
        assertEquals(
            WeatherType.SUNNY,
            provider.getCurrentWeather(FishingWorldId("vanilla_disposed")),
        )
    }
}
