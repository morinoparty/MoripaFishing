package party.morino.moripafishing.api.model.world

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import party.morino.moripafishing.api.serialization.FishingWorldIdSerializer

/**
 * ワールドのIDを表すデータクラス
 * @property value ワールドの一意の識別子
 */
@Serializable(with = FishingWorldIdSerializer::class)
data class FishingWorldId(
    val value: String,
) {
    /**
     * このワールドの表示名に対応する翻訳キーを返す。
     */
    fun toTranslateKey(): String = "moripa_fishing.world.$value.name"

    /**
     * このワールドの表示名を表す翻訳コンポーネントを返す。
     *
     * 翻訳キーが登録されていない場合 (定義が削除された場合など) は、キーの代わりに ID を表示する。
     */
    fun toTranslatable(): Component = Component.translatable(toTranslateKey()).fallback(value)
}
