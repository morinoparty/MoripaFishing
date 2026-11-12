package party.morino.moripafishing.testing

import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * 検証せずにレアリティを保持する [RarityManager]。
 */
internal class FakeRarityManager(
    vararg rarities: RarityData,
) : RarityManager {
    private val rarities = rarities.associateByTo(LinkedHashMap()) { it.id }

    override fun register(rarity: RarityData) {
        rarities[rarity.id] = rarity
    }

    override fun unregister(id: RarityId): Boolean = rarities.remove(id) != null

    override fun get(id: RarityId): RarityData? = rarities[id]

    override fun getAll(): List<RarityData> = rarities.values.toList()
}
