package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable

/**
 * スポーン関連の設定を管理するクラス
 * @param spawnMobs モブをスポーンさせるかどうか
 * @param spawnMonsters モンスターをスポーンさせるかどうか
 * @param spawnAnimals 動物をスポーンさせるかどうか
 * @param receiveDamage ダメージを受けるかどうか
 */
@Serializable
data class SpawnConfig(
    val spawnMobs: Boolean = false,
    val spawnMonsters: Boolean = false,
    val spawnAnimals: Boolean = false,
    val receiveDamage: Boolean = false,
)
