package party.morino.moripafishing.core.world.weather.provider

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.weather.WeatherProviderRegistry
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

class ExternalWeatherProviderTest {
    private class CollectingHandler : Handler() {
        val records: MutableList<LogRecord> = mutableListOf()

        override fun publish(record: LogRecord) {
            records += record
        }

        override fun flush() = Unit

        override fun close() = Unit
    }

    private fun newLogger(): Pair<Logger, CollectingHandler> {
        val logger = Logger.getLogger("ExternalWeatherProviderTest-${System.nanoTime()}")
        logger.useParentHandlers = false
        val handler = CollectingHandler()
        logger.addHandler(handler)
        logger.level = Level.ALL
        return logger to handler
    }

    @Test
    fun `unregistered provider returns SUNNY and warns once per world`() {
        val (logger, handler) = newLogger()
        val registry = WeatherProviderRegistry()
        val provider = ExternalWeatherProvider(registry, logger)
        val id = FishingWorldId("external_w")

        assertEquals(WeatherType.SUNNY, provider.getCurrentWeather(id))
        assertEquals(WeatherType.SUNNY, provider.getCurrentWeather(id))

        val warnings = handler.records.filter { it.level == Level.WARNING }
        assertEquals(1, warnings.size, "warning should be emitted only once per world")
    }

    @Test
    fun `returns value from registered provider`() {
        val (logger, _) = newLogger()
        val registry = WeatherProviderRegistry()
        val id = FishingWorldId("external_w2")
        registry.register(id, WeatherProvider { WeatherType.THUNDERSTORM })

        val provider = ExternalWeatherProvider(registry, logger)
        assertEquals(WeatherType.THUNDERSTORM, provider.getCurrentWeather(id))
    }

    @Test
    fun `warning re-armed after unregister`() {
        val (logger, handler) = newLogger()
        val registry = WeatherProviderRegistry()
        val id = FishingWorldId("external_w3")
        val provider = ExternalWeatherProvider(registry, logger)

        registry.register(id, WeatherProvider { WeatherType.RAINY })
        assertEquals(WeatherType.RAINY, provider.getCurrentWeather(id))

        registry.unregister(id)
        assertEquals(WeatherType.SUNNY, provider.getCurrentWeather(id))
        assertEquals(WeatherType.SUNNY, provider.getCurrentWeather(id))

        val warnings = handler.records.filter { it.level == Level.WARNING }
        assertEquals(1, warnings.size, "single warning after unregister")
    }
}
