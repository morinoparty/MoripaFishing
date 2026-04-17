package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * `enableWeather: Boolean` (旧) -> `weatherMode: WeatherMode` (新) への自動マイグレーションを検証する。
 */
class ClimateConfigLegacyTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `legacy enableWeather true maps to INTERNAL`() {
        val input = """{"enableWeather": true, "enableDayCycle": true}"""
        val cfg = json.decodeFromString(ClimateConfig.serializer(), input)
        assertEquals(WeatherMode.INTERNAL, cfg.weatherMode)
    }

    @Test
    fun `legacy enableWeather false maps to VANILLA`() {
        val input = """{"enableWeather": false, "enableDayCycle": true}"""
        val cfg = json.decodeFromString(ClimateConfig.serializer(), input)
        assertEquals(WeatherMode.VANILLA, cfg.weatherMode)
    }

    @Test
    fun `new weatherMode takes precedence over legacy enableWeather`() {
        val input = """{"enableWeather": true, "weatherMode": "EXTERNAL"}"""
        val cfg = json.decodeFromString(ClimateConfig.serializer(), input)
        assertEquals(WeatherMode.EXTERNAL, cfg.weatherMode)
    }

    @Test
    fun `missing both defaults to INTERNAL`() {
        val input = """{"enableDayCycle": true}"""
        val cfg = json.decodeFromString(ClimateConfig.serializer(), input)
        assertEquals(WeatherMode.INTERNAL, cfg.weatherMode)
    }

    @Test
    fun `round-trip serializes weatherMode not enableWeather`() {
        val original = ClimateConfig(weatherMode = WeatherMode.VANILLA)
        val encoded = json.encodeToString(ClimateConfig.serializer(), original)
        assert(encoded.contains("\"weatherMode\":\"VANILLA\""))
        assert(!encoded.contains("enableWeather")) { "encoded: $encoded" }
    }
}
