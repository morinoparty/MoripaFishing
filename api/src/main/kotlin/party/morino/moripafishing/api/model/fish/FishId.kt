package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.utils.serializer.FishIdSerializer

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
}
