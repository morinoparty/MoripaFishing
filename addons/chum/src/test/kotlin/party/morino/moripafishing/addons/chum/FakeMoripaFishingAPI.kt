package party.morino.moripafishing.addons.chum

import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishDrawPool
import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 与えたレアリティ・魚・釣り人だけを返す [MoripaFishingAPI]。
 *
 * [getFishDrawer] は晴れの天候で候補を作るだけで、抽選はできない。
 * 登録されていない釣り人は釣りワールド外にいるものとして返す。
 */
class FakeMoripaFishingAPI(
    private val rarities: List<RarityData> = emptyList(),
    private val fishes: List<FishData> = emptyList(),
    private val anglers: List<Angler> = emptyList(),
) : MoripaFishingAPI {
    override fun getConfigManager(): ConfigManager = error("not used")

    override fun getFishManager(): FishManager =
        object : FishManager {
            override fun register(fish: FishData) = error("not used")

            override fun unregister(id: FishId): Boolean = error("not used")

            override fun get(id: FishId): FishData? = fishes.firstOrNull { it.id == id }

            override fun getAll(): List<FishData> = fishes

            override fun getByRarity(rarity: RarityId): List<FishData> = fishes.filter { it.rarity == rarity }
        }

    override fun getRarityManager(): RarityManager =
        object : RarityManager {
            override fun register(rarity: RarityData) = error("not used")

            override fun unregister(id: RarityId): Boolean = error("not used")

            override fun get(id: RarityId): RarityData? = rarities.firstOrNull { it.id == id }

            override fun getAll(): List<RarityData> = rarities
        }

    override fun getFishDrawer(): FishDrawer =
        object : FishDrawer {
            override fun createPool(world: FishingWorldId): FishDrawPool = FishDrawPool(world, WeatherType.SUNNY, rarities, fishes)

            override fun draw(pool: FishDrawPool): Fish? = error("not used")

            override fun roll(data: FishData): Fish = error("not used")
        }

    override fun getWorldManager(): WorldManager = error("not used")

    override fun getAnglerManager(): AnglerManager =
        object : AnglerManager {
            override fun getAngler(id: AnglerId): Angler = anglers.firstOrNull { it.getId() == id } ?: FakeAngler(id, null)

            override fun getOnlineAnglers(): List<Angler> = anglers
        }
}
