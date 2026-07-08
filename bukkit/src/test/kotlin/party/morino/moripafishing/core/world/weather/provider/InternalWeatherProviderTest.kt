package party.morino.moripafishing.core.world.weather.provider

import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `returns the randomizer's current weather`() {
        val provider = InternalWeatherProvider(FixedRandomizer(WeatherType.CLOUDY))
        assertEquals(WeatherType.CLOUDY, provider.getCurrentWeather(FishingWorldId("w")))
    }

    @Test
    fun `follows the randomizer on every call`() {
        var current = WeatherType.SUNNY
        val randomizer =
            object : WeatherRandomizer {
                override fun drawWeather(): WeatherType = current

                override fun drawWeatherForecast(limit: Int): List<WeatherType> = List(limit) { current }
            }
        val provider = InternalWeatherProvider(randomizer)
        assertEquals(WeatherType.SUNNY, provider.getCurrentWeather(FishingWorldId("w")))
        current = WeatherType.RAINY
        assertEquals(WeatherType.RAINY, provider.getCurrentWeather(FishingWorldId("w")))
    }
}
