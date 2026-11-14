package party.morino.moripafishing.api.model.rarity

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import party.morino.moripafishing.api.serialization.RarityIdSerializer

/**
 * レアリティのIDを表すデータクラス
 *
 * `RarityData` への解決は `RarityManager.get(id)` を使用する。
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

    /**
     * このレアリティの表示名を表す翻訳コンポーネントを返す。
     *
     * 翻訳キーが登録されていない場合 (定義が削除された場合など) は、キーの代わりに ID を表示する。
     */
    fun toTranslatable(): Component = Component.translatable(toTranslateKey()).fallback(value)
}
