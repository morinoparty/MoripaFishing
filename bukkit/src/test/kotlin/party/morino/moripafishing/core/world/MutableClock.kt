package party.morino.moripafishing.core.world

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** テストから進められる [Clock]。 */
internal class MutableClock(
    var instant: Instant,
    private val zone: ZoneId = ZoneOffset.UTC,
) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClockView(this, zone)

    override fun instant(): Instant = instant

    private class MutableClockView(
        private val source: MutableClock,
        private val zone: ZoneId,
    ) : Clock() {
        override fun getZone(): ZoneId = zone

        override fun withZone(zone: ZoneId): Clock = MutableClockView(source, zone)

        override fun instant(): Instant = source.instant
    }
}
