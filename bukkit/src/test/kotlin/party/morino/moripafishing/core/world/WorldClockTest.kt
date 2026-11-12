package party.morino.moripafishing.core.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import party.morino.moripafishing.api.config.climate.TimePolicy
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class WorldClockTest {
    private fun at(text: String): ZonedDateTime = LocalDateTime.parse(text).atZone(ZoneId.of("Asia/Tokyo"))

    @Test
    fun `real time maps the clock to world ticks`() {
        // 0 tick = 6:00, 18000 tick = 0:00
        assertEquals(18000L, WorldClock.ticks(TimePolicy.RealTime(), at("2025-01-01T00:00")))
        assertEquals(0L, WorldClock.ticks(TimePolicy.RealTime(), at("2025-01-01T06:00")))
        assertEquals(6500L, WorldClock.ticks(TimePolicy.RealTime(), at("2025-01-01T12:30")))
        assertEquals(17983L, WorldClock.ticks(TimePolicy.RealTime(), at("2025-01-01T23:59")))
    }

    @Test
    fun `real time offset wraps around the day`() {
        assertEquals(2000L, WorldClock.ticks(TimePolicy.RealTime(offsetHours = 3), at("2025-01-01T05:00")))
        assertEquals(1000L, WorldClock.ticks(TimePolicy.RealTime(offsetHours = 3), at("2025-01-01T04:00")))
        // 負のオフセットでも負の tick にならない
        assertEquals(16000L, WorldClock.ticks(TimePolicy.RealTime(offsetHours = -2), at("2025-01-01T00:00")))
        assertEquals(23000L, WorldClock.ticks(TimePolicy.RealTime(offsetHours = -1), at("2025-01-01T06:00")))
    }

    @Test
    fun `fixed hour ignores the clock`() {
        assertEquals(0L, WorldClock.ticks(TimePolicy.Fixed(6), at("2025-01-01T21:00")))
        assertEquals(18000L, WorldClock.ticks(TimePolicy.Fixed(0), at("2025-01-01T21:00")))
        assertEquals(17000L, WorldClock.ticks(TimePolicy.Fixed(23), at("2025-01-01T21:00")))
        assertThrows<IllegalArgumentException> { TimePolicy.Fixed(24) }
    }

    @Test
    fun `vanilla leaves the time unmanaged`() {
        assertNull(WorldClock.ticks(TimePolicy.Vanilla, at("2025-01-01T12:00")))
    }
}
