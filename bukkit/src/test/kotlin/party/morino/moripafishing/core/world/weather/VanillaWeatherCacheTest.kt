package party.morino.moripafishing.core.world.weather

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.world.WorldLoadEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.api.model.world.WeatherType

class VanillaWeatherCacheTest {
    private lateinit var server: ServerMock
    private lateinit var cache: VanillaWeatherCache

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        cache = VanillaWeatherCache()
        server.pluginManager.registerEvents(cache, MockBukkit.createMockPlugin())
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun addWorld(
        name: String,
        storm: Boolean = false,
        thunder: Boolean = false,
    ): WorldMock {
        val world = WorldMock(WorldCreator(NamespacedKey("moripafishing", name)))
        world.setStorm(storm)
        world.isThundering = thunder
        server.addWorld(world)
        return world
    }

    private fun key(name: String): Key = Key.key("moripafishing", name)

    @Test
    fun `classify maps vanilla flags correctly`() {
        assertEquals(WeatherType.SUNNY, VanillaWeatherCache.classify(storming = false, thundering = false))
        assertEquals(WeatherType.RAINY, VanillaWeatherCache.classify(storming = true, thundering = false))
        assertEquals(WeatherType.THUNDERSTORM, VanillaWeatherCache.classify(storming = true, thundering = true))
        // 雷フラグのみではバニラは雨を描画しないため SUNNY 扱い
        assertEquals(WeatherType.SUNNY, VanillaWeatherCache.classify(storming = false, thundering = true))
    }

    @Test
    fun `unknown worlds are sunny`() {
        assertEquals(WeatherType.SUNNY, cache.get(key("missing")))
    }

    @Test
    fun `primeAll reads the loaded worlds`() {
        addWorld("primed", storm = true)
        cache.primeAll(server.worlds)
        assertEquals(WeatherType.RAINY, cache.get(key("primed")))
    }

    @Test
    fun `WorldLoadEvent primes the snapshot`() {
        val world = addWorld("loaded", storm = true, thunder = true)
        server.pluginManager.callEvent(WorldLoadEvent(world))
        assertEquals(WeatherType.THUNDERSTORM, cache.get(key("loaded")))
    }

    @Test
    fun `reacts to WeatherChangeEvent`() {
        val world = addWorld("rain")
        cache.primeAll(server.worlds)
        server.pluginManager.callEvent(WeatherChangeEvent(world, true))
        assertEquals(WeatherType.RAINY, cache.get(key("rain")))
    }

    @Test
    fun `reacts to ThunderChangeEvent`() {
        val world = addWorld("thunder", storm = true)
        cache.primeAll(server.worlds)
        server.pluginManager.callEvent(ThunderChangeEvent(world, true))
        assertEquals(WeatherType.THUNDERSTORM, cache.get(key("thunder")))
    }

    @Test
    fun `storm ending clears thunderstorm even while the thunder flag is stale`() {
        val world = addWorld("storm_end", storm = true, thunder = true)
        cache.primeAll(server.worlds)
        assertEquals(WeatherType.THUNDERSTORM, cache.get(key("storm_end")))
        // 雨が止んだ時点で thunder フラグが残っていても、描画上は晴れになる
        server.pluginManager.callEvent(WeatherChangeEvent(world, false))
        assertEquals(WeatherType.SUNNY, cache.get(key("storm_end")))
    }

    @Test
    fun `events only update their own world`() {
        addWorld("target")
        val other = addWorld("other")
        cache.primeAll(server.worlds)
        server.pluginManager.callEvent(WeatherChangeEvent(other, true))
        assertEquals(WeatherType.SUNNY, cache.get(key("target")))
        assertEquals(WeatherType.RAINY, cache.get(key("other")))
    }

    @Test
    fun `unloading removes the snapshot`() {
        val world = addWorld("unloaded", storm = true)
        cache.primeAll(server.worlds)
        server.unloadWorld(world, false)
        assertEquals(WeatherType.SUNNY, cache.get(key("unloaded")))
    }
}
