package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 天候に関する設定を保持するデータクラス
 * 
 * @property constant 固定の天候・時間設定
 * @property dayCycle 昼夜サイクルの設定
 * @property weather 天候変化の設定
 * @property hashPepper ハッシュ計算用のペッパー値
 */
@Serializable
data class ClimateConfig(
    val constant: ConstantConfig = ConstantConfig(),
    val dayCycle: DayCycleConfig = DayCycleConfig(),
    val weather: WeatherConfig = WeatherConfig(),
    val hashPepper: String = "pepper",
) 