package party.morino.moripafishing.api.model.world

import kotlinx.serialization.Serializable

/**
 * 釣りワールドのスポーン地点。
 *
 * ワールドはこれを保持する `FishingWorldConfig` から自明なため、ワールド ID を持たない。
 *
 * @property x X 座標
 * @property y Y 座標
 * @property z Z 座標
 * @property yaw ヨー角
 * @property pitch ピッチ角
 */
@Serializable
data class SpawnPoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
)
