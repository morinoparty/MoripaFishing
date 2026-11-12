package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.Serializable

/**
 * 釣りワールドの気候 (天候と時刻) の方針。
 *
 * コアは方針に従って値を決定するだけで、Bukkit ワールドへの適用は Integration が行う。
 * `FishingWorldConfig.climate` でワールドごとに上書きでき、未設定の場合は `WorldConfig.defaultClimate` を使う。
 *
 * @property weather 天候の決め方
 * @property time 時刻の決め方
 */
@Serializable
data class ClimateConfig(
    val weather: WeatherPolicy = WeatherPolicy.Random(),
    val time: TimePolicy = TimePolicy.RealTime(),
)
