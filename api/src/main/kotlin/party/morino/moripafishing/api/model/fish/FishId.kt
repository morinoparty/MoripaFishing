package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import party.morino.moripafishing.api.serialization.FishIdSerializer

/**
 * 魚のIDを表すデータクラス
 * @property value 魚の一意の識別子
 */
@Serializable(with = FishIdSerializer::class)
data class FishId(
    val value: String,
) {
    /**
     * この魚の表示名に対応する翻訳キーを返す。
     */
    fun toTranslateKey(): String = "moripa_fishing.fish.$value.name"

    /**
     * この魚の表示名を表す翻訳コンポーネントを返す。
     *
     * 翻訳キーが登録されていない場合 (定義が削除された場合など) は、キーの代わりに ID を表示する。
     */
    fun toTranslatable(): Component = Component.translatable(toTranslateKey()).fallback(value)
}
