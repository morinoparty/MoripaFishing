package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.serialization.KeySerializer

/**
 * 釣りワールドの天候の決め方。JSON では `type` で種類を指定する。
 */
@Serializable
sealed interface WeatherPolicy {
    /**
     * 内蔵カレンダー (決定的ノイズ) で天候を決定し、ワールドへ適用させる。
     *
     * @property weights 天候ごとの重み。マップの順に累積して抽選する
     * @property intervalHours 天候が切り替わる間隔 (時間)
     * @property offsetHours 切り替えタイミングのずらし幅 (時間)。負の値も可
     * @property maxInclination ノイズの最大傾斜 (1..100)。大きいほど天候が急に変わる
     * @property seed ノイズのハッシュに混ぜるシード文字列
     */
    @Serializable
    @SerialName("random")
    data class Random(
        val weights: Map<WeatherType, Int> = DEFAULT_WEIGHTS,
        val intervalHours: Int = 8,
        val offsetHours: Int = 0,
        val maxInclination: Int = 30,
        val seed: String = "pepper",
    ) : WeatherPolicy {
        init {
            require(weights.isNotEmpty() && weights.values.all { it >= 0 } && weights.values.sum() > 0) {
                "weights must be non-empty, non-negative and have a positive sum: $weights"
            }
            require(intervalHours > 0) { "intervalHours must be positive: $intervalHours" }
            require(maxInclination in 1..MAX_INCLINATION) { "maxInclination must be in 1..100: $maxInclination" }
        }
    }

    /**
     * 常に同じ天候。ワールドへ適用させる。
     *
     * @property weather 固定する天候
     */
    @Serializable
    @SerialName("fixed")
    data class Fixed(
        val weather: WeatherType,
    ) : WeatherPolicy

    /**
     * バニラの天候を読み取り、魚の条件にのみ使う。ワールドの天候は変更させない (`ADVANCE_WEATHER = true`)。
     */
    @Serializable
    @SerialName("vanilla")
    data object Vanilla : WeatherPolicy

    /**
     * `ServicesManager` に登録された `WeatherSource` (キー一致) に委ねる。
     * 未登録の場合は既定値の [Random] で代替し、警告を一度だけ出す。
     *
     * @property source 使用する `WeatherSource` のキー
     */
    @Serializable
    @SerialName("external")
    data class External(
        @Serializable(with = KeySerializer::class)
        val source: Key,
    ) : WeatherPolicy
}

private const val MAX_INCLINATION = 100

/** 既定の天候の重み (晴れ 4 : 曇り 1 : 雨 2 : 雷雨 1)。 */
private val DEFAULT_WEIGHTS =
    mapOf(
        WeatherType.SUNNY to 4,
        WeatherType.CLOUDY to 1,
        WeatherType.RAINY to 2,
        WeatherType.THUNDERSTORM to 1,
    )
