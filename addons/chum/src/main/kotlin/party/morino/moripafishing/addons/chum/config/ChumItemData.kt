package party.morino.moripafishing.addons.chum.config

import kotlinx.serialization.Serializable

/**
 * 撒き餌アイテムの見た目。
 *
 * @property material アイテムの Material 名 (例: `COD`)。アイテムとして使える Material であること
 * @property name 表示名 (MiniMessage)
 * @property lore 説明文 (MiniMessage、1 要素 1 行)
 * @property customModelData カスタムモデルデータの float 値
 */
@Serializable
data class ChumItemData(
    val material: String,
    val name: String,
    val lore: List<String> = emptyList(),
    val customModelData: List<Float> = emptyList(),
)
