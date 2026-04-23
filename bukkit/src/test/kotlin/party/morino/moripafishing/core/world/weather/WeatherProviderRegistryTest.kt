package party.morino.moripafishing.core.world.weather

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

class WeatherProviderRegistryTest {
    @Test
    fun `registered provider is returned and removable`() {
        val registry = WeatherProviderRegistry()
        val id = FishingWorldId("world_a")
        val provider = WeatherProvider { WeatherType.RAINY }

        assertNull(registry.get(id))

        registry.register(id, provider)
        assertEquals(WeatherType.RAINY, registry.get(id)!!.getCurrentWeather(id))

        registry.unregister(id)
        assertNull(registry.get(id))
    }

    @Test
    fun `second register overwrites the first`() {
        val registry = WeatherProviderRegistry()
        val id = FishingWorldId("world_b")
        registry.register(id) { WeatherType.SUNNY }
        registry.register(id) { WeatherType.THUNDERSTORM }
        assertEquals(WeatherType.THUNDERSTORM, registry.get(id)!!.getCurrentWeather(id))
    }
}
