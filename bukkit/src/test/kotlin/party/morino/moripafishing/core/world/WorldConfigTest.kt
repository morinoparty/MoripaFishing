package party.morino.moripafishing.core.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import party.morino.moripafishing.api.config.world.WorldConfig

class WorldConfigTest {
    @Test
    fun `an invalid time zone is rejected when the config is built`() {
        assertThrows<IllegalArgumentException> { WorldConfig(timeZone = "Not/AZone") }
    }

    @Test
    fun `a valid time zone is accepted`() {
        assertEquals("UTC", WorldConfig(timeZone = "UTC").timeZone)
    }

    @Test
    fun `a non-positive refresh interval is rejected`() {
        assertThrows<IllegalArgumentException> { WorldConfig(refreshIntervalSeconds = 0) }
    }
}
