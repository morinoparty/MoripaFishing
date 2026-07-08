package party.morino.moripafishing.api.model.rarity

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.utils.serializer.RarityIdSerializer

/**
 * レアリティのIDを表すデータクラス
 *
 * `RarityData` への解決は `RarityManager.getRarity(id)` を使用する。
 *
 * @property value レアリティの一意の識別子
 */
@Serializable(with = RarityIdSerializer::class)
data class RarityId(
    val value: String,
) {
    /**
     * このレアリティの表示名に対応する翻訳キーを返す。
     */
    fun toTranslateKey(): String = "moripa_fishing.rarity.$value.name"
}
