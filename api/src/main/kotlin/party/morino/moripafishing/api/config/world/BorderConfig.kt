package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable

/**
 * 釣りワールドのワールドボーダー設定。
 *
 * @property centerX ボーダー中心の X 座標
 * @property centerZ ボーダー中心の Z 座標
 * @property size ボーダーの一辺の長さ。0 より大きいこと
 */
@Serializable
data class BorderConfig(
    val centerX: Double = 0.0,
    val centerZ: Double = 0.0,
    val size: Double = 100.0,
) {
    init {
        require(size > 0.0) { "border size must be positive: $size" }
    }
}
