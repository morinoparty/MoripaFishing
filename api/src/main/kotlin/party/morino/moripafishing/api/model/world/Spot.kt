package party.morino.moripafishing.api.model.world

import kotlinx.serialization.Serializable

/**
 * 位置と半径を表すデータクラス
 * @param location 釣り場の位置
 * @param radius 釣り場の半径 なお、これは円形
 */
@Serializable
data class Spot(
    val location: Location,
    val radius: Double,
)
