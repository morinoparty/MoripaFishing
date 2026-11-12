package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.time.DateTimeException
import java.time.ZoneId

/**
 * 釣りワールド全体に関する設定 (`config.json` の `world`)。
 *
 * @property defaultId デフォルトの釣りワールド ID
 * @property timeZone 天候カレンダーと実時刻同期に使うタイムゾーン ID (例: `Asia/Tokyo`)。不正な ID は読み込み時に拒否する
 * @property refreshIntervalSeconds ワールド状態を再決定・適用する間隔 (秒)。1 以上
 * @property defaultClimate ワールドごとの `climate` が `null` の場合に使う気候設定
 */
@Serializable
data class WorldConfig(
    val defaultId: FishingWorldId = FishingWorldId("default"),
    val timeZone: String = "Asia/Tokyo",
    val refreshIntervalSeconds: Int = 60,
    val defaultClimate: ClimateConfig = ClimateConfig(),
) {
    init {
        require(refreshIntervalSeconds > 0) { "refreshIntervalSeconds must be positive: $refreshIntervalSeconds" }
        try {
            ZoneId.of(timeZone)
        } catch (e: DateTimeException) {
            throw IllegalArgumentException("timeZone is not a valid zone ID: $timeZone", e)
        }
    }
}
