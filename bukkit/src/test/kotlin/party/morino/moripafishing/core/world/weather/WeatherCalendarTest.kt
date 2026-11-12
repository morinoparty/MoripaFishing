package party.morino.moripafishing.core.world.weather

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import party.morino.moripafishing.api.config.climate.WeatherPolicy
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class WeatherCalendarTest {
    private val zone = ZoneId.of("Asia/Tokyo")
    private val worldId = FishingWorldId("default")

    private fun at(text: String): ZonedDateTime = LocalDateTime.parse(text).atZone(zone)

    @Test
    fun `same inputs always give the same weather`() {
        val time = at("2025-06-15T13:30")
        val first = WeatherCalendar(WeatherPolicy.Random(), worldId).at(time)
        repeat(10) { assertEquals(first, WeatherCalendar(WeatherPolicy.Random(), worldId).at(time)) }
    }

    @Test
    fun `pins the weather for the default settings`() {
        // 既定設定・seed "pepper" で記録した値。ノイズの計算を変えた場合は意図した変更かを確認してから更新する
        val calendar = WeatherCalendar(WeatherPolicy.Random(), worldId)
        assertEquals(WeatherType.SUNNY, calendar.at(at("2024-01-01T00:00")))
        assertEquals(WeatherType.RAINY, calendar.at(at("2024-03-15T09:00")))
        assertEquals(WeatherType.CLOUDY, calendar.at(at("2026-11-12T07:00")))
        assertEquals(276L, calendar.slotOf(at("2024-01-01T00:00")))
        assertEquals(499L, calendar.slotOf(at("2024-03-15T09:00")))
        assertEquals(3414L, calendar.slotOf(at("2026-11-12T07:00")))
    }

    @Test
    fun `noise stays within 0 to 100`() {
        listOf(1, 30, 100).forEach { inclination ->
            val calendar = WeatherCalendar(WeatherPolicy.Random(maxInclination = inclination), worldId)
            (-1_000L..1_000L).forEach { slot ->
                val noise = calendar.noise(slot)
                assertTrue(noise in 0L..100L, "noise $noise of slot $slot is out of range")
            }
        }
    }

    @Test
    fun `intervalHours changes the slot length`() {
        val calendar = WeatherCalendar(WeatherPolicy.Random(intervalHours = 1), worldId)
        assertEquals(0L, calendar.slotOf(at("2023-10-01T00:59")))
        assertEquals(1L, calendar.slotOf(at("2023-10-01T01:00")))
        assertEquals(24L, calendar.slotOf(at("2023-10-02T00:00")))
    }

    @Test
    fun `offsetHours shifts the slot boundaries`() {
        val positive = WeatherCalendar(WeatherPolicy.Random(offsetHours = 2), worldId)
        assertEquals(-1L, positive.slotOf(at("2023-10-01T01:59")))
        assertEquals(0L, positive.slotOf(at("2023-10-01T02:00")))
        assertEquals(1L, positive.slotOf(at("2023-10-01T10:00")))

        val negative = WeatherCalendar(WeatherPolicy.Random(offsetHours = -2), worldId)
        assertEquals(0L, negative.slotOf(at("2023-10-01T05:59")))
        assertEquals(1L, negative.slotOf(at("2023-10-01T06:00")))
    }

    @Test
    fun `slots before the origin are floored`() {
        val calendar = WeatherCalendar(WeatherPolicy.Random(), worldId)
        assertEquals(-1L, calendar.slotOf(at("2023-09-30T23:00")))
    }

    @Test
    fun `distribution roughly follows the weights`() {
        val policy = WeatherPolicy.Random(weights = mapOf(WeatherType.SUNNY to 3, WeatherType.RAINY to 1))
        val counts = (0L until 1000L).groupingBy { WeatherCalendar(policy, worldId).weatherOfSlot(it) }.eachCount()
        val sunny = counts[WeatherType.SUNNY] ?: 0
        assertEquals(setOf(WeatherType.SUNNY, WeatherType.RAINY), counts.keys)
        assertTrue(sunny in 600..900, "SUNNY count $sunny is far from 750")
    }

    @Test
    fun `a single weight always gives that weather`() {
        val policy = WeatherPolicy.Random(weights = mapOf(WeatherType.FOGGY to 1))
        val calendar = WeatherCalendar(policy, worldId)
        (0L until 100L).forEach { assertEquals(WeatherType.FOGGY, calendar.weatherOfSlot(it)) }
    }

    @Test
    fun `invalid settings are rejected`() {
        assertThrows<IllegalArgumentException> { WeatherPolicy.Random(weights = emptyMap()) }
        assertThrows<IllegalArgumentException> { WeatherPolicy.Random(weights = mapOf(WeatherType.SUNNY to 0)) }
        assertThrows<IllegalArgumentException> { WeatherPolicy.Random(weights = mapOf(WeatherType.SUNNY to -1)) }
        assertThrows<IllegalArgumentException> { WeatherPolicy.Random(intervalHours = 0) }
        assertThrows<IllegalArgumentException> { WeatherPolicy.Random(maxInclination = 0) }
        assertThrows<IllegalArgumentException> { WeatherPolicy.Random(maxInclination = 101) }
    }
}
