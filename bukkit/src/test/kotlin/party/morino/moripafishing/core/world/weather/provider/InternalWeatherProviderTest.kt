package party.morino.moripafishing.core.world.weather.provider

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

class InternalWeatherProviderTest {
    private class FixedRandomizer(
        private val value: WeatherType,
    ) : WeatherRandomizer {
        override fun drawWeather(): WeatherType = value

        override fun drawWeatherForecast(limit: Int): List<WeatherType> = List(limit) { value }
    }

    @Test
    fun `falls back to randomizer when no applied weather is cached`() {
        val provider = InternalWeatherProvider(FixedRandomizer(WeatherType.CLOUDY))
        assertEquals(WeatherType.CLOUDY, provider.getCurrentWeather(FishingWorldId("w")))
    }

    @Test
    fun `prefers applied cached weather over randomizer`() {
        val provider = InternalWeatherProvider(FixedRandomizer(WeatherType.CLOUDY))
        provider.applyWeather(WeatherType.RAINY)
        assertEquals(WeatherType.RAINY, provider.getCurrentWeather(FishingWorldId("w")))
    }

    @Test
    fun `applyWeather reports whether the cached value changed`() {
        val provider = InternalWeatherProvider(FixedRandomizer(WeatherType.CLOUDY))
        assertTrue(provider.applyWeather(WeatherType.RAINY))
        assertFalse(provider.applyWeather(WeatherType.RAINY))
        assertTrue(provider.applyWeather(WeatherType.THUNDERSTORM))
    }
}
