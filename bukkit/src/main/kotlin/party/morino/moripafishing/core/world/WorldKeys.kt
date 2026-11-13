package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.config.world.WorldBinding
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りワールドから Bukkit ワールドのキーへの唯一の対応付け。
 */
internal object WorldKeys {
    /** 釣りワールド用 Bukkit ワールドの名前空間。 */
    const val NAMESPACE = WorldBinding.MANAGED_NAMESPACE

    /**
     * 釣りワールド ID に対応する Bukkit ワールドのキー (`moripafishing:<id>`) を返す。
     * キーとして不正な ID の場合は `InvalidKeyException` を投げる。
     */
    fun of(id: FishingWorldId): Key = Key.key(NAMESPACE, id.value)

    /**
     * 設定に対応する Bukkit ワールドのキーを返す。外部ワールドは登録したキー、管理ワールドは [of]。
     * 返すキーは [normalize] 済み。
     */
    fun of(config: FishingWorldConfig): Key =
        when (val world = config.world) {
            is WorldBinding.Managed -> of(config.id)
            is WorldBinding.External -> normalize(world.key)
        }

    /**
     * キーを Adventure の `Key` 実装に揃える。
     * `NamespacedKey` と Adventure の `Key` 実装は `hashCode` (実装によっては `equals`) が一致しないため、
     * キーを比較・マップのキーにする前に必ずこれを通す。
     */
    fun normalize(key: Key): Key = Key.key(key.namespace(), key.value())
}
