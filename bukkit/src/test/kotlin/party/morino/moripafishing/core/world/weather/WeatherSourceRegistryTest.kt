package party.morino.moripafishing.core.world.weather

import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

class WeatherSourceRegistryTest {
    private class StubSource(
        override val key: Key,
    ) : WeatherSource {
        override val managesWorldWeather: Boolean = false
        override val usesVanillaWeatherCycle: Boolean = false

        override fun createProvider(worldId: FishingWorldId): WeatherProvider = WeatherProvider { WeatherType.SUNNY }
    }

    @Test
    fun `registers and resolves a source by its key`() {
        val registry = WeatherSourceRegistry()
        val source = StubSource(Key.key("test", "source"))
        registry.register(source)
        assertSame(source, registry.get(Key.key("test", "source")))
    }

    @Test
    fun `unregister removes the source`() {
        val registry = WeatherSourceRegistry()
        val key = Key.key("test", "source")
        registry.register(StubSource(key))
        registry.unregister(key)
        assertNull(registry.get(key))
    }

    @Test
    fun `re-registering the same key overwrites the previous source`() {
        val registry = WeatherSourceRegistry()
        val key = Key.key("test", "source")
        registry.register(StubSource(key))
        val replacement = StubSource(key)
        registry.register(replacement)
        assertSame(replacement, registry.get(key))
    }
}
