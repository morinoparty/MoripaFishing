package party.morino.moripafishing.core.world

import party.morino.moripafishing.api.config.climate.TimePolicy
import java.time.ZonedDateTime

/**
 * [TimePolicy] からワールド時刻 (tick) を決める純粋関数。
 *
 * Minecraft の 0 tick は 6 時であるため、`時 * 1000 + 18000` を 24000 で割った余りを使う。
 */
internal object WorldClock {
    private const val TICKS_PER_HOUR = 1000L
    private const val MINUTES_PER_HOUR = 60L
    private const val MIDNIGHT_TICKS = 18_000L
    private const val TICKS_PER_DAY = 24_000L

    /**
     * 指定時刻におけるワールド時刻を返す。`null` の場合は時刻を管理しない。
     *
     * @param policy 時刻の方針
     * @param now 現在時刻 (`WorldConfig.timeZone`)
     */
    fun ticks(
        policy: TimePolicy,
        now: ZonedDateTime,
    ): Long? =
        when (policy) {
            is TimePolicy.RealTime ->
                Math.floorMod(
                    (now.hour + policy.offsetHours) * TICKS_PER_HOUR +
                        now.minute * TICKS_PER_HOUR / MINUTES_PER_HOUR +
                        MIDNIGHT_TICKS,
                    TICKS_PER_DAY,
                )
            is TimePolicy.Fixed -> Math.floorMod(policy.hour * TICKS_PER_HOUR + MIDNIGHT_TICKS, TICKS_PER_DAY)
            TimePolicy.Vanilla -> null
        }
}
