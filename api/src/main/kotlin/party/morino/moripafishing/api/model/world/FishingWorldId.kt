package party.morino.moripafishing.api.model.world

import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.utils.serializer.FishingWorldIdSerializer

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
     * このワールドの表示名を参照する MiniMessage の `<lang>` タグを返す。
     */
    fun localeTag(): String = "<lang:${toTranslateKey()}>"

    /**
     * このワールドに対応する Bukkit ワールドのキー (`moripafishing:<value>`) を返す。
     */
    fun toWorldKey(): Key = Key.key(WORLD_KEY_NAMESPACE, value)

    companion object {
        /**
         * 釣りワールドの Bukkit ワールドキーに使う名前空間。
         */
        const val WORLD_KEY_NAMESPACE = "moripafishing"

        /**
         * Bukkit ワールドのキーから釣りワールドの ID を返す。
         * 名前空間が [WORLD_KEY_NAMESPACE] でない場合は `null` を返す。
         */
        fun fromWorldKey(key: Key): FishingWorldId? = if (key.namespace() == WORLD_KEY_NAMESPACE) FishingWorldId(key.value()) else null
    }
}
