package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 釣りワールドの時刻の決め方。JSON では `type` で種類を指定する。
 */
@Serializable
sealed interface TimePolicy {
    /**
     * `WorldConfig.timeZone` の実時刻にワールド時刻を同期する (`ADVANCE_TIME = false`)。
     *
     * @property offsetHours 実時刻からのずらし幅 (時間)。負の値も可
     */
    @Serializable
    @SerialName("real_time")
    data class RealTime(
        val offsetHours: Int = 0,
    ) : TimePolicy

    /**
     * 常に同じ時刻に固定する (`ADVANCE_TIME = false`)。
     *
     * @property hour 固定する時 (0..23)
     */
    @Serializable
    @SerialName("fixed")
    data class Fixed(
        val hour: Int,
    ) : TimePolicy {
        init {
            require(hour in 0 until HOURS_PER_DAY) { "hour must be in 0..23: $hour" }
        }
    }

    /**
     * 時刻を変更させない (`ADVANCE_TIME = true`)。
     */
    @Serializable
    @SerialName("vanilla")
    data object Vanilla : TimePolicy
}

private const val HOURS_PER_DAY = 24
