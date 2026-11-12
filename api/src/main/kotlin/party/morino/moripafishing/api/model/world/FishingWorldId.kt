package party.morino.moripafishing.api.model.world

import kotlinx.serialization.Serializable
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
}
