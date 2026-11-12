package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 魚が釣れる条件。空の項目は制限なしを表す。
 *
 * @property weather 釣れる天候
 * @property world 釣れる釣りワールド
 */
@Serializable
data class FishConditions(
    val weather: Set<WeatherType> = emptySet(),
    val world: Set<FishingWorldId> = emptySet(),
) {
    /**
     * 条件を満たすかどうかを返す。空の条件は常に満たす。
     * [weather] が `null` (天候不明) の場合、天候条件付きの魚は対象外になる。
     *
     * @param world 釣りワールドの ID
     * @param weather 現在の天候
     */
    fun matches(
        world: FishingWorldId,
        weather: WeatherType?,
    ): Boolean =
        (this.world.isEmpty() || world in this.world) &&
            (this.weather.isEmpty() || (weather != null && weather in this.weather))
}
