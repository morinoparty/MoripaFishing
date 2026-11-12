package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.climate.TimePolicy
import party.morino.moripafishing.api.config.climate.WeatherPolicy
import party.morino.moripafishing.api.config.world.BorderConfig
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.spi.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.weather.WeatherCalendar
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Stream

class FishingWorldImplTest {
    private class FakeContext(
        private val now: ZonedDateTime,
        private val vanilla: WeatherType = WeatherType.RAINY,
        private val sources: Map<Key, WeatherSource> = emptyMap(),
        private val defaultClimate: ClimateConfig = ClimateConfig(),
    ) : WorldContext {
        override fun now(): ZonedDateTime = now

        override fun defaultClimate(): ClimateConfig = defaultClimate

        override fun vanillaWeather(worldKey: Key): WeatherType = vanilla

        override fun externalWeather(
            source: Key,
            worldId: FishingWorldId,
            worldKey: Key,
        ): WeatherDecision? = sources[source]?.let { WeatherDecision(it.getWeather(worldId, worldKey), it.appliesToWorld) }
    }

    private class FakeSource(
        override val key: Key,
        override val appliesToWorld: Boolean,
        private val weather: WeatherType,
    ) : WeatherSource {
        override fun getWeather(
            worldId: FishingWorldId,
            worldKey: Key,
        ): WeatherType = weather
    }

    companion object {
        private val NOW: ZonedDateTime = LocalDateTime.parse("2026-11-12T07:00").atZone(ZoneId.of("Asia/Tokyo"))
        private val ID = FishingWorldId("table")
        private val APPLIED = Key.key("test", "applied")
        private val READ_ONLY = Key.key("test", "read_only")
        private val MISSING = Key.key("test", "missing")
        private val SOURCES =
            mapOf(
                APPLIED to FakeSource(APPLIED, true, WeatherType.SNOWY),
                READ_ONLY to FakeSource(READ_ONLY, false, WeatherType.FOGGY),
            )
        private val RANDOM_DEFAULT = WeatherCalendar(WeatherPolicy.Random(), ID).at(NOW)

        /** 天候の方針, 期待する魚の天候, 期待する WorldState.weather */
        @JvmStatic
        fun weatherCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(WeatherPolicy.Random(), RANDOM_DEFAULT, RANDOM_DEFAULT),
                Arguments.of(WeatherPolicy.Fixed(WeatherType.CLOUDY), WeatherType.CLOUDY, WeatherType.CLOUDY),
                Arguments.of(WeatherPolicy.Vanilla, WeatherType.RAINY, null),
                Arguments.of(WeatherPolicy.External(APPLIED), WeatherType.SNOWY, WeatherType.SNOWY),
                Arguments.of(WeatherPolicy.External(READ_ONLY), WeatherType.FOGGY, null),
                Arguments.of(WeatherPolicy.External(MISSING), RANDOM_DEFAULT, RANDOM_DEFAULT),
            )

        /** 時刻の方針, 期待する WorldState.time */
        @JvmStatic
        fun timeCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(TimePolicy.RealTime(), 1000L),
                Arguments.of(TimePolicy.Fixed(18), 12000L),
                Arguments.of(TimePolicy.Vanilla, null),
            )
    }

    private fun world(
        climate: ClimateConfig?,
        border: BorderConfig? = BorderConfig(),
        defaultClimate: ClimateConfig = ClimateConfig(),
    ) = FishingWorldImpl(
        FishingWorldConfig(id = ID, border = border, climate = climate, spawn = SpawnPoint(1.0, 2.0, 3.0, 4f, 5f)),
        FakeContext(NOW, sources = SOURCES, defaultClimate = defaultClimate),
    )

    @ParameterizedTest
    @MethodSource("weatherCases")
    fun `weather policy decides fish weather and applied weather`(
        policy: WeatherPolicy,
        fishWeather: WeatherType,
        appliedWeather: WeatherType?,
    ) {
        val world = world(ClimateConfig(weather = policy))
        val state = world.desiredState(NOW)
        assertEquals(fishWeather, world.decideWeather(NOW).weather)
        assertEquals(appliedWeather, state.weather)
        assertEquals(state.weather == null, state.doWeatherCycle)
    }

    @ParameterizedTest
    @MethodSource("timeCases")
    fun `time policy decides the world time`(
        policy: TimePolicy,
        expected: Long?,
    ) {
        val state = world(ClimateConfig(time = policy)).desiredState(NOW)
        assertEquals(expected, state.time)
        assertEquals(state.time == null, state.doDaylightCycle)
    }

    @Test
    fun `getWeather uses the context clock`() {
        val world = world(ClimateConfig(weather = WeatherPolicy.Fixed(WeatherType.THUNDERSTORM)))
        assertEquals(WeatherType.THUNDERSTORM, world.getWeather())
    }

    @Test
    fun `border and spawn are copied from the config`() {
        val managed = world(null, border = BorderConfig(1.0, 2.0, 30.0)).desiredState(NOW)
        assertEquals(BorderConfig(1.0, 2.0, 30.0), managed.border)
        assertEquals(SpawnPoint(1.0, 2.0, 3.0, 4f, 5f), managed.spawn)
        assertEquals(Key.key("moripafishing", "table"), managed.worldKey)

        assertNull(world(null, border = null).desiredState(NOW).border)
    }

    @Test
    fun `null climate falls back to the default climate`() {
        val defaultClimate = ClimateConfig(WeatherPolicy.Fixed(WeatherType.SNOWY), TimePolicy.Vanilla)
        val world = world(null, defaultClimate = defaultClimate)
        val state = world.desiredState(NOW)
        assertEquals(WeatherType.SNOWY, state.weather)
        assertNull(state.time)
    }

    @Test
    fun `desiredState uses a given weather decision instead of deciding again`() {
        val world = world(ClimateConfig(weather = WeatherPolicy.Fixed(WeatherType.SUNNY)))
        val state = world.desiredState(NOW, WeatherDecision(WeatherType.RAINY, appliesToWorld = true))
        assertEquals(WeatherType.RAINY, state.weather)
        assertEquals(false, state.doWeatherCycle)
    }
}
