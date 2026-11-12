package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable

/**
 * 魚アイテムの見た目。
 *
 * @property material アイテムの Material 名 (例: `COD`)。アイテムとして使える Material であること
 * @property customModelData カスタムモデルデータの float 値
 * @property skullTexture `PLAYER_HEAD` に適用するテクスチャ (`textures` プロパティの Base64 値)
 */
@Serializable
data class FishItemData(
    val material: String,
    val customModelData: List<Float> = emptyList(),
    val skullTexture: String? = null,
)
