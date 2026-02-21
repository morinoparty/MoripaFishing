package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 天候に関する設定を保持するデータクラス
 *
 * ワールドごとの天候制御・時間同期の有効/無効を管理する。
 * `WorldDetailConfig.climateConfig` でワールドごとにオーバーライド可能で、
 * 未設定の場合は `WorldConfig.defaultClimateConfig` がフォールバックとして使用される。
 *
 * @property enableWeather プラグインによる天候制御を有効にするかどうか。
 *   `true` の場合、`DO_WEATHER_CYCLE` を `false` に設定しプラグインが天候を管理する。
 *   `false` の場合、`DO_WEATHER_CYCLE` を `true` に設定しバニラの天候サイクルに委ねる。
 * @property enableDayCycle プラグインによる時間同期を有効にするかどうか。
 *   `true` の場合、`DO_DAYLIGHT_CYCLE` を `false` に設定しプラグインがリアルタイム同期または固定時間を管理する。
 *   `false` の場合、`DO_DAYLIGHT_CYCLE` を `true` に設定しバニラの昼夜サイクルに委ねる。
 * @property constant 固定の天候・時間設定
 * @property dayCycle 昼夜サイクルの設定
 * @property weather 天候変化の設定
 * @property hashPepper ハッシュ計算用のペッパー値
 */
@Serializable
data class ClimateConfig(
    val enableWeather: Boolean = true,
    val enableDayCycle: Boolean = true,
    val constant: ConstantConfig = ConstantConfig(),
    val dayCycle: DayCycleConfig = DayCycleConfig(),
    val weather: WeatherConfig = WeatherConfig(),
    val hashPepper: String = "pepper",
)