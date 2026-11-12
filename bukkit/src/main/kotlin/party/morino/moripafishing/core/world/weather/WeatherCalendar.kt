package party.morino.moripafishing.core.world.weather

import party.morino.moripafishing.api.config.climate.WeatherPolicy
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.security.MessageDigest
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.floor

/**
 * [WeatherPolicy.Random] の内蔵天候カレンダー。
 *
 * 時刻を `intervalHours` ごとのスロットに区切り、スロット番号から決定的ノイズ (0..100) を求めて
 * `weights` の累積で天候を選ぶ。同じ設定・ワールド ID・時刻からは常に同じ天候が得られる純粋な計算であり、
 * Bukkit にも状態にも依存しない。
 *
 * @property policy 天候カレンダーの設定
 * @property worldId ノイズのハッシュに混ぜるワールド ID
 */
internal class WeatherCalendar(
    private val policy: WeatherPolicy.Random,
    private val worldId: FishingWorldId,
) {
    /**
     * 指定時刻の天候を返す。
     */
    fun at(time: ZonedDateTime): WeatherType = weatherOfSlot(slotOf(time))

    /**
     * 指定時刻が属するスロット番号を返す。起点は `time` と同じタイムゾーンの 2023-10-01 00:00。
     */
    internal fun slotOf(time: ZonedDateTime): Long {
        val origin = ZonedDateTime.of(2023, 10, 1, 0, 0, 0, 0, time.zone)
        val seconds = Duration.between(origin, time).seconds
        return Math.floorDiv(seconds - policy.offsetHours * SECONDS_PER_HOUR, policy.intervalHours * SECONDS_PER_HOUR)
    }

    /**
     * スロットの天候を返す。ノイズを重みの合計に正規化し、マップ順に累積して選ぶ。
     */
    internal fun weatherOfSlot(slot: Long): WeatherType {
        val total = policy.weights.values.sum()
        val normalized = noise(slot).toDouble() / NOISE_RANGE * total
        var accumulated = 0
        for ((weather, weight) in policy.weights) {
            accumulated += weight
            if (normalized < accumulated) return weather
        }
        return policy.weights.keys.last()
    }

    /**
     * スロット `x` のノイズ (0..100)。`check` スロットごとに乱数を引き、その間は線形補間する。
     */
    internal fun noise(x: Long): Long {
        val check = floor(NOISE_RANGE / policy.maxInclination.toDouble()).toLong()
        val offset = Math.floorMod(x, check)
        if (offset == 0L) return random(x).toLong()
        val small = random(x - offset)
        val large = random(x - offset + check)
        return (large - small) / check * offset + small
    }

    /**
     * スロット `x` の乱数 (0..100)。
     */
    private fun random(x: Long): Int = Math.floorMod(hash(x), NOISE_RANGE + 1L).toInt()

    /**
     * `worldId + seed + x` の SHA-256 の先頭 8 桁 (16 進) を整数にする。
     */
    private fun hash(x: Long): Long {
        val digest = MessageDigest.getInstance("SHA-256").digest((worldId.value + policy.seed + x).toByteArray())
        val hex = digest.joinToString("") { "%02x".format(it) }
        return hex.substring(0, HASH_HEX_DIGITS).toLong(HEX_RADIX)
    }

    private companion object {
        const val SECONDS_PER_HOUR = 3600L
        const val NOISE_RANGE = 100
        const val HASH_HEX_DIGITS = 8
        const val HEX_RADIX = 16
    }
}
