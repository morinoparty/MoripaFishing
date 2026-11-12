package party.morino.moripafishing.testing

import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * 検証せずに魚を保持する [FishManager]。
 */
internal class FakeFishManager(
    vararg fishes: FishData,
) : FishManager {
    private val fishes = fishes.associateByTo(LinkedHashMap()) { it.id }

    override fun register(fish: FishData) {
        fishes[fish.id] = fish
    }

    override fun unregister(id: FishId): Boolean = fishes.remove(id) != null

    override fun get(id: FishId): FishData? = fishes[id]

    override fun getAll(): List<FishData> = fishes.values.toList()

    override fun getByRarity(rarity: RarityId): List<FishData> = fishes.values.filter { it.rarity == rarity }
}
