package party.morino.moripafishing.testing

import party.morino.moripafishing.api.model.fish.FishConditions
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.fish.FishItemData
import party.morino.moripafishing.api.model.fish.FishSize
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import java.util.Locale

/** テスト用のレアリティを作る。 */
internal fun rarity(
    id: String,
    weight: Double = 1.0,
    worth: String = "1",
): RarityData = RarityData(RarityId(id), mapOf(Locale.JAPAN to id), weight, worth)

/** テスト用の魚を作る。 */
internal fun fish(
    id: String,
    rarity: String,
    weight: Double = 1.0,
    worth: String? = null,
    conditions: FishConditions = FishConditions(),
    item: FishItemData = FishItemData("COD"),
    lore: Map<Locale, List<String>> = emptyMap(),
): FishData =
    FishData(
        id = FishId(id),
        rarity = RarityId(rarity),
        displayName = mapOf(Locale.JAPAN to id),
        lore = lore,
        item = item,
        size = FishSize(10.0, 20.0),
        weight = weight,
        worth = worth,
        conditions = conditions,
    )
