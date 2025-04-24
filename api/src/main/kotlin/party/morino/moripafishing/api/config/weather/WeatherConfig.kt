package party.morino.moripafishing.api.config.weather

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 天候に関する設定を保持するデータクラス
 *
 * @property weatherUpdateInterval 天候の更新間隔（時間単位）
 * @property weatherUpdateOffset 天候更新のオフセット（時間単位）
 * @property isAutoDayCycleEnabled 自動で昼夜サイクルを変更するかどうか
 * @property dayCycleOffset 昼夜サイクルのオフセット（WorldConfigのデフォルトタイムゾーンからのずれ）
 * @property maxWeatherInclination 天候変化の最大傾斜角度
 * @property weatherProbabilities 各天候タイプの出現確率の重み付け
 * @property hashPepper ハッシュ計算用のペッパー値
 */
@Serializable
data class WeatherConfig(
    val weatherUpdateInterval: Int = 8, 
    val weatherUpdateOffset: Int = 4,
    val isAutoDayCycleEnabled: Boolean = true,
    val dayCycleOffset: Int = 0,
    val maxWeatherInclination: Int = 30,
    val weatherProbabilities: Map<WeatherType, Int> = mapOf(
        WeatherType.SUNNY to 4,
        WeatherType.CLOUDY to 2,
        WeatherType.RAINY to 2,
        WeatherType.THUNDERSTORM to 1,
    ),
    val hashPepper: String = "pepper",
)
