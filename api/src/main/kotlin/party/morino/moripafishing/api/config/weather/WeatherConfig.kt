package party.morino.moripafishing.api.config.weather

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.WeatherType

// 天候に関する設定を保持するデータクラス
@Serializable
data class WeatherConfig(
    val dayCycleTimeZone: String = "Asia/Tokyo", // 日周期のタイムゾーン
    val interval: Int = 8, // 天候の更新間隔
    val offset: Int = 0, // 天候のオフセット
    val maxInclination: Int = 30, // 天候の最大傾斜
    val weatherSetting: Map<WeatherType, Int> = mapOf(
        WeatherType.SUNNY to 30,
        WeatherType.CLOUDY to 20,
        WeatherType.RAINY to 20,
        WeatherType.THUNDERSTORM to 10
    ),
    val hashPepper: String = "pepper"
) 