package party.morino.moripafishing.addons.chum

import party.morino.moripafishing.addons.chum.config.ChumDefinition
import party.morino.moripafishing.addons.chum.config.ChumItemData
import party.morino.moripafishing.addons.chum.config.ChumScope
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.fish.FishItemData
import party.morino.moripafishing.api.model.fish.FishSize
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import java.util.Locale

/**
 * テスト用のレアリティ・魚・撒き餌。
 */
object ChumFixtures {
    val world = FishingWorldId("default")

    fun rarity(
        id: String,
        weight: Double,
    ) = RarityData(RarityId(id), mapOf(Locale.JAPAN to id), weight, "1")

    fun fish(
        id: String,
        rarity: String,
        weight: Double = 1.0,
    ) = FishData(
        id = FishId(id),
        rarity = RarityId(rarity),
        displayName = mapOf(Locale.JAPAN to id),
        item = FishItemData("COD"),
        size = FishSize(10.0, 20.0),
        weight = weight,
    )

    /** common (90): c / rare (10): a, b / 餌限定の bait (rare, 重み 0)。 */
    val rarities = listOf(rarity("common", 90.0), rarity("rare", 10.0))
    val fishes = listOf(fish("c", "common"), fish("a", "rare"), fish("b", "rare"), fish("bait", "rare", 0.0))

    fun chum(
        scope: ChumScope = ChumScope.ANGLER,
        durationSeconds: Long = 60,
        charges: Int? = null,
        radius: Double? = null,
        rarityMultipliers: Map<String, Double> = emptyMap(),
        fishMultipliers: Map<String, Double> = emptyMap(),
        fishWeights: Map<String, Double> = emptyMap(),
    ) = ChumDefinition(
        item = ChumItemData("COD", "<aqua>Chum"),
        scope = scope,
        durationSeconds = durationSeconds,
        charges = charges,
        radius = radius,
        rarityMultipliers = rarityMultipliers,
        fishMultipliers = fishMultipliers,
        fishWeights = fishWeights,
    )

    fun location(
        x: Double = 0.0,
        z: Double = 0.0,
        world: FishingWorldId = this.world,
    ) = LocationData(world, x, 64.0, z, 0.0, 0.0)
}
