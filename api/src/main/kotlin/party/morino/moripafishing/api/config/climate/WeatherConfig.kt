package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 天候変化の設定を保持するデータクラス
 *
 * @property weight 各天候タイプの出現確率の重み付け
 * @property offset 天気の更新タイミングのオフセット（時間）
 * @property interval 天気の更新間隔（時間）
 * @property maxInclination 天気の変化の最大傾斜（度）
 */
@Serializable
data class WeatherConfig(
    val weight: Map<WeatherType, Int> = mapOf(
        WeatherType.SUNNY to 4,
        WeatherType.CLOUDY to 2,
        WeatherType.RAINY to 2,
        WeatherType.THUNDERSTORM to 1,
    ),
    val offset: Int = 0,
    val interval: Int = 8,
    val maxInclination: Int = 30,
)
