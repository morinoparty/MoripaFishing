package party.morino.moripafishing.addons.catchannounce

import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * 与えたレアリティと釣り人だけを返す [MoripaFishingAPI]。レアリティと釣り人以外のマネージャーは使えない。
 */
class FakeMoripaFishingAPI(
    rarities: List<RarityData> = emptyList(),
    private val anglers: List<Angler> = emptyList(),
) : MoripaFishingAPI,
    RarityManager,
    AnglerManager {
    private val rarities = rarities.associateByTo(LinkedHashMap()) { it.id }

    override fun getConfigManager(): ConfigManager = error("not used")

    override fun getFishManager(): FishManager = error("not used")

    override fun getRarityManager(): RarityManager = this

    override fun getFishDrawer(): FishDrawer = error("not used")

    override fun getWorldManager(): WorldManager = error("not used")

    override fun getAnglerManager(): AnglerManager = this

    override fun register(rarity: RarityData) {
        rarities[rarity.id] = rarity
    }

    override fun unregister(id: RarityId): Boolean = rarities.remove(id) != null

    override fun get(id: RarityId): RarityData? = rarities[id]

    override fun getAll(): List<RarityData> = rarities.values.toList()

    override fun getAngler(id: AnglerId): Angler = anglers.first { it.getId() == id }

    override fun getOnlineAnglers(): List<Angler> = anglers
}
