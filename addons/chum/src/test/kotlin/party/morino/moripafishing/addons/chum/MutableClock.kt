package party.morino.moripafishing.addons.chum

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * テストから進められる [Clock]。
 */
class MutableClock(
    private var now: Instant = Instant.parse("2026-01-01T00:00:00Z"),
) : Clock() {
    /** 時刻を [duration] だけ進める。 */
    fun advance(duration: Duration) {
        now = now.plus(duration)
    }

    override fun instant(): Instant = now

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = this
}
