package party.morino.moripafishing.core.world

import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 天候の決定結果。
 *
 * @property weather 魚の抽選条件に使う天候
 * @property appliesToWorld この天候をワールドへ適用させるかどうか
 */
internal data class WeatherDecision(
    val weather: WeatherType,
    val appliesToWorld: Boolean,
)
