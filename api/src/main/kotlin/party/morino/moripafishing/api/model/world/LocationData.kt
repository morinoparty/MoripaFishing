package party.morino.moripafishing.api.model.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.FishingWorldId
/**
 * LocationDataクラスは、位置情報を表すデータクラスです。
 * @param x 位置のX座標
 * @param y 位置のY座標
 * @param z 位置のZ座標
 * @param yaw 位置のヨー角
 * @param pitch 位置のピッチ角
 */
@Serializable
data class LocationData(
    val worldId: FishingWorldId,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double,
    val pitch: Double,
)
