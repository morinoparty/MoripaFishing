package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.api.utils.serializer.KeySerializer

/**
 * 天候に関する設定を保持するデータクラス
 *
 * ワールドごとの天候制御・時間同期の有効/無効を管理する。
 * `WorldDetailConfig.climateConfig` でワールドごとにオーバーライド可能で、
 * 未設定の場合は `WorldConfig.defaultClimateConfig` がフォールバックとして使用される。
 *
 * @property weatherSource 天候を決定するソースの名前空間キー。`WeatherSourceRegistry` に登録された
 *   `WeatherSource` から解決される。
 *   組み込みでは `moripafishing:internal`（内蔵ランダマイザーで決定し Bukkit にも適用、
 *   `DO_WEATHER_CYCLE = false`）と `moripafishing:vanilla`（バニラ天候を読み取り魚の抽選条件に
 *   のみ使用、`DO_WEATHER_CYCLE = true`）を提供する。
 *   外部プラグインは `MoripaFishingAPI.registerWeatherSource` で独自キーのソースを登録できる。
 *   未登録のキーが指定された場合は `moripafishing:internal` にフォールバックする。
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
    @Serializable(with = KeySerializer::class)
    val weatherSource: Key = WeatherSource.INTERNAL,
    val enableDayCycle: Boolean = true,
    val constant: ConstantConfig = ConstantConfig(),
    val dayCycle: DayCycleConfig = DayCycleConfig(),
    val weather: WeatherConfig = WeatherConfig(),
    val hashPepper: String = "pepper",
)
