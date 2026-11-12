package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りワールド ID から Bukkit ワールドのキーへの唯一の対応付け。
 *
 * 外部ワールドへの対応 (#334) では `config.worldKey ?: of(id)` に置き換える。
 */
internal object WorldKeys {
    /** 釣りワールド用 Bukkit ワールドの名前空間。 */
    const val NAMESPACE = "moripafishing"

    /**
     * 釣りワールド ID に対応する Bukkit ワールドのキー (`moripafishing:<id>`) を返す。
     * キーとして不正な ID の場合は `InvalidKeyException` を投げる。
     */
    fun of(id: FishingWorldId): Key = Key.key(NAMESPACE, id.value)

    /**
     * キーを Adventure の `Key` 実装に揃える。
     * `NamespacedKey` と Adventure の `Key` 実装は `hashCode` (実装によっては `equals`) が一致しないため、
     * キーを比較・マップのキーにする前に必ずこれを通す。
     */
    fun normalize(key: Key): Key = Key.key(key.namespace(), key.value())
}
